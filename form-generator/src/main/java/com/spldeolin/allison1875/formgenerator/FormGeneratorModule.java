package com.spldeolin.allison1875.formgenerator;

import com.spldeolin.allison1875.base.ancestor.Allison1875MainProcessor;
import com.spldeolin.allison1875.base.ancestor.Allison1875Module;
import com.spldeolin.allison1875.base.util.ValidateUtils;
import com.spldeolin.allison1875.formgenerator.processor.FormGenerator;
import lombok.ToString;

/**
 * @author Deolin 2022-04-02
 */
@ToString
public class FormGeneratorModule extends Allison1875Module {

    private final FormGeneratorConfig formGeneratorConfig;

    public FormGeneratorModule(FormGeneratorConfig formGeneratorConfig) {
        this.formGeneratorConfig = formGeneratorConfig;
    }

    @Override
    protected void configure() {
        ValidateUtils.ensureValid(formGeneratorConfig);
        bind(FormGeneratorConfig.class).toInstance(formGeneratorConfig);
    }

    @Override
    public Class<? extends Allison1875MainProcessor> provideMainProcessorType() {
        return FormGenerator.class;
    }

}