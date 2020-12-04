package com.spldeolin.allison1875.docanalyzer.processor;

import java.util.Collection;
import java.util.Set;
import javax.validation.ConstraintViolation;
import com.fasterxml.jackson.module.jsonSchema.JsonSchemaGenerator;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.spldeolin.allison1875.base.ancestor.Allison1875MainProcessor;
import com.spldeolin.allison1875.base.ast.AstForest;
import com.spldeolin.allison1875.base.ast.AstForestContext;
import com.spldeolin.allison1875.base.util.ValidateUtils;
import com.spldeolin.allison1875.docanalyzer.DocAnalyzerConfig;
import com.spldeolin.allison1875.docanalyzer.dto.ControllerFullDto;
import com.spldeolin.allison1875.docanalyzer.dto.EndpointDto;
import com.spldeolin.allison1875.docanalyzer.dto.HandlerFullDto;
import com.spldeolin.allison1875.docanalyzer.dto.RequestMappingFullDto;
import com.spldeolin.allison1875.docanalyzer.handle.AnalyzeCustomValidationHandle;
import com.spldeolin.allison1875.docanalyzer.handle.AnalyzeEnumConstantHandle;
import com.spldeolin.allison1875.docanalyzer.handle.MoreJpdvAnalysisHandle;
import com.spldeolin.allison1875.docanalyzer.handle.ObtainConcernedResponseBodyHandle;
import com.spldeolin.allison1875.docanalyzer.handle.SpecificFieldDescriptionsHandle;
import com.spldeolin.allison1875.docanalyzer.handle.impl.DefaultAnalyzeCustomValidationHandle;
import com.spldeolin.allison1875.docanalyzer.handle.impl.DefaultAnalyzeEnumConstantHandle;
import com.spldeolin.allison1875.docanalyzer.handle.impl.DefaultMoreJpdvAnalysisHandle;
import com.spldeolin.allison1875.docanalyzer.handle.impl.DefaultObtainConcernedResponseBodyHandle;
import com.spldeolin.allison1875.docanalyzer.handle.impl.DefaultSpecificFieldDescriptionsHandle;
import lombok.extern.log4j.Log4j2;

/**
 * doc-analyzer的主流程
 *
 * @author Deolin 2020-06-10
 */
@Log4j2
public class DocAnalyzer implements Allison1875MainProcessor<DocAnalyzerConfig, DocAnalyzer> {

    protected ObtainConcernedResponseBodyHandle obtainConcernedResponseBodyHandle =
            new DefaultObtainConcernedResponseBodyHandle();

    protected AnalyzeCustomValidationHandle analyzeCustomValidationHandle = new DefaultAnalyzeCustomValidationHandle();

    protected SpecificFieldDescriptionsHandle specificFieldDescriptionsHandle =
            new DefaultSpecificFieldDescriptionsHandle();

    protected AnalyzeEnumConstantHandle analyzeEnumConstantHandle = new DefaultAnalyzeEnumConstantHandle();

    protected MoreJpdvAnalysisHandle moreJpdvAnalysisHandle = new DefaultMoreJpdvAnalysisHandle();

    ListHandlersProc listHandlersProc = new ListHandlersProc();

    RequestMappingProc requestMappingProcessor = new RequestMappingProc();

    CopyEndpointProc copyEndpointProc = new CopyEndpointProc();

    SimplyAnalyzeProc simplyAnalyzeProc = new SimplyAnalyzeProc();

    public static final ThreadLocal<DocAnalyzerConfig> CONFIG = ThreadLocal.withInitial(DocAnalyzerConfig::new);

    @Override
    public DocAnalyzer config(DocAnalyzerConfig config) {
        Set<ConstraintViolation<DocAnalyzerConfig>> violations = ValidateUtils.validate(config);
        if (violations.size() > 0) {
            log.warn("配置项校验未通过，请检查后重新运行");
            for (ConstraintViolation<DocAnalyzerConfig> violation : violations) {
                log.warn(violation.getRootBeanClass().getSimpleName() + "." + violation.getPropertyPath() + " "
                        + violation.getMessage());
            }
            System.exit(-9);
        }
        CONFIG.set(config);
        return this;
    }

    @Override
    public void process(AstForest astForest) {
        // 重新生成astForest（将解析范围扩大到所有用户配置的项目路径）
        astForest = new AstForest(astForest.getAnyClassFromHost(), CONFIG.get().getDependencyProjectPaths());
        AstForestContext.setCurrent(astForest);

        // 首次遍历并解析astForest，然后构建jsg对象，jsg对象为后续生成JsonSchema所需，构建完毕后重置astForest游标
        JsgBuildProc jsgBuildProc = new JsgBuildProc(analyzeCustomValidationHandle,
                specificFieldDescriptionsHandle.provideSpecificFieldDescriptions(), analyzeEnumConstantHandle,
                moreJpdvAnalysisHandle);
        JsonSchemaGenerator jsg = jsgBuildProc.analyzeAstAndBuildJsg(astForest);

        // 收集endpoint
        Collection<EndpointDto> endpoints = Lists.newArrayList();

        // 遍历controller、遍历handler
        Collection<HandlerFullDto> handlers = listHandlersProc.process(astForest);
        for (HandlerFullDto handler : handlers) {
            ControllerFullDto controller = handler.getController();
            EndpointDto endpoint = new EndpointDto();

            // 分析并保存handler的分类、代码简称、描述、是否过时、作者、源码位置 等基本信息
            simplyAnalyzeProc.process(controller.getCoid(), handler, endpoint);

            try {
                // 分析Request Body
                RequestBodyProc requestBodyProc = new RequestBodyProc();
                endpoint.setRequestBodyJsonSchema(requestBodyProc.analyze(jsg, handler.getMd()));

                // 分析Response Body
                ResponseBodyProc responseBodyProc = new ResponseBodyProc(obtainConcernedResponseBodyHandle);
                endpoint.setResponseBodyJsonSchema(
                        responseBodyProc.analyze(jsg, controller.getCoid(), handler.getMd()));

                // 处理controller级与handler级的@RequestMapping
                RequestMappingFullDto requestMappingFullDto = requestMappingProcessor
                        .analyze(controller.getReflection(), handler.getReflection());

                // 如果handler能通过多种url+Http动词请求的话，分裂成多个Endpoint
                Collection<EndpointDto> copies = copyEndpointProc.process(endpoint, requestMappingFullDto);

                endpoints.addAll(copies);
            } catch (Exception e) {
                log.info("description={} author={} sourceCode={}", Joiner.on(" ").join(endpoint.getDescriptionLines()),
                        endpoint.getSourceCode(), endpoint.getAuthor(), e);
            }
        }

        // 同步到YApi
        new YApiSyncProc(moreJpdvAnalysisHandle, endpoints).process();
        log.info(endpoints.size());
    }

}
