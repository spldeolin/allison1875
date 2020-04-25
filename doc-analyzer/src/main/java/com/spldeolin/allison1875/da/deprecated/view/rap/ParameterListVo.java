package com.spldeolin.allison1875.da.deprecated.view.rap;

import java.util.Collection;
import java.util.List;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.spldeolin.allison1875.da.deprecated.core.definition.BodyFieldDefinition;
import com.spldeolin.allison1875.da.deprecated.core.definition.ValidatorDefinition;
import com.spldeolin.allison1875.da.deprecated.core.enums.StringFormatTypeEnum;
import lombok.Data;

/**
 * @author Deolin 2019-10-21
 */
@Data
public class ParameterListVo {

    private Long id;

    private String identifier;

    private String name;

    private String remark;

    private List<ParameterListVo> parameterList;

    /**
     * unknown
     */
    private String validator;

    /**
     * @see RapJsonType
     */
    private String dataType;

    public static ParameterListVo build(BodyFieldDefinition fieldDto) {
        ParameterListVo result = new ParameterListVo();
        result.setId(-2333L);
        result.setIdentifier(fieldDto.getFieldName());
        result.setName(fieldDto.getDescription());
        result.setValidator("");
        result.setDataType(RapJsonType.convert(fieldDto.getJsonType()).getName());
        result.setParameterList(Lists.newArrayList());

        StringBuilder remark = new StringBuilder(64);
        if (fieldDto.getStringFormat() != null && !StringFormatTypeEnum.normal.getValue()
                .equals(fieldDto.getStringFormat())) {
            remark.append("格式：");
            remark.append(fieldDto.getStringFormat());
            remark.append("　");
        }
        if (fieldDto.getNumberFormat() != null) {
            remark.append("格式：");
            remark.append(fieldDto.getNumberFormat().getValue());
            remark.append("　");
        }

        if (Boolean.FALSE.equals(fieldDto.getNullable())) {
            remark.append("必填");
            remark.append("　");
        }

        Collection<ValidatorDefinition> validators = fieldDto.getValidators();
        if (validators != null && validators.size() > 0) {
            Collection<String> parts = Lists.newLinkedList();
            validators.forEach(validator -> {
                parts.add(validator.getValidatorType().getDescription());
                parts.add(validator.getNote());
            });
            Joiner.on("　").skipNulls().appendTo(remark, parts);
        }

        result.setRemark(remark.toString());
        return result;
    }

}