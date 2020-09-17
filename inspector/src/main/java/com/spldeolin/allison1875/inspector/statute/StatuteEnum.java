package com.spldeolin.allison1875.inspector.statute;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 所有规约注册在此处
 *
 * @author Deolin 2020-02-23
 */
@AllArgsConstructor
@Getter
public enum StatuteEnum {

    S_0013("[1]", "任何方法禁止超过200行", new MethodLine()),

    S_0019("[2]", "代码中禁止出现数字魔法值", new NumberMagicValue()),

    ;

    private final String no;

    private final String statuteDescription;

    private final Statute statute;

}
