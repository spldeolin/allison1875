package com.spldeolin.allison1875.docanalyzer;

import com.spldeolin.allison1875.base.ancestor.Allison1875MainProcessor;
import com.spldeolin.allison1875.base.ancestor.Allison1875Module;
import com.spldeolin.allison1875.base.util.ValidateUtils;
import com.spldeolin.allison1875.docanalyzer.processor.DocAnalyzer;
import lombok.ToString;

/**
 * @author Deolin 2020-12-06
 */
@ToString
public final class DocAnalyzerModule extends Allison1875Module {

    private final DocAnalyzerConfig docAnalyzerConfig;

    public DocAnalyzerModule(DocAnalyzerConfig docAnalyzerConfig) {
        this.docAnalyzerConfig = docAnalyzerConfig;
    }

    @Override
    protected void configure() {
        ValidateUtils.ensureValid(docAnalyzerConfig);
        bind(DocAnalyzerConfig.class).toInstance(docAnalyzerConfig);
    }

    @Override
    public Class<? extends Allison1875MainProcessor> provideMainProcessorType() {
        return DocAnalyzer.class;
    }

}