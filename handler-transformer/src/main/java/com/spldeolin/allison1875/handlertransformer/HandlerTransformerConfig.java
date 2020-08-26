package com.spldeolin.allison1875.handlertransformer;

import java.util.Collection;
import com.spldeolin.allison1875.base.util.YamlUtils;
import lombok.Data;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

/**
 * Allison1875[handler-transformer]的配置
 *
 * @author Deolin 2020-08-25
 */
@Data
@Log4j2
public class HandlerTransformerConfig {

    @Getter
    private static final HandlerTransformerConfig instance = YamlUtils
            .toObject("handler-transformer-config.yml", HandlerTransformerConfig.class);

    private String dtoPackage;

    private String reqDtoPackage;

    private String respDtoPackage;

    private String servicePackage;

    private String serviceImplPackage;

    private Collection<String> handlerAnnotations;

    private String result;

    private String resultVoid;

    private String returnWrappedResult;

    private String returnWrappedResultVoid;

    private Collection<String> controllerImports;

    private HandlerTransformerConfig() {
    }

}