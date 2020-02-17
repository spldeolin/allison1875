package com.spldeolin.allison1875.da.view.markdown;

import freemarker.template.Configuration;

/**
 * @author Deolin 2020-02-17
 */
public class FreeMarkerPrinter {

    public static void print(SimpleMdOutputFtl ftl) {
        Configuration cfg = new Configuration(Configuration.getVersion());
        cfg.setClassForTemplateLoading(FreeMarkerPrinter.class, "");
        cfg.setDefaultEncoding("utf-8");

    }

}
