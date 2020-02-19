package com.spldeolin.allison1875.da.core.processor;

import java.util.Collection;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.google.common.collect.Lists;
import com.spldeolin.allison1875.base.util.Locations;
import com.spldeolin.allison1875.da.core.domain.ApiDomain;
import com.spldeolin.allison1875.da.core.domain.BodyFieldDomain;
import com.spldeolin.allison1875.da.core.enums.BodyTypeEnum;
import com.spldeolin.allison1875.da.core.processor.result.BodyProcessResult;
import com.spldeolin.allison1875.da.core.processor.result.HandlerProcessResult;
import com.spldeolin.allison1875.da.core.processor.result.RequestMappingProcessResult;
import com.spldeolin.allison1875.da.core.processor.result.ValueStructureBodyProcessResult;
import com.spldeolin.allison1875.da.core.util.Javadocs;
import lombok.AllArgsConstructor;

/**
 * 处理一个handler的核心主流程，处理结果可以交给视图转化器进行输出
 *
 * @author Deolin 2019-12-03
 */
@AllArgsConstructor
public class MainProcessor {

    private final HandlerProcessResult handlerProcessorResult;

    public ApiDomain process() {
        ClassOrInterfaceDeclaration controller = handlerProcessorResult.controller();
        MethodDeclaration handler = handlerProcessorResult.handler();

        ApiDomain api = new ApiDomain();

        // method uri
        RequestMappingProcessResult requestMappingProcessResult = new RequestMappingProcessor()
                .process(controller, handler);
        api.method(requestMappingProcessResult.methodTypes());
        api.uri(requestMappingProcessResult.uris());

        // description
        api.description(Javadocs.extractFirstLine(handler));

        // path query
        api.pathVariableFields(new PathVariableProcessor().processor(handlerProcessorResult.pathVariables()));
        api.requestParamFields(new RequestParamProcessor().processor(handlerProcessorResult.requestParams()));

        // request body
        BodyProcessResult req = new BodyProcessor(handlerProcessorResult.requestBodyResolvedType()).process();
        this.calcRequestBodyType(api, req);
        this.processRequestBodyFields(api, req);

        // response body
        BodyProcessResult resp = new BodyProcessor(handlerProcessorResult.responseBodyResolvedType()).process();
        this.calcResponseBodyType(api, resp);
        this.processResponseBodyFields(api, resp);

        // authur
        api.author(new AuthorProcessor().process(controller, handler));

        // code source location
        api.codeSourceLocation(Locations.getRelativePath(handler) + ":" + Locations.getBeginLine(handler.getName()));

        return api;
    }

    private void calcRequestBodyType(ApiDomain api, BodyProcessResult req) {
        if (req.isVoidStructure()) {
            api.requestBodyType(BodyTypeEnum.none);
        }
        if (req.isChaosStructure()) {
            api.requestBodyType(BodyTypeEnum.chaos);
        }
        if (req.isValueStructure()) {
            if (req.inArray()) {
                api.requestBodyType(BodyTypeEnum.valueArray);
            } else {
                api.requestBodyType(BodyTypeEnum.va1ue);
            }
        }
        if (req.isKeyValueStructure()) {
            if (req.inArray()) {
                api.requestBodyType(BodyTypeEnum.keyValueArray);
            } else if (req.inPage()) {
                api.requestBodyType(BodyTypeEnum.keyValuePage);
            } else {
                api.requestBodyType(BodyTypeEnum.keyValue);
            }
        }
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

    private void calcResponseBodyType(ApiDomain api, BodyProcessResult resp) {
        if (resp.isVoidStructure()) {
            api.responseBodyType(BodyTypeEnum.none);
        }
        if (resp.isChaosStructure()) {
            api.responseBodyType(BodyTypeEnum.chaos);
        }
        if (resp.isValueStructure()) {
            if (resp.inArray()) {
                api.responseBodyType(BodyTypeEnum.valueArray);
            } else {
                api.responseBodyType(BodyTypeEnum.va1ue);
            }
        }
        if (resp.isKeyValueStructure()) {
            if (resp.inArray()) {
                api.responseBodyType(BodyTypeEnum.keyValueArray);
            } else if (resp.inPage()) {
                api.responseBodyType(BodyTypeEnum.keyValuePage);
            } else {
                api.responseBodyType(BodyTypeEnum.keyValue);
            }
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