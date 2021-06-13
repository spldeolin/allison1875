package com.spldeolin.allison1875.docanalyzer.processor;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;
import com.fasterxml.jackson.module.jsonSchema.JsonSchemaGenerator;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.base.ancestor.Allison1875MainProcessor;
import com.spldeolin.allison1875.base.ast.AstForest;
import com.spldeolin.allison1875.docanalyzer.DocAnalyzerConfig;
import com.spldeolin.allison1875.docanalyzer.javabean.ControllerFullDto;
import com.spldeolin.allison1875.docanalyzer.javabean.EndpointDto;
import com.spldeolin.allison1875.docanalyzer.javabean.HandlerFullDto;
import com.spldeolin.allison1875.docanalyzer.javabean.RequestMappingFullDto;
import lombok.extern.log4j.Log4j2;

/**
 * doc-analyzer的主流程
 *
 * @author Deolin 2020-06-10
 */
@Singleton
@Log4j2
public class DocAnalyzer implements Allison1875MainProcessor {

    @Inject
    private ListHandlersProc listHandlersProc;

    @Inject
    private RequestMappingProc requestMappingProcessor;

    @Inject
    private CopyEndpointProc copyEndpointProc;

    @Inject
    private SimplyAnalyzeProc simplyAnalyzeProc;

    @Inject
    private YApiSyncProc yapiSyncProc;

    @Inject
    private JsgBuildProc jsgBuildProc;

    @Inject
    private RequestBodyProc requestBodyProc;

    @Inject
    private ResponseBodyProc responseBodyProc;

    @Inject
    private DocAnalyzerConfig config;

    @Override
    public void process(AstForest astForest) {
        // 重新生成astForest（将解析范围扩大到 项目根目录 + 所有用户配置的依赖项目路径）
        Set<Path> dependencyProjectPaths = config.getDependencyProjectPaths().stream().map(Paths::get)
                .collect(Collectors.toSet());
        astForest = new AstForest(astForest.getPrimaryClass(), true, dependencyProjectPaths);

        // 首次遍历并解析astForest，然后构建jsg对象，jsg对象为后续生成JsonSchema所需，构建完毕后重置astForest游标
        JsonSchemaGenerator jsg = jsgBuildProc.analyzeAstAndBuildJsg(astForest);

        // 收集endpoint
        Collection<EndpointDto> endpoints = Lists.newArrayList();

        // 遍历controller、遍历handler
        Collection<HandlerFullDto> handlers = listHandlersProc.process(astForest);
        if (handlers.size() == 0) {
            log.warn("no handler detect.");
            return;
        }
        for (HandlerFullDto handler : handlers) {
            ControllerFullDto controller = handler.getController();
            EndpointDto endpoint = new EndpointDto();

            // 分析并保存handler的分类、代码简称、描述、是否过时、作者、源码位置 等基本信息
            simplyAnalyzeProc.process(controller.getCoid(), handler, endpoint);

            try {
                // 分析Request Body
                endpoint.setRequestBodyJsonSchema(requestBodyProc.analyze(jsg, handler.getMd()));

                // 分析Response Body
                endpoint.setResponseBodyJsonSchema(
                        responseBodyProc.analyze(jsg, controller.getCoid(), handler.getMd()));

                // 处理controller级与handler级的@RequestMapping
                RequestMappingFullDto requestMappingFullDto = requestMappingProcessor
                        .analyze(controller.getReflection(), handler.getReflection(), config.getGlobalUrlPrefix());

                // 如果handler能通过多种url+Http动词请求的话，分裂成多个Endpoint
                Collection<EndpointDto> copies = copyEndpointProc.process(endpoint, requestMappingFullDto);

                endpoints.addAll(copies);
            } catch (Exception e) {
                log.info("description={} author={} sourceCode={}", Joiner.on(" ").join(endpoint.getDescriptionLines()),
                        endpoint.getSourceCode(), endpoint.getAuthor(), e);
            }
        }

        // 同步到YApi
        try {
            yapiSyncProc.process(endpoints);
        } catch (Exception e) {
            log.info("YApi sync failed.", e);
        }
        log.info(endpoints.size());
    }

}
