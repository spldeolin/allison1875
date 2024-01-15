package com.spldeolin.allison1875.docanalyzer;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import com.fasterxml.jackson.module.jsonSchema.JsonSchemaGenerator;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Table;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.common.ancestor.Allison1875MainService;
import com.spldeolin.allison1875.common.ast.AstForest;
import com.spldeolin.allison1875.common.util.CollectionUtils;
import com.spldeolin.allison1875.docanalyzer.enums.OutputToEnum;
import com.spldeolin.allison1875.docanalyzer.javabean.BodyTypeAnalysisDto;
import com.spldeolin.allison1875.docanalyzer.javabean.ControllerFullDto;
import com.spldeolin.allison1875.docanalyzer.javabean.EndpointDto;
import com.spldeolin.allison1875.docanalyzer.javabean.HandlerFullDto;
import com.spldeolin.allison1875.docanalyzer.javabean.JsonPropertyDescriptionValueDto;
import com.spldeolin.allison1875.docanalyzer.javabean.RequestMappingFullDto;
import com.spldeolin.allison1875.docanalyzer.service.CopyEndpointService;
import com.spldeolin.allison1875.docanalyzer.service.JsgBuildService;
import com.spldeolin.allison1875.docanalyzer.service.MarkdownOutputService;
import com.spldeolin.allison1875.docanalyzer.service.RequestBodyService;
import com.spldeolin.allison1875.docanalyzer.service.RequestMappingService;
import com.spldeolin.allison1875.docanalyzer.service.ResponseBodyService;
import com.spldeolin.allison1875.docanalyzer.service.SimplyAnalyzeService;
import com.spldeolin.allison1875.docanalyzer.service.SpringMvcHandlerService;
import com.spldeolin.allison1875.docanalyzer.service.YApiSyncService;
import lombok.extern.log4j.Log4j2;

/**
 * doc-analyzer的主流程
 *
 * @author Deolin 2020-06-10
 */
@Singleton
@Log4j2
public class DocAnalyzer implements Allison1875MainService {

    @Inject
    private SpringMvcHandlerService listHandlersService;

    @Inject
    private RequestMappingService requestMappingService;

    @Inject
    private CopyEndpointService copyEndpointService;

    @Inject
    private SimplyAnalyzeService simplyAnalyzeService;

    @Inject
    private YApiSyncService yApiSyncService;

    @Inject
    private MarkdownOutputService markdownOutputService;

    @Inject
    private JsgBuildService jsgBuildService;

    @Inject
    private RequestBodyService requestBodyService;

    @Inject
    private ResponseBodyService responseBodyService;

    @Inject
    private DocAnalyzerConfig config;

    @Override
    public void process(AstForest astForest) {
        // 重新生成astForest（将解析范围扩大到 项目根目录 + 所有用户配置的依赖项目路径）
        Set<Path> dependencyProjectPaths = config.getDependencyProjectDirectories().stream().map(File::toPath)
                .collect(Collectors.toSet());
        astForest = new AstForest(astForest.getPrimaryClass(), true, dependencyProjectPaths,
                astForest.getAstFilterService());

        // 首次遍历并解析astForest，然后构建2个jsg对象，jsg对象为后续req与resp生成JsonSchema所需，构建完毕后重置astForest游标
        Table<String, String, JsonPropertyDescriptionValueDto> jpdvs = jsgBuildService.analyzeJpdvs(astForest);
        JsonSchemaGenerator jsg4req = jsgBuildService.analyzeAstAndBuildJsg(jpdvs, true);
        JsonSchemaGenerator jsg4resp = jsgBuildService.analyzeAstAndBuildJsg(jpdvs, false);
        astForest.reset();

        // 收集endpoint
        List<EndpointDto> endpoints = Lists.newArrayList();

        // 遍历controller、遍历handler
        List<HandlerFullDto> handlers = listHandlersService.listHandlers(astForest);
        if (CollectionUtils.isEmpty(handlers)) {
            log.warn("no Handler detected");
            return;
        }
        for (HandlerFullDto handler : handlers) {
            ControllerFullDto controller = handler.getController();
            EndpointDto endpoint = new EndpointDto();

            // 分析并保存handler的分类、代码简称、描述、是否过时、作者、源码位置 等基本信息
            simplyAnalyzeService.analyze(controller.getCoid(), handler, endpoint);

            try {
                // 分析Request Body
                BodyTypeAnalysisDto reqBodyAnalysis = requestBodyService.analyze(jsg4req, handler.getMd());
                endpoint.setRequestBodyDescribe(reqBodyAnalysis.getDescribe());
                endpoint.setRequestBodyJsonSchema(reqBodyAnalysis.getJsonSchema());

                // 分析Response Body
                BodyTypeAnalysisDto respBodyAnalysis = responseBodyService.analyze(jsg4resp, controller.getCoid(),
                        handler.getMd());
                endpoint.setResponseBodyDescribe(respBodyAnalysis.getDescribe());
                endpoint.setResponseBodyJsonSchema(respBodyAnalysis.getJsonSchema());

                // 处理controller级与handler级的@RequestMapping
                RequestMappingFullDto requestMappingFullDto = requestMappingService.analyze(controller.getReflection(),
                        handler.getReflection(), config.getGlobalUrlPrefix());

                // 如果handler能通过多种url+Http动词请求的话，分裂成多个Endpoint
                List<EndpointDto> copies = copyEndpointService.copy(endpoint, requestMappingFullDto);

                endpoints.addAll(copies);
            } catch (Exception e) {
                log.info("description={} author={} sourceCode={}", Joiner.on(" ").join(endpoint.getDescriptionLines()),
                        endpoint.getSourceCode(), endpoint.getAuthor(), e);
            }
        }

        if (config.getOutputTo() == OutputToEnum.YAPI) {
            try {
                yApiSyncService.outputToYApi(endpoints);
            } catch (Exception e) {
                log.error("fail to output to YApi", e);
            }
        }

        if (config.getOutputTo() == OutputToEnum.LOCAL_MARKDOWN) {
            try {
                markdownOutputService.outputToMarkdown(endpoints);
            } catch (Exception e) {
                log.error("fail to output to Markdown", e);
            }
        }

        log.info(endpoints.size());
    }

}
