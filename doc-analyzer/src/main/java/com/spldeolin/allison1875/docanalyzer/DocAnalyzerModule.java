package com.spldeolin.allison1875.docanalyzer;

import com.google.inject.Injector;
import com.spldeolin.allison1875.base.Allison1875;
import com.spldeolin.allison1875.base.ancestor.Allison1875MainProcessor;
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
public class DocAnalyzerModule extends Allison1875.Module {

    protected final DocAnalyzerConfig docAnalyzerConfig;

    public DocAnalyzerModule(DocAnalyzerConfig docAnalyzerConfig) {
        this.docAnalyzerConfig = super.ensureValid(docAnalyzerConfig);
    }

    @Override
    protected void configure() {
        bind(AnalyzeCustomValidationHandle.class).to(DefaultAnalyzeCustomValidationHandle.class);
        bind(AnalyzeEnumConstantHandle.class).to(DefaultAnalyzeEnumConstantHandle.class);
        bind(MoreJpdvAnalysisHandle.class).to(DefaultMoreJpdvAnalysisHandle.class);
        bind(ObtainConcernedResponseBodyHandle.class).to(DefaultObtainConcernedResponseBodyHandle.class);
        bind(SpecificFieldDescriptionsHandle.class).to(DefaultSpecificFieldDescriptionsHandle.class);
        bind(DocAnalyzerConfig.class).toInstance(docAnalyzerConfig);
    }

    @Override
    public Allison1875MainProcessor getMainProcessor(Injector injector) {
        return injector.getInstance(DocAnalyzer.class);
    }

}