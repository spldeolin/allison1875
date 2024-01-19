package com.spldeolin.allison1875.docanalyzer;

import java.util.List;
import com.spldeolin.allison1875.common.ancestor.Allison1875MainService;
import com.spldeolin.allison1875.common.ancestor.Allison1875Module;
import com.spldeolin.allison1875.common.javabean.InvalidDto;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Deolin 2020-12-06
 */
@Slf4j
public class DocAnalyzerModule extends Allison1875Module {

    private final DocAnalyzerConfig docAnalyzerConfig;

    public DocAnalyzerModule(DocAnalyzerConfig docAnalyzerConfig) {
        this.docAnalyzerConfig = docAnalyzerConfig;
    }

    @Override
    public Class<? extends Allison1875MainService> declareMainService() {
        return DocAnalyzer.class;
    }

    @Override
    public List<InvalidDto> validConfigs() {
        return docAnalyzerConfig.invalidSelf();
    }

    @Override
    protected void configure() {
        bind(DocAnalyzerConfig.class).toInstance(docAnalyzerConfig);
    }

}