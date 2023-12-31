package com.spldeolin.allison1875.querytransformer;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

/**
 * @author Deolin 2020-08-09
 */
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public final class QueryTransformerConfig {

    /**
     * Mapper方法签名中Condition类的包名
     */
    @NotEmpty String mapperConditionPackage;

    /**
     * Mapper方法签名中Record类的包名
     */
    @NotEmpty String mapperRecordPackage;

    /**
     * 为生成的代码指定作者
     */
    @NotEmpty String author;

    /**
     * Design类的包名
     */
    @NotEmpty String designPackage;

    /**
     * 是否在该生成的地方生成诸如 Allison 1875 Lot No: QT1000S-967D9357 的声明
     */
    @NotNull Boolean enableLotNoAnnounce;

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
            this.mapperConditionPackage = packageNameWithWildcard.replace(".-", ".javabean.cond");
            this.mapperRecordPackage = packageNameWithWildcard.replace(".-", ".javabean.record");
            this.designPackage = packageNameWithWildcard.replace(".-", ".design");
        }
    }

}