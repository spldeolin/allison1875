package com.spldeolin.allison1875.da.view.markdown;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Paths;
import com.google.common.collect.Lists;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2020-02-17
 */
@Log4j2
public class FreeMarkerPrinter {

    public static void print(SimpleMdOutputVo ftl, String fileName) {
        Configuration cfg = new Configuration(Configuration.getVersion());
        cfg.setClassForTemplateLoading(FreeMarkerPrinter.class, "/");
        cfg.setDefaultEncoding("utf-8");

        File outFile = Paths.get("/Users/deolin/Documents/docs")
                .resolve(fileName + (fileName.endsWith(".md") ? "" : ".md")).toFile();

        try (Writer out = new FileWriter(outFile)) {
            Template template = cfg.getTemplate("simple-md-output.ftl");
            template.process(ftl, out);
        } catch (IOException | TemplateException e) {
            log.error("FreeMarkerPrinter print failed.", e);
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        SimpleMdOutputVo vo = new SimpleMdOutputVo();
        vo.setUri("/user/create");
        vo.setDescription("神动色飞测试");
        vo.setIsRequestBodyNone(false);
        vo.setIsRequestBodyChaos(false);
        vo.setIsResponseBodyNone(false);
        vo.setIsResponseBodyChaos(true);
        vo.setLocation("java.lang.String");

        RequestBodyFieldVo reqVo1 = new RequestBodyFieldVo();
        reqVo1.setLinkName("aa.a");
        reqVo1.setDescription("阿斯顿发地方");
        reqVo1.setJsonType("aadf");
        reqVo1.setValidators("阿斯顿发生地方顶顶顶顶");
        RequestBodyFieldVo reqVo2 = new RequestBodyFieldVo();
        reqVo2.setLinkName("bb.bbb.bbb.bbb[0]");
        reqVo2.setDescription("a实得分数的阿斯顿");
        reqVo2.setJsonType("jasd");
        reqVo2.setValidators("斯蒂芬看到福建师范");
        vo.setRequestBodyFields(Lists.newArrayList(reqVo1, reqVo2));

//        ResponseBodyFieldVo respVo = new ResponseBodyFieldVo();
//        respVo.setLinkName("fff.ss");
//        respVo.setDescription("阿斯顿发生地方");
//        respVo.setJsonType("string");
//        vo.setResponseBodyFields(Lists.newArrayList(respVo));


        print(vo, "ddd");
    }

}
