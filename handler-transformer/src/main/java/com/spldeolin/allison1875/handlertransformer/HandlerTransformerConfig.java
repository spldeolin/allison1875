package com.spldeolin.allison1875.handlertransformer;

import javax.validation.constraints.NotEmpty;
import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import lombok.Data;

/**
 * Allison1875[handler-transformer]的配置
 *
 * @author Deolin 2020-08-25
 */
@Singleton
@Data
public final class HandlerTransformerConfig extends AbstractModule {

    /**
     * 控制层 @RequestBody类型所在包的包名
     */
    @NotEmpty
    protected String reqDtoPackage;

    /**
     * 控制层 @ResponseBody业务数据部分类型所在包的包名
     */
    @NotEmpty
    protected String respDtoPackage;

    /**
     * 业务层 Service接口所在包的包名
     */
    @NotEmpty
    protected String servicePackage;

    /**
     * 业务 ServiceImpl类所在包的包名
     */
    @NotEmpty
    private String serviceImplPackage;

    /**
     * 为生成的代码指定作者
     */
    @NotEmpty
    private String author;

    /**
     * 分页对象的全限定名
     */
    @NotEmpty
    private String pageTypeQualifier;

    @Override
    protected void configure() {
        bind(HandlerTransformerConfig.class).toInstance(this);
    }

    /**
     * 使用通配符的方式设置所有包名，通配符是<code>.-</code>
     *
     * <pre>
     * e.g.1:
     * input:
     *  com.company.orginization.project.-
     *
     * output:
     *  com.company.orginization.project.javabean.req
     *  com.company.orginization.project.javabean.resp
     *  com.company.orginization.project.service
     *  com.company.orginization.project.serviceimpl
     *
     *
     * e.g.2:
     * input:
     *  com.company.orginization.project.-.module.sub
     *
     * output:
     *  com.company.orginization.project.javabean.req.module.sub
     *  com.company.orginization.project.javabean.resp.module.sub
     *  com.company.orginization.project.service.module.sub
     *  com.company.orginization.project.serviceimpl.module.sub
     *
     * </pre>
     */
    public void batchSetAllPackagesByWildcard(String packageNameWithWildcard) {
        if (packageNameWithWildcard != null && packageNameWithWildcard.contains(".-")) {
            this.reqDtoPackage = packageNameWithWildcard.replace(".-", ".javabean.req");
            this.respDtoPackage = packageNameWithWildcard.replace(".-", ".javabean.resp");
            this.servicePackage = packageNameWithWildcard.replace(".-", ".service");
            this.serviceImplPackage = packageNameWithWildcard.replace(".-", ".serviceimpl");
        }
    }

}