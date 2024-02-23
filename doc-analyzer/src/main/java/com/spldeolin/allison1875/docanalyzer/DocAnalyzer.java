package com.spldeolin.allison1875.docanalyzer;

import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.RequestMethod;
import com.fasterxml.jackson.module.jsonSchema.JsonSchemaGenerator;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Table;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.common.ancestor.Allison1875MainService;
import com.spldeolin.allison1875.common.ast.AstForest;
import com.spldeolin.allison1875.common.util.CollectionUtils;
import com.spldeolin.allison1875.docanalyzer.enums.FlushToEnum;
import com.spldeolin.allison1875.docanalyzer.javabean.AnalyzeBodyRetval;
import com.spldeolin.allison1875.docanalyzer.javabean.AnalyzeMvcHandlerRetval;
import com.spldeolin.allison1875.docanalyzer.javabean.AnalyzeRequestMappingRetval;
import com.spldeolin.allison1875.docanalyzer.javabean.EndpointDto;
import com.spldeolin.allison1875.docanalyzer.javabean.JsonPropertyDescriptionValueDto;
import com.spldeolin.allison1875.docanalyzer.javabean.MvcControllerDto;
import com.spldeolin.allison1875.docanalyzer.javabean.MvcHandlerDto;
import com.spldeolin.allison1875.docanalyzer.service.FieldService;
import com.spldeolin.allison1875.docanalyzer.service.JsgBuilderService;
import com.spldeolin.allison1875.docanalyzer.service.MarkdownService;
import com.spldeolin.allison1875.docanalyzer.service.MvcHandlerAnalyzerService;
import com.spldeolin.allison1875.docanalyzer.service.MvcHandlerDetectorService;
import com.spldeolin.allison1875.docanalyzer.service.RequestBodyService;
import com.spldeolin.allison1875.docanalyzer.service.RequestMappingService;
import com.spldeolin.allison1875.docanalyzer.service.ResponseBodyService;
import com.spldeolin.allison1875.docanalyzer.service.YApiService;
import lombok.extern.slf4j.Slf4j;

/**
 * doc-analyzer的主流程
 *
 * @author Deolin 2020-06-10
 */
@Singleton
@Slf4j
public class DocAnalyzer implements Allison1875MainService {

    @Inject
    private MvcHandlerDetectorService mvcHandlerDetectorService;

    @Inject
    private RequestMappingService requestMappingService;

    @Inject
    private MvcHandlerAnalyzerService mvcHandlerAnalyzerService;

    @Inject
    private YApiService yapiService;

    @Inject
    private MarkdownService markdownService;

    @Inject
    private FieldService fieldService;

    @Inject
    private JsgBuilderService jsgBuilderService;

    @Inject
    private RequestBodyService requestBodyService;

    @Inject
    private ResponseBodyService responseBodyService;

    @Inject
    private DocAnalyzerConfig config;

    @Override
    public void process(AstForest astForest) {
        // 分析所有相关Java文件分析出jpdvs，然后构建2个jsg对象，jsg对象为后续req与resp生成JsonSchema所需
        Table<String, String, JsonPropertyDescriptionValueDto> jpdvs = fieldService.analyzeFieldVars(
                astForest.cloneWithResetting());
        JsonSchemaGenerator jsg4req = jsgBuilderService.buildJsgByJpdvs(jpdvs, true);
        JsonSchemaGenerator jsg4resp = jsgBuilderService.buildJsgByJpdvs(jpdvs, false);

        // 收集endpoint
        List<EndpointDto> endpoints = Lists.newArrayList();

        // 遍历controller、遍历handler
        List<MvcHandlerDto> mvcHandlers = mvcHandlerDetectorService.detectMvcHandler(astForest);
        if (CollectionUtils.isEmpty(mvcHandlers)) {
            log.info("no MVC Handlers detected");
            return;
        }
        for (MvcHandlerDto mvcHandler : mvcHandlers) {
            MvcControllerDto mvcController = mvcHandler.getMvcController();
            EndpointDto endpoint = new EndpointDto();

            // 分析并保存handler的分类、代码简称、描述、是否过时、作者、源码位置 等基本信息
            AnalyzeMvcHandlerRetval analyzeMvcHandlerRetval = mvcHandlerAnalyzerService.analyzeMvcHandler(
                    mvcController.getCoid(), mvcHandler);
            setTo(analyzeMvcHandlerRetval, endpoint);

            // 分析
            try {
                // 分析Request Body
                AnalyzeBodyRetval analyzeBodyRetval = requestBodyService.analyzeBody(jsg4req,
                        mvcHandler.getMethodDec());
                endpoint.setRequestBodyDescribe(analyzeBodyRetval.getDescribe());
                endpoint.setRequestBodyJsonSchema(analyzeBodyRetval.getJsonSchema());

                // 分析Response Body
                analyzeBodyRetval = responseBodyService.analyzeBody(jsg4resp, mvcController.getCoid(),
                        mvcHandler.getMethodDec());
                endpoint.setResponseBodyDescribe(analyzeBodyRetval.getDescribe());
                endpoint.setResponseBodyJsonSchema(analyzeBodyRetval.getJsonSchema());

                // 分析controller级与handler级的@RequestMapping
                AnalyzeRequestMappingRetval analyzeRequestMappingRetval = requestMappingService.analyzeRequestMapping(
                        mvcController.getReflection(), mvcHandler.getReflection(), config.getGlobalUrlPrefix());

                // 如果handler能通过多种url+Http动词请求的话，分裂成多个Endpoint
                List<EndpointDto> divides = setToAndDivide(analyzeRequestMappingRetval, endpoint);

                endpoints.addAll(divides);
            } catch (Exception e) {
                log.info("description={} author={} sourceCode={}", Joiner.on(" ").join(endpoint.getDescriptionLines()),
                        endpoint.getSourceCode(), endpoint.getAuthor(), e);
            }
        }

        // 保存到YAPI
        if (config.getFlushTo().contains(FlushToEnum.YAPI)) {
            try {
                yapiService.flushToYApi(endpoints);
            } catch (Exception e) {
                log.error("fail to flush to YApi", e);
            }
        }

        // 保存到本地Markdwon
        if (config.getFlushTo().contains(FlushToEnum.LOCAL_MARKDOWN)) {
            try {
                markdownService.flushToMarkdown(endpoints);
            } catch (Exception e) {
                log.error("fail to flush to Markdown", e);
            }
        }

        log.info("endpoints.size={}", endpoints.size());
    }

    private void setTo(AnalyzeMvcHandlerRetval analyzeMvcHandlerRetval, EndpointDto endpoint) {
        endpoint.setCat(analyzeMvcHandlerRetval.getCat());
        endpoint.setHandlerSimpleName(analyzeMvcHandlerRetval.getHandlerSimpleName());
        endpoint.setDescriptionLines(analyzeMvcHandlerRetval.getDescriptionLines());
        endpoint.setIsDeprecated(analyzeMvcHandlerRetval.getIsDeprecated());
        endpoint.setAuthor(analyzeMvcHandlerRetval.getAuthor());
        endpoint.setSourceCode(analyzeMvcHandlerRetval.getSourceCode());
        endpoint.setMore(analyzeMvcHandlerRetval.getMoreInfo());
    }

    private List<EndpointDto> setToAndDivide(AnalyzeRequestMappingRetval analyzeRequestMappingRetval,
            EndpointDto endpoint) {
        List<EndpointDto> copies = Lists.newArrayList();
        for (String combinedUrl : analyzeRequestMappingRetval.getCombinedUrls()) {
            for (RequestMethod combinedVerb : analyzeRequestMappingRetval.getCombinedVerbs()) {
                EndpointDto copy = endpoint.copy();
                copy.setUrl(combinedUrl);
                copy.setHttpMethod(StringUtils.lowerCase(combinedVerb.toString()));
                copies.add(copy);
            }
        }
        return copies;
    }

}
