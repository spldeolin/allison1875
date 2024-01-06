package com.spldeolin.allison1875.docanalyzer;

import com.spldeolin.allison1875.common.ancestor.Allison1875MainService;
import com.spldeolin.allison1875.common.ancestor.Allison1875Module;
import com.spldeolin.allison1875.common.util.ValidUtils;
import lombok.ToString;

/**
 * @author Deolin 2020-12-06
 */
@ToString
public class DocAnalyzerModule extends Allison1875Module {

    private final DocAnalyzerConfig docAnalyzerConfig;

    public DocAnalyzerModule(DocAnalyzerConfig docAnalyzerConfig) {
        this.docAnalyzerConfig = docAnalyzerConfig;
    }

    @Override
    protected void configure() {
        ValidUtils.ensureValid(docAnalyzerConfig);
        bind(DocAnalyzerConfig.class).toInstance(docAnalyzerConfig);
    }

    @Override
    public Class<? extends Allison1875MainService> declareMainService() {
        return DocAnalyzer.class;
    }

}