package com.spldeolin.allison1875.da.deprecated.core.processor;

import java.util.Collection;
import com.google.common.collect.Lists;
import com.spldeolin.allison1875.da.deprecated.core.definition.ApiDefinition;
import com.spldeolin.allison1875.da.deprecated.core.definition.BodyFieldDefinition;
import com.spldeolin.allison1875.da.deprecated.core.enums.BodyStructureEnum;
import com.spldeolin.allison1875.da.deprecated.core.enums.FieldTypeEnum;
import com.spldeolin.allison1875.da.deprecated.core.enums.NumberFormatTypeEnum;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * 单纯的值类型结构
 *
 * 整个body是boolean、字符串、数字中的一个。e.g.: @ResponseBody public BigDecaimal ....
 *
 * @author Deolin 2020-02-20
 */
@Accessors(fluent = true)
class ValueBodyProcessor extends BodyStructureProcessor {

    @Setter
    private FieldTypeEnum valueStructureJsonType;

    @Setter
    private NumberFormatTypeEnum valueStructureNumberFormat;

    @Override
    ValueBodyProcessor moreProcess(ApiDefinition api) {
        moreCheckStatus();

        BodyFieldDefinition bodyField = new BodyFieldDefinition().setJsonType(valueStructureJsonType);
        if (valueStructureJsonType.isNotNumberLike()) {
            bodyField.setNumberFormat(valueStructureNumberFormat);
        }
        Collection<BodyFieldDefinition> field = Lists.newArrayList(bodyField);
        if (super.forRequestBodyOrNot) {
            api.requestBodyFields(field);
        } else {
            api.responseBodyFields(field);
        }
        return this;
    }

    @Override
    BodyStructureEnum calcBodyStructure() {
        if (super.inArray) {
            return BodyStructureEnum.valueArray;
        } else {
            return BodyStructureEnum.va1ue;
        }
    }

    private void moreCheckStatus() {
        if (valueStructureJsonType == null) {
            throw new IllegalStateException("valueStructureJsonType cannot be absent.");
        }
    }

}
