package com.spldeolin.allison1875.inspector;

import java.util.Collection;
import com.google.common.collect.Lists;
import com.google.inject.Injector;
import com.google.inject.TypeLiteral;
import com.spldeolin.allison1875.base.ancestor.Allison1875MainProcessor;
import com.spldeolin.allison1875.base.ancestor.Allison1875Module;
import com.spldeolin.allison1875.inspector.processor.Inspector;
import com.spldeolin.allison1875.inspector.statute.Statute;

/**
 * @author Deolin 2020-12-07
 */
public class InspectorModule extends Allison1875Module {

    {
        addConfigType(InspectorConfig.class);
    }

    @Override
    protected void configure() {
        // multi bind
        bind(new TypeLiteral<Collection<Statute>>() {
        }).toInstance(Lists.newArrayList());
    }

    @Override
    public Allison1875MainProcessor getMainProcessor(Injector injector) {
        return injector.getInstance(Inspector.class);
    }

}