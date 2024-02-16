package com.spldeolin.allison1875.handlertransformer;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import com.spldeolin.allison1875.common.ancestor.Allison1875Config;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

/**
 * Allison1875[handler-transformer]的配置
 *
 * @author Deolin 2020-08-25
 */
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public final class HandlerTransformerConfig extends Allison1875Config {

    /**
     * 控制层 @RequestBody类型所在包的包名
     */
    @NotEmpty String reqDtoPackage;

    /**
     * 控制层 ReqDto中的NestDto所在的包的报名，
     * null代表包名缺省为 ${reqDtoPackage}.dto
     */
    String reqNestDtoPackage;

    /**
     * 控制层 @ResponseBody业务数据部分类型所在包的包名
     */
    @NotEmpty String respDtoPackage;

    /**
     * 控制层 RespDto中的NestDto所在的包的报名，
     * null代表包名缺省为 ${respDtoPackage}.dto
     */
    String respNestDtoPackage;

    /**
     * 业务层 Service接口所在包的包名
     */
    @NotEmpty String servicePackage;

    /**
     * 业务 ServiceImpl类所在包的包名
     */
    @NotEmpty String serviceImplPackage;

    /**
     * 为生成的代码指定作者
     */
    @NotEmpty String author;

    /**
     * 分页对象的全限定名
     */
    @NotEmpty String pageTypeQualifier;

    /**
     * 是否在该生成的地方生成诸如 Allison 1875 Lot No: HT1000S-967D9357 的声明
     */
    @NotNull Boolean enableLotNoAnnounce;

}