package com.spldeolin.allison1875.da.core.processor;

import java.util.Collection;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.google.common.collect.Lists;
import com.spldeolin.allison1875.da.core.definition.ApiDefinition;
import com.spldeolin.allison1875.da.core.strategy.ReturnStmtBaseResponseBodyTypeParser;
import lombok.AllArgsConstructor;

/**
 * 处理一个handler的核心主流程，处理结果可以交给视图转化器进行输出
 *
 * @author Deolin 2019-12-03
 */
@AllArgsConstructor
public class MainProcessor {

    public Collection<ApiDefinition> process() {
        final HandlerProcessor handlerP = new HandlerProcessor().handlerFilter(handler -> true)
                .responseBodyTypeParser(new ReturnStmtBaseResponseBodyTypeParser()).process();

        Collection<ApiDefinition> apis = Lists.newLinkedList();
        handlerP.handlerDefinitions().forEach(handlerDefinition -> {
            ClassOrInterfaceDeclaration controller = handlerDefinition.controller();
            MethodDeclaration handler = handlerDefinition.handler();
            ApiDefinition api = new ApiDefinition();

            // uri、请求方法
            RouteProcessor routeP = new RouteProcessor().controller(controller).handler(handler).process();
            api.method(routeP.methodTypes());
            api.uri(routeP.uris());

            // 描述
            DescriptionProcessor descriptionP = new DescriptionProcessor().handler(handler).process();
            api.description(descriptionP.description());

            // @PathVariable
            PathVariableProcessor pathVariableP = new PathVariableProcessor()
                    .parameters(handlerDefinition.pathVariables()).process();
            api.pathVariableFields(pathVariableP.fields());

            // @RequestParam
            RequestParamProcessor requestParamP = new RequestParamProcessor()
                    .parameters(handlerDefinition.requestParams()).process();
            api.requestParamFields(requestParamP.fields());

            // @RequestBody
            BodyStructureProcessor requestBodyP = new BodyStructureProcessor().forRequestBodyOrNot(true)
                    .bodyType(handlerDefinition.requestBodyResolvedType()).process().moreProcess(api);
            api.requestBodyStructure(requestBodyP.calcBodyStructure());

            // 返回类型
            BodyStructureProcessor responseBodyP = new BodyStructureProcessor().forRequestBodyOrNot(false)
                    .bodyType(handlerDefinition.responseBodyResolvedType()).process().moreProcess(api);
            api.responseBodyStructure(responseBodyP.calcBodyStructure());

            // 作者
            AuthorProcessor authorP = new AuthorProcessor().controller(controller).handler(handler).process();
            api.author(authorP.author());

            // 源码位置
            CodeSourceProcessor codeSourceP = new CodeSourceProcessor().handler(handler).process();
            api.codeSource(codeSourceP.location());

            apis.add(api);
        });
        return apis;
    }

}