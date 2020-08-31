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

    METHOD_LINE("0013", new MethodLine()),

    NUMBER_MAGIC_VALUE("0019", new NumberMagicValue());

    private final String no;

    private final Statute statute;

}
