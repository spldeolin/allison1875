package com.spldeolin.allison1875.extension.satisficing;

import com.spldeolin.allison1875.common.config.CommonConfig;
import com.spldeolin.allison1875.docanalyzer.DocAnalyzerConfig;
import com.spldeolin.allison1875.docanalyzer.service.EnumService;
import com.spldeolin.allison1875.docanalyzer.service.FieldService;
import com.spldeolin.allison1875.docanalyzer.service.ResponseBodyService;
import com.spldeolin.allison1875.extension.satisficing.docanalyzer.EnumServiceImpl2;
import com.spldeolin.allison1875.extension.satisficing.docanalyzer.FieldServiceImpl2;
import com.spldeolin.allison1875.extension.satisficing.docanalyzer.ResponseBodyServiceImpl2;

/**
 * @author Deolin 2024-06-15
 */
public class DocAnalyzerModule extends com.spldeolin.allison1875.docanalyzer.DocAnalyzerModule {

    public DocAnalyzerModule(CommonConfig commonConfig, DocAnalyzerConfig docAnalyzerConfig) {
        super(commonConfig, docAnalyzerConfig);
    }

    @Override
    protected void configure() {
        super.configure();
        bind(ResponseBodyService.class).toInstance(new ResponseBodyServiceImpl2());
        bind(FieldService.class).toInstance(new FieldServiceImpl2());
        bind(EnumService.class).toInstance(new EnumServiceImpl2());
    }

}
