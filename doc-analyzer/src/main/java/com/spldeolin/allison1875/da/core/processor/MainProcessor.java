package com.spldeolin.allison1875.da.core.processor;

import java.util.Collection;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.google.common.collect.Lists;
import com.spldeolin.allison1875.da.core.domain.ApiDomain;
import com.spldeolin.allison1875.da.core.processor.result.HandlerProcessResult;
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

            // uri、请求方法
            RouteProcessor routeProcessor = new RouteProcessor().controller(controller).handler(handler).process();
            api.method(routeProcessor.methodTypes());
            api.uri(routeProcessor.uris());

            // 描述
            DescriptionProcessor descriptionProcessor = new DescriptionProcessor().handler(handler).process();
            api.description(descriptionProcessor.description());

            // @PathVariable
            PathVariableProcessor pathVariableProcessor = new PathVariableProcessor()
                    .parameters(handlerInfo.pathVariables()).process();
            api.pathVariableFields(pathVariableProcessor.fields());

            // @RequestParam
            RequestParamProcessor requestParamProcessor = new RequestParamProcessor()
                    .parameters(handlerInfo.requestParams()).process();
            api.requestParamFields(requestParamProcessor.fields());

            // @RequestBody
            BodyStructureProcessor requestBodyP = new BodyStructureProcessor().forRequestBodyOrNot(true)
                    .bodyType(handlerInfo.requestBodyResolvedType()).process().moreProcess(api);
            api.requestBodyStructure(requestBodyP.calcBodyStructure());

            // 返回类型
            BodyStructureProcessor responseBodyP = new BodyStructureProcessor().forRequestBodyOrNot(false)
                    .bodyType(handlerInfo.responseBodyResolvedType()).process().moreProcess(api);
            api.responseBodyStructure(responseBodyP.calcBodyStructure());

            // 作者
            AuthorProcessor authorProcessor = new AuthorProcessor().controller(controller).handler(handler).process();
            api.author(authorProcessor.author());

            // 源码位置
            CodeSourceProcessor processor = new CodeSourceProcessor().handler(handler).process();
            api.codeSource(processor.location());

            apis.add(api);
        });

        return apis;
    }

}