package com.spldeolin.allison1875.inspector;

import java.util.Collection;
import java.util.List;
import com.google.common.collect.Lists;
import com.google.inject.TypeLiteral;
import com.spldeolin.allison1875.common.ancestor.Allison1875MainService;
import com.spldeolin.allison1875.common.ancestor.Allison1875Module;
import com.spldeolin.allison1875.common.javabean.InvalidDto;
import com.spldeolin.allison1875.inspector.statute.Statute;
import lombok.ToString;

/**
 * @author Deolin 2020-12-07
 */
@ToString
public class InspectorModule extends Allison1875Module {

    private final InspectorConfig inspectorConfig;

    public InspectorModule(InspectorConfig inspectorConfig) {
        this.inspectorConfig = inspectorConfig;
    }

    @Override
    public Class<? extends Allison1875MainService> declareMainService() {
        return Inspector.class;
    }

    @Override
    public List<InvalidDto> validConfigs() {
        return inspectorConfig.invalidSelf();
    }

    @Override
    protected void configure() {
        // bind config
        bind(InspectorConfig.class).toInstance(inspectorConfig);
        // bind statutes
        bind(new TypeLiteral<Collection<Statute>>() {
        }).toInstance(Lists.newArrayList());
    }

}