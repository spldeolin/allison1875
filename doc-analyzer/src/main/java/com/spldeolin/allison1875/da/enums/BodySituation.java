package com.spldeolin.allison1875.da.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;

/**
 * 代表RequestBody或者ResponseBody的4种可能情况
 *
 * @author Deolin 2020-06-02
 */
@AllArgsConstructor
public enum BodySituation {

    /**
     * 代表没有body，即请求不需要参数或是没有返回值
     */
    NONE(1),

    /**
     * 代表body类型的数据结构比较特殊，即便解析出来也难以用二维表单的形式展示在页面上
     * 出现这种情况的原因往往是使用特殊的返回类型，如多维数组，或是使用了运行期才能确定的类型，如Map< String, Object >
     * 发生这种情况时，一般需要开发者来自己来说明如何交互
     * 可以考虑通过制定规范来阻止这种情况的发生
     */
    CHAOS(2),

    /**
     * 解析body类型发生任何异常
     * 出现这种情况的原因往往是源码变更后没有及时运行CompileSourceAndCopyDependencyTool，或是其他预想之外的原因
     * 发生这种情况时，一般可以运行CompileSourceAndCopyDependencyTool后重新解析，此后若依然存在这种情况，可以报告BUG
     */
    FAIL(3),

    /**
     * body结构整体上是个key-value结构
     */
    KEY_VALUE(4),

    /**
     * body结构整体上是个数组中的key-value结构
     */
    KEY_VALUE_ARRAY(5);

    private final Integer value;

    @JsonValue
    public Integer getValue() {
        return value;
    }
}