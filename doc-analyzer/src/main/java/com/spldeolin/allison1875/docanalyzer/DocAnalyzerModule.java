package com.spldeolin.allison1875.docanalyzer;

import java.util.Set;
import com.google.common.collect.Sets;
import com.spldeolin.allison1875.base.ancestor.Allison1875MainProcessor;
import com.spldeolin.allison1875.base.ancestor.Allison1875Module;
import com.spldeolin.allison1875.docanalyzer.handle.AccessDescriptionHandle;
import com.spldeolin.allison1875.docanalyzer.handle.AnalyzeCustomValidationHandle;
import com.spldeolin.allison1875.docanalyzer.handle.AnalyzeEnumConstantHandle;
import com.spldeolin.allison1875.docanalyzer.handle.MoreHandlerAnalysisHandle;
import com.spldeolin.allison1875.docanalyzer.handle.MoreJpdvAnalysisHandle;
import com.spldeolin.allison1875.docanalyzer.handle.ObtainConcernedResponseBodyHandle;
import com.spldeolin.allison1875.docanalyzer.handle.SpecificFieldDescriptionsHandle;
import com.spldeolin.allison1875.docanalyzer.processor.DocAnalyzer;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2020-12-06
 */
@Log4j2
public class DocAnalyzerModule extends Allison1875Module {

    @Override
    protected void configure() {
        bind(AnalyzeCustomValidationHandle.class).to(AnalyzeCustomValidationHandle.class);
        bind(AnalyzeEnumConstantHandle.class).to(AnalyzeEnumConstantHandle.class);
        bind(MoreJpdvAnalysisHandle.class).to(MoreJpdvAnalysisHandle.class);
        bind(ObtainConcernedResponseBodyHandle.class).to(ObtainConcernedResponseBodyHandle.class);
        bind(SpecificFieldDescriptionsHandle.class).to(SpecificFieldDescriptionsHandle.class);
        bind(AccessDescriptionHandle.class).to(AccessDescriptionHandle.class);
        bind(MoreHandlerAnalysisHandle.class).to(MoreHandlerAnalysisHandle.class);
    }

    @Override
    protected Class<? extends Allison1875MainProcessor> provideMainProcessorType() {
        return DocAnalyzer.class;
    }

    @Override
    protected Set<Class<?>> provideConfigTypes() {
        return Sets.newHashSet(DocAnalyzerConfig.class);
    }

}