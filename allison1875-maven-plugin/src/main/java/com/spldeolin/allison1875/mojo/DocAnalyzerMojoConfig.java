package com.spldeolin.allison1875.mojo;

import com.spldeolin.allison1875.docanalyzer.DocAnalyzerConfig;
import com.spldeolin.allison1875.docanalyzer.DocAnalyzerModule;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

/**
 * @author Deolin 2024-07-29
 */
@Data
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DocAnalyzerMojoConfig extends DocAnalyzerConfig {

    String module = DocAnalyzerModule.class.getName();

}
