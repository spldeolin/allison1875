package com.spldeolin.allison1875.inspector;

import com.google.inject.Injector;
import com.spldeolin.allison1875.base.Allison1875Guice;
import com.spldeolin.allison1875.base.ancestor.Allison1875MainProcessor;
import com.spldeolin.allison1875.inspector.processor.Inspector;

/**
 * @author Deolin 2020-12-07
 */
public class InspectorModule extends Allison1875Guice.Module {

    private final InspectorConfig inspectorConfig;

    public InspectorModule(InspectorConfig inspectorConfig) {
        this.inspectorConfig = super.ensureValid(inspectorConfig);
    }

    @Override
    protected void configure() {
        bind(InspectorConfig.class).toInstance(inspectorConfig);
        super.configure();
    }

    @Override
    public Allison1875MainProcessor<?, ?> getMainProcessor(Injector injector) {
        return injector.getInstance(Inspector.class);
    }

}