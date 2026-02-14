package com.spldeolin.allison1875.formgenerator;

import java.util.List;
import org.springframework.core.io.ClassPathResource;
import org.yaml.snakeyaml.Yaml;
import com.spldeolin.allison1875.formgenerator.dsl.FormDef;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Deolin 2026-02-11
 */
@Slf4j
public class DevEntry {

    /** YAML 根结构：forms 字段对应表单列表 */
    @Data
    public static class FormsWrapper {

        private List<FormDef> forms;

    }

    public static void main(String[] args) throws Exception {
        Yaml yaml = new Yaml();
        FormsWrapper wrapper = yaml.loadAs(new ClassPathResource("study-dsl.yml").getInputStream(), FormsWrapper.class);
        List<FormDef> formDefs = wrapper.getForms();
        System.out.println(formDefs);
    }

}
