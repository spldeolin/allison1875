package com.spldeolin.allison1875.querytransformer;

import javax.validation.constraints.NotEmpty;
import lombok.Data;

/**
 * @author Deolin 2020-08-09
 */
@Data
public final class QueryTransformerConfig {

    /**
     * Mapper方法签名中Condition类的路径
     */
    @NotEmpty
    private String mapperConditionQualifier;

    /**
     * Mapper方法签名中Record类的路径
     */
    @NotEmpty
    private String mapperRecordQualifier;

    /**
     * 使用通配符的方式设置所有包名，通配符是<code>.-</code>
     *
     * <pre>
     * e.g.1:
     * input:
     *  com.company.orginization.project.-
     *
     * output:
     *  com.company.orginization.project.cond
     *  com.company.orginization.project.record
     *
     *
     * e.g.2:
     * input:
     *  com.company.orginization.project.-.module.sub
     *
     * output:
     *  com.company.orginization.project.cond.module.sub
     *  com.company.orginization.project.record.module.sub
     *
     * </pre>
     */
    public void batchSetAllPackagesByWildcard(String packageNameWithWildcard) {
        if (packageNameWithWildcard != null && packageNameWithWildcard.contains(".-")) {
            this.mapperConditionQualifier = packageNameWithWildcard.replace(".-", ".cond");
            this.mapperRecordQualifier = packageNameWithWildcard.replace(".-", ".record");
        }
    }

}