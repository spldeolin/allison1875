package com.spldeolin.allison1875.da.core.processor;

import com.fasterxml.jackson.module.jsonSchema.types.ObjectSchema;
import com.spldeolin.allison1875.da.core.domain.ApiDomain;
import com.spldeolin.allison1875.da.core.enums.BodyStructureEnum;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * kv型数据结构
 *
 * e.g.: @ResponseBody public UserVo ....
 *
 * @author Deolin 2020-02-20
 */
@Accessors(fluent = true)
public class KeyValueBodyProcessor extends BodyStructureProcessor {

    @Setter
    private ObjectSchema objectSchema;

    @Override
    KeyValueBodyProcessor moreProcess(ApiDomain api) {
        new BodyFieldProcessor(super.forRequestBodyOrNot).process(objectSchema, api);
        return this;
    }

    @Override
    BodyStructureEnum calcBodyStructure() {
        if (super.inArray) {
            return BodyStructureEnum.keyValueArray;
        } else if (super.inPage) {
            return BodyStructureEnum.keyValuePage;
        } else {
            return BodyStructureEnum.keyValue;
        }
    }

}
