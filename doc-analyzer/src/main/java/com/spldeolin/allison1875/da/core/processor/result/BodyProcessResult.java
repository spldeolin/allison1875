package com.spldeolin.allison1875.da.core.processor.result;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * ResponseBody的数据结构
 * 1、不返回
 * 2、值类型结构
 * 3、kv型结构
 * 4、复杂结构
 *
 * @author Deolin 2020-01-03
 */
@Data
@Accessors(fluent = true)
public abstract class BodyProcessResult {

    private boolean inArray = false;

    private boolean inPage = false;

    public boolean isChaosStructure() {
        return getClass() == ChaosStructureBodyProcessResult.class;
    }

    public ChaosStructureBodyProcessResult asChaosStructure() {
        return (ChaosStructureBodyProcessResult) this;
    }

    public boolean isKeyValueStructure() {
        return getClass() == KeyValueStructureBodyProcessResult.class;
    }

    public KeyValueStructureBodyProcessResult asKeyValueStructure() {
        return (KeyValueStructureBodyProcessResult) this;
    }

    public boolean isValueStructure() {
        return getClass() == ValueStructureBodyProcessResult.class;
    }

    public ValueStructureBodyProcessResult asValueStructure() {
        return (ValueStructureBodyProcessResult) this;
    }

    public boolean isVoidStructure() {
        return getClass() == VoidStructureBodyProcessResult.class;
    }

    public VoidStructureBodyProcessResult asVoidStructure() {
        return (VoidStructureBodyProcessResult) this;
    }

}
