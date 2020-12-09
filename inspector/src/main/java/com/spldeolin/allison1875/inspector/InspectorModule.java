package com.spldeolin.allison1875.inspector;

import com.google.inject.Injector;
import com.spldeolin.allison1875.base.Allison1875;
import com.spldeolin.allison1875.base.ancestor.Allison1875MainProcessor;
import com.spldeolin.allison1875.inspector.processor.Inspector;

/**
 * @author Deolin 2020-12-07
 */
public class InspectorModule extends Allison1875.Module {

    protected final InspectorConfig inspectorConfig;

    public InspectorModule(InspectorConfig inspectorConfig) {
        this.inspectorConfig = inspectorConfig;
    }

    @Override
    protected void configure() {
        bind(InspectorConfig.class).toInstance(inspectorConfig);
    }

    @Override
    public Allison1875MainProcessor getMainProcessor(Injector injector) {
        return injector.getInstance(Inspector.class);
    }

}