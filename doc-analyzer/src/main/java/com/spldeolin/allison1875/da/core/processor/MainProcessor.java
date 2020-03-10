package com.spldeolin.allison1875.da.core.processor;

import java.util.Collection;
import java.util.stream.Collectors;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.spldeolin.allison1875.da.core.definition.ApiDefinition;
import com.spldeolin.allison1875.da.core.enums.BodyStructureEnum;
import com.spldeolin.allison1875.da.core.strategy.ReturnStmtBaseResponseBodyTypeParser;
import lombok.extern.log4j.Log4j2;

/**
 * 处理一个handler的核心主流程，处理结果可以交给视图转化器进行输出
 *
 * @author Deolin 2019-12-03
 */
@Log4j2
public class MainProcessor {

    public Collection<ApiDefinition> process() {
        HandlerProcessor handlerP = new HandlerProcessor().handlerFilter(handler -> true)
                .responseBodyTypeParser(new ReturnStmtBaseResponseBodyTypeParser()).process();

        new JsonPropertyDescriptionGenerateProcessor().process();
        new MavenPackageProcessor().process();

        return handlerP.handlerDefinitions().stream().map(handlerDefinition -> {
            ClassOrInterfaceDeclaration controller = handlerDefinition.controller();
            MethodDeclaration handler = handlerDefinition.handler();
            String handlerSignature = handlerDefinition.shortestQualifiedSignature();
            ApiDefinition api = new ApiDefinition();

            // uri、请求方法
            try {
                RouteProcessor routeP = new RouteProcessor().controller(controller).handler(handler).process();
                api.method(routeP.methodTypes());
                api.uri(routeP.uris());
            } catch (Exception e) {
                log.warn("RouteProcessor processed failed for Handler [{}].", handlerSignature, e);
                api.method(Sets.newHashSet());
                api.uri(Sets.newHashSet());
            }

            // 描述
            try {
                DescriptionProcessor descriptionP = new DescriptionProcessor().handler(handler).process();
                api.description(descriptionP.description());
            } catch (Exception e) {
                log.warn("DescriptionProcessor processed failed for Handler [{}].", handlerSignature, e);
                api.description("");
            }

            // @PathVariable
            try {
                PathVariableProcessor pathVariableP = new PathVariableProcessor()
                        .parameters(handlerDefinition.pathVariables()).process();
                api.pathVariableFields(pathVariableP.fields());
            } catch (Exception e) {
                log.warn("PathVariableProcessor processed failed for Handler [{}].", handlerSignature, e);
                api.pathVariableFields(Lists.newArrayList());
            }

            // @RequestParam
            try {
                RequestParamProcessor requestParamP = new RequestParamProcessor()
                        .parameters(handlerDefinition.requestParams()).process();
                api.requestParamFields(requestParamP.fields());
            } catch (Exception e) {
                log.warn("RequestParamProcessor processed failed for Handler [{}].", handlerSignature, e);
                api.requestParamFields(Lists.newArrayList());
            }

            // @RequestBody
            try {
                BodyStructureProcessor requestBodyP = new BodyStructureProcessor().forRequestBodyOrNot(true)
                        .bodyType(handlerDefinition.requestBodyResolvedType()).process().moreProcess(api);
                api.requestBodyStructure(requestBodyP.calcBodyStructure());
            } catch (Exception e) {
                log.warn("BodyStructureProcessor(RequestBody) processed failed for Handler [{}].", handlerSignature, e);
                api.requestBodyStructure(BodyStructureEnum.none);
            }

            // 返回类型
            try {
                BodyStructureProcessor responseBodyP = new BodyStructureProcessor().forRequestBodyOrNot(false)
                        .bodyType(handlerDefinition.responseBodyResolvedType()).process().moreProcess(api);
                api.responseBodyStructure(responseBodyP.calcBodyStructure());
            } catch (Exception e) {
                log.warn("BodyStructureProcessor(ResponseBody) processed failed for Handler [{}].", handlerSignature,
                        e);
                api.responseBodyStructure(BodyStructureEnum.none);
            }

            // 作者
            try {
                AuthorProcessor authorP = new AuthorProcessor().handler(handler).process();
                api.author(authorP.author());
            } catch (Exception e) {
                log.warn("BodyStructureProcessor(ResponseBody) processed failed for Handler [{}].", handlerSignature,
                        e);
                api.author("");
            }

            // 源码位置
            try {
                SourceCodeProcessor sourceCodeP = new SourceCodeProcessor().handler(handler).process();
                api.sourceCode(sourceCodeP.location());
            } catch (Exception e) {
                log.warn("BodyStructureProcessor(ResponseBody) processed failed for Handler [{}].", handlerSignature,
                        e);
                api.sourceCode("");
            }

            return api;
        }).collect(Collectors.toList());
    }

}