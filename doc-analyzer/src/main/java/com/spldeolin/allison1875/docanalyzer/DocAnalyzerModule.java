package com.spldeolin.allison1875.docanalyzer;

import java.util.Set;
import javax.validation.ConstraintViolation;
import com.google.inject.Injector;
import com.spldeolin.allison1875.base.Allison1875Guice;
import com.spldeolin.allison1875.base.ancestor.Allison1875MainProcessor;
import com.spldeolin.allison1875.base.util.ValidateUtils;
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
import com.spldeolin.allison1875.docanalyzer.processor.DocAnalyzer;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2020-12-06
 */
@Log4j2
public class DocAnalyzerModule extends Allison1875Guice.Module {

    private final DocAnalyzerConfig config;

    public DocAnalyzerModule(DocAnalyzerConfig config) {
        Set<ConstraintViolation<DocAnalyzerConfig>> violations = ValidateUtils.validate(config);
        if (violations.size() > 0) {
            log.warn("配置项校验未通过，请检查后重新运行");
            for (ConstraintViolation<DocAnalyzerConfig> violation : violations) {
                log.warn(violation.getRootBeanClass().getSimpleName() + "." + violation.getPropertyPath() + " "
                        + violation.getMessage());
            }
            System.exit(-9);
        }
        this.config = config;
    }

    @Override
    protected void configure() {
        bind(AnalyzeCustomValidationHandle.class).to(DefaultAnalyzeCustomValidationHandle.class);
        bind(AnalyzeEnumConstantHandle.class).to(DefaultAnalyzeEnumConstantHandle.class);
        bind(MoreJpdvAnalysisHandle.class).to(DefaultMoreJpdvAnalysisHandle.class);
        bind(ObtainConcernedResponseBodyHandle.class).to(DefaultObtainConcernedResponseBodyHandle.class);
        bind(SpecificFieldDescriptionsHandle.class).to(DefaultSpecificFieldDescriptionsHandle.class);
        bind(DocAnalyzerConfig.class).toInstance(config);
        super.configure();
    }

    @Override
    public Allison1875MainProcessor<?, ?> getMainProcessor(Injector injector) {
        return injector.getInstance(DocAnalyzer.class);
    }

}