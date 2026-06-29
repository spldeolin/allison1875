package com.spldeolin.allison1875.formgenerator.dsl;

import java.util.regex.Pattern;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

/**
 * @author Deolin 2026-02-11
 */
@Value
@Builder(toBuilder = true)
@Jacksonized
public class OptionDef {

    private static final Pattern CAMEL_CASE_PATTERN = Pattern.compile("(?<=[a-z])(?=[A-Z])");

    private static final Pattern NON_ALNUM_PATTERN = Pattern.compile("[^a-zA-Z0-9]");

    private static final Pattern MULTI_UNDERSCORE_PATTERN = Pattern.compile("_+");

    private static final Pattern LEAD_TRAIL_UNDERSCORE = Pattern.compile("^_+|_+$");

    /**
     * 可选项唯一标示
     */
    String code;

    /**
     * 可选项标题
     */
    String title;

    /**
     * 转化为Java枚举项名
     */
    public String javaEnumConstantName() {
        String s = CAMEL_CASE_PATTERN.matcher(code).replaceAll("_");
        s = NON_ALNUM_PATTERN.matcher(s).replaceAll("_");
        s = MULTI_UNDERSCORE_PATTERN.matcher(s).replaceAll("_");
        s = LEAD_TRAIL_UNDERSCORE.matcher(s).replaceAll("");
        if (!s.isEmpty() && Character.isDigit(s.charAt(0))) {
            s = "E" + s;
        }
        return s.toUpperCase();
    }

}
