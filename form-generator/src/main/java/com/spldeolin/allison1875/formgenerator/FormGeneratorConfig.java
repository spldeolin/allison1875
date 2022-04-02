package com.spldeolin.allison1875.formgenerator;

import javax.validation.constraints.NotEmpty;
import lombok.Data;

/**
 * @author Deolin 2022-04-02
 */
@Data
public final class FormGeneratorConfig {

    /**
     * 为生成的代码指定作者
     */
    @NotEmpty
    private String author;

    /**
     * Controller的包名（根据目标工程的情况填写）
     */
    @NotEmpty
    private String controllerPackage;

    /**
     * 控制层 @RequestBody类型所在包的包名
     */
    @NotEmpty
    private String reqDtoPackage;

    /**
     * 控制层 @ResponseBody业务数据部分类型所在包的包名
     */
    @NotEmpty
    private String respDtoPackage;

    /**
     * Enum的包名
     */
    private String enumPackage;

    /**
     * Service的包名（根据目标工程的情况填写）
     */
    @NotEmpty
    private String servicePackage;

    /**
     * ServiceImpl的包名
     */
    private String serviceImplPackage;

    /**
     * Logic的包名
     */
    private String logicPackage;

    /**
     * Mapper的包名
     */
    private String mapperPackage;

    /**
     * Mapper.xml的相对路径
     */
    private String mapperXmlDirectory;

    /**
     * Design类的包名
     */
    private String designPackage;

    /**
     * 使用通配符的方式设置所有包名，通配符是<code>.-</code>
     *
     * <pre>
     * e.g.1:
     * input:
     *  com.company.orginization.project.-
     *
     * output:
     *  com.company.orginization.project.controller
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
     *  com.company.orginization.project.controller.module.sub
     *  com.company.orginization.project.javabean.req.module.sub
     *  com.company.orginization.project.javabean.resp.module.sub
     *  com.company.orginization.project.service.module.sub
     *  com.company.orginization.project.serviceimpl.module.sub
     *
     * </pre>
     */
    public void batchSetAllPackagesByWildcard(String packageNameWithWildcard) {
        if (packageNameWithWildcard != null && packageNameWithWildcard.contains(".-")) {
            this.controllerPackage = packageNameWithWildcard.replace(".-", ".controller");
            this.reqDtoPackage = packageNameWithWildcard.replace(".-", ".javabean.req");
            this.respDtoPackage = packageNameWithWildcard.replace(".-", ".javabean.resp");
            this.servicePackage = packageNameWithWildcard.replace(".-", ".service");
            this.serviceImplPackage = packageNameWithWildcard.replace(".-", ".serviceimpl");

        }
    }

}