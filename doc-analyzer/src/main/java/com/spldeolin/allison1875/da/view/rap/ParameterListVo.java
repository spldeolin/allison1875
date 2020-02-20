package com.spldeolin.allison1875.da.view.rap;

import java.util.Collection;
import java.util.List;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.spldeolin.allison1875.da.core.domain.BodyFieldDomain;
import com.spldeolin.allison1875.da.core.domain.ValidatorDomain;
import com.spldeolin.allison1875.da.core.enums.StringFormatTypeEnum;
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

    public static ParameterListVo build(BodyFieldDomain fieldDto) {
        ParameterListVo result = new ParameterListVo();
        result.setId(-2333L);
        result.setIdentifier(fieldDto.fieldName());
        result.setName(fieldDto.description());
        result.setValidator("");
        result.setDataType(RapJsonType.convert(fieldDto.jsonType()).getName());
        result.setParameterList(Lists.newArrayList());

        StringBuilder remark = new StringBuilder(64);
        if (fieldDto.stringFormat() != null && !StringFormatTypeEnum.normal.getValue()
                .equals(fieldDto.stringFormat())) {
            remark.append("格式：");
            remark.append(fieldDto.stringFormat());
            remark.append("　");
        }
        if (fieldDto.numberFormat() != null) {
            remark.append("格式：");
            remark.append(fieldDto.numberFormat().getValue());
            remark.append("　");
        }

        if (Boolean.FALSE.equals(fieldDto.nullable())) {
            remark.append("必填");
            remark.append("　");
        }

        Collection<ValidatorDomain> validators = fieldDto.validators();
        if (validators != null && validators.size() > 0) {
            Collection<String> parts = Lists.newLinkedList();
            validators.forEach(validator -> {
                parts.add(validator.validatorType().getDescription());
                parts.add(validator.note());
            });
            Joiner.on("　").skipNulls().appendTo(remark, parts);
        }

        result.setRemark(remark.toString());
        return result;
    }

}