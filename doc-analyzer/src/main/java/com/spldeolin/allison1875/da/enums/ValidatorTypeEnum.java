package com.spldeolin.allison1875.da.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Deolin 2019-12-03
 */
@AllArgsConstructor
@Getter
public enum ValidatorTypeEnum {

    notBlank("必须有非空格字符"),

    notEmpty("必须有字符"),

    maxSize("最大长度/容量："),

    minSize("最小长度/容量："),

    maxInteger("最大值："),

    minInteger("最小值："),

    maxFloat("最大值："),

    minFloat("最小值："),

    future("必须是未来的时间"),

    past("必须是过去的时间"),

    maxIntegralDigits("最大整数位数："),

    maxFractionalDigits("最小整数位数："),

    positive("必须是正数"),

    enumValue("可选枚举值："),

    regex("正则表达式：");

    private final String value;

}
