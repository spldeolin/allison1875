package com.spldeolin.allison1875.da.approved.markdown;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import com.spldeolin.allison1875.base.exception.FreeMarkerPrintExcpetion;
import com.spldeolin.allison1875.da.approved.DocAnalyzerConfig;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

/**
 * @author Deolin 2020-02-17
 */
public class FreeMarkerPrinter {

    public static void print(SimpleMdOutputVo ftl, String fileName) throws FreeMarkerPrintExcpetion {
        Configuration cfg = new Configuration(Configuration.VERSION_2_3_30);
        cfg.setClassForTemplateLoading(FreeMarkerPrinter.class, "/");
        cfg.setDefaultEncoding("utf-8");

        fileName = fileName + (fileName.endsWith(".md") ? "" : ".md");
        fileName = fileName.replace(File.separator, ", ");
        File outFile = DocAnalyzerConfig.CONFIG.getDocOutputDirectoryPath().resolve(fileName).toFile();

        try (Writer out = new BufferedWriter(new FileWriter(outFile))) {
            Template template = cfg.getTemplate("simple-md-output.ftl");
            template.process(ftl, out);
        } catch (IOException | TemplateException e) {
            throw new FreeMarkerPrintExcpetion(e);
        }
    }

}
