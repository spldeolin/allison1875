package com.spldeolin.allison1875.docanalyzer;

import java.util.Set;
import com.google.common.collect.Sets;
import com.spldeolin.allison1875.base.ancestor.Allison1875MainProcessor;
import com.spldeolin.allison1875.base.ancestor.Allison1875Module;
import com.spldeolin.allison1875.docanalyzer.handle.AccessDescriptionHandle;
import com.spldeolin.allison1875.docanalyzer.handle.AnalyzeCustomValidationHandle;
import com.spldeolin.allison1875.docanalyzer.handle.AnalyzeEnumConstantHandle;
import com.spldeolin.allison1875.docanalyzer.handle.DefaultMoreHandlerAnalysisHandle;
import com.spldeolin.allison1875.docanalyzer.handle.MoreHandlerAnalysisHandle;
import com.spldeolin.allison1875.docanalyzer.handle.MoreJpdvAnalysisHandle;
import com.spldeolin.allison1875.docanalyzer.handle.ObtainConcernedResponseBodyHandle;
import com.spldeolin.allison1875.docanalyzer.handle.SpecificFieldDescriptionsHandle;
import com.spldeolin.allison1875.docanalyzer.handle.impl.DefaultAnalyzeCustomValidationHandle;
import com.spldeolin.allison1875.docanalyzer.handle.impl.DefaultAnalyzeEnumConstantHandle;
import com.spldeolin.allison1875.docanalyzer.handle.impl.DefaultMoreJpdvAnalysisHandle;
import com.spldeolin.allison1875.docanalyzer.handle.impl.DefaultObtainConcernedResponseBodyHandle;
import com.spldeolin.allison1875.docanalyzer.handle.impl.DefaultSpecificFieldDescriptionsHandle;
import com.spldeolin.allison1875.docanalyzer.handleimpl.DefaultAccessDescriptionHandle;
import com.spldeolin.allison1875.docanalyzer.processor.DocAnalyzer;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2020-12-06
 */
@Log4j2
public class DocAnalyzerModule extends Allison1875Module {

    @Override
    protected void configure() {
        bind(AnalyzeCustomValidationHandle.class).to(DefaultAnalyzeCustomValidationHandle.class);
        bind(AnalyzeEnumConstantHandle.class).to(DefaultAnalyzeEnumConstantHandle.class);
        bind(MoreJpdvAnalysisHandle.class).to(DefaultMoreJpdvAnalysisHandle.class);
        bind(ObtainConcernedResponseBodyHandle.class).to(DefaultObtainConcernedResponseBodyHandle.class);
        bind(SpecificFieldDescriptionsHandle.class).to(DefaultSpecificFieldDescriptionsHandle.class);
        bind(AccessDescriptionHandle.class).to(DefaultAccessDescriptionHandle.class);
        bind(MoreHandlerAnalysisHandle.class).to(DefaultMoreHandlerAnalysisHandle.class);
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