package com.spldeolin.allison1875.da.core.processor;

import java.util.Collection;
import com.google.common.collect.Lists;
import com.spldeolin.allison1875.da.core.definition.ApiDefinition;
import com.spldeolin.allison1875.da.core.definition.BodyFieldDefinition;
import com.spldeolin.allison1875.da.core.enums.BodyStructureEnum;
import com.spldeolin.allison1875.da.core.enums.FieldTypeEnum;
import com.spldeolin.allison1875.da.core.enums.NumberFormatTypeEnum;
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
public class ValueBodyProcessor extends BodyStructureProcessor {

    @Setter
    private FieldTypeEnum valueStructureJsonType;

    @Setter
    private NumberFormatTypeEnum valueStructureNumberFormat;

    @Override
    ValueBodyProcessor moreProcess(ApiDefinition api) {
        moreCheckStatus();

        Collection<BodyFieldDefinition> field = Lists.newArrayList(
                new BodyFieldDefinition().jsonType(valueStructureJsonType).numberFormat(valueStructureNumberFormat));
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
        if (valueStructureNumberFormat == null) {
            throw new IllegalStateException("valueStructureNumberFormat cannot be absent.");
        }
    }

}
