package com.spldeolin.allison1875.da.deprecated.core.processor;

import com.spldeolin.allison1875.da.deprecated.core.definition.ApiDefinition;
import com.spldeolin.allison1875.da.deprecated.core.enums.BodyStructureEnum;
import lombok.experimental.Accessors;

/**
 * 没有body
 *
 * @author Deolin 2020-02-20
 */
@Accessors(fluent = true)
class VoidBodyProcessor extends BodyStructureProcessor {

    @Override
    VoidBodyProcessor moreProcess(ApiDefinition api) {
        return this;
    }

    @Override
    BodyStructureEnum calcBodyStructure() {
        return BodyStructureEnum.none;
    }

}
