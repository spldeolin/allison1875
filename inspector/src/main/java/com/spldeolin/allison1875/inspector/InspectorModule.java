package com.spldeolin.allison1875.inspector;

import java.util.Collection;
import java.util.Set;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.inject.TypeLiteral;
import com.spldeolin.allison1875.base.ancestor.Allison1875MainProcessor;
import com.spldeolin.allison1875.base.ancestor.Allison1875Module;
import com.spldeolin.allison1875.inspector.processor.Inspector;
import com.spldeolin.allison1875.inspector.statute.Statute;

/**
 * @author Deolin 2020-12-07
 */
public class InspectorModule extends Allison1875Module {

    @Override
    protected void configure() {
        // multi bind
        bind(new TypeLiteral<Collection<Statute>>() {
        }).toInstance(Lists.newArrayList());
    }

    @Override
    protected Class<? extends Allison1875MainProcessor> provideMainProcessorType() {
        return Inspector.class;
    }

    @Override
    protected Set<Class<?>> provideConfigTypes() {
        return Sets.newHashSet(InspectorConfig.class);
    }

}