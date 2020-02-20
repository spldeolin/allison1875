package com.spldeolin.allison1875.da.core.processor;

import java.util.Collection;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.google.common.collect.Lists;
import com.spldeolin.allison1875.da.core.domain.ApiDomain;
import com.spldeolin.allison1875.da.core.domain.BodyFieldDomain;
import com.spldeolin.allison1875.da.core.processor.result.BodyProcessResult;
import com.spldeolin.allison1875.da.core.processor.result.HandlerProcessResult;
import com.spldeolin.allison1875.da.core.processor.result.ValueStructureBodyProcessResult;
import com.spldeolin.allison1875.da.core.strategy.DefaultHandlerFilter;
import com.spldeolin.allison1875.da.core.strategy.ReturnStmtBaseResponseBodyTypeParser;
import lombok.AllArgsConstructor;

/**
 * 处理一个handler的核心主流程，处理结果可以交给视图转化器进行输出
 *
 * @author Deolin 2019-12-03
 */
@AllArgsConstructor
public class MainProcessor {

    public Collection<ApiDomain> process() {
        Collection<HandlerProcessResult> handlerInfos = new HandlerProcessor().process(new DefaultHandlerFilter() {
            @Override
            public boolean filter(ClassOrInterfaceDeclaration controller) {
                return true;
            }
        }, new ReturnStmtBaseResponseBodyTypeParser());

        Collection<ApiDomain> apis = Lists.newLinkedList();
        handlerInfos.forEach(handlerInfo -> {
            ClassOrInterfaceDeclaration controller = handlerInfo.controller();
            MethodDeclaration handler = handlerInfo.handler();
            ApiDomain api = new ApiDomain();

            RouteProcessor routeProcessor = new RouteProcessor().controller(controller).handler(handler).process();
            api.method(routeProcessor.methodTypes());
            api.uri(routeProcessor.uris());

            DescriptionProcessor descriptionProcessor = new DescriptionProcessor().handler(handler).process();
            api.description(descriptionProcessor.description());

            PathVariableProcessor pathVariableProcessor = new PathVariableProcessor()
                    .parameters(handlerInfo.pathVariables()).process();
            api.pathVariableFields(pathVariableProcessor.fields());

            RequestParamProcessor requestParamProcessor = new RequestParamProcessor()
                    .parameters(handlerInfo.requestParams()).process();
            api.requestParamFields(requestParamProcessor.fields());

            // request body
            BodyProcessResult req = new BodyProcessor(handlerInfo.requestBodyResolvedType()).process();
            api.requestBodyType(req.calcBodyType());
            this.processRequestBodyFields(api, req);

            // response body
            BodyProcessResult resp = new BodyProcessor(handlerInfo.responseBodyResolvedType()).process();
            api.responseBodyType(resp.calcBodyType());
            this.processResponseBodyFields(api, resp);

            AuthorProcessor authorProcessor = new AuthorProcessor().controller(controller).handler(handler).process();
            api.author(authorProcessor.author());

            CodeSourceProcessor processor = new CodeSourceProcessor().handler(handler).process();
            api.codeSource(processor.location());

            apis.add(api);
        });

        return apis;
    }

    private void processRequestBodyFields(ApiDomain api, BodyProcessResult req) {
        if (req.isKeyValueStructure()) {
            new BodyFieldProcessor(true).process(req.asKeyValueStructure().objectSchema(), api);
        }
        if (req.isValueStructure()) {
            ValueStructureBodyProcessResult valueStruct = req.asValueStructure();
            Collection<BodyFieldDomain> field = Lists.newArrayList(
                    new BodyFieldDomain().jsonType(valueStruct.valueStructureJsonType())
                            .numberFormat(valueStruct.valueStructureNumberFormat()));
            api.requestBodyFields(field);
        }
        if (req.isChaosStructure()) {
            api.requestBodyChaosJsonSchema(req.asChaosStructure().jsonSchema());
        }
    }

    private void processResponseBodyFields(ApiDomain api, BodyProcessResult resp) {
        if (resp.isKeyValueStructure()) {
            new BodyFieldProcessor(false).process(resp.asKeyValueStructure().objectSchema(), api);
        }
        if (resp.isValueStructure()) {
            ValueStructureBodyProcessResult valueStruct = resp.asValueStructure();
            Collection<BodyFieldDomain> field = Lists.newArrayList(
                    new BodyFieldDomain().jsonType(valueStruct.valueStructureJsonType())
                            .numberFormat(valueStruct.valueStructureNumberFormat()));
            api.responseBodyFields(field);
        }
        if (resp.isChaosStructure()) {
            api.responseBodyChaosJsonSchema(resp.asChaosStructure().jsonSchema());
        }
    }

}