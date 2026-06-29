package com.spldeolin.allison1875.common.config;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

/**
 * Domain configuration describing where each layer's code lives for a single business domain (immutable).
 *
 * @author Deolin 2026-04-28
 */
@Value
@Jacksonized
@Builder(toBuilder = true)
public class DomainConfig {

    /**
     * 业务领域名称（如 user、order），用于 -Ddomain 参数匹配
     */
    String name;

    /**
     * Controller类所在Maven模块的绝对路径
     */
    String controllerModule;

    /**
     * 控制器所在包的包名
     */
    String controllerPackage;

    /**
     * reqDTO和respDTO类所在Maven模块的绝对路径
     */
    String dtoModule;

    /**
     * 控制层@RequestBody类型所在包的包名
     */
    String reqDTOPackage;

    /**
     * 控制层@ResponseBody业务数据部分类型所在包的包名
     */
    String respDTOPackage;

    /**
     * 枚举类所在Maven模块的绝对路径
     */
    String enumModule;

    /**
     * 枚举所在包的包名
     */
    String enumPackage;

    /**
     * Service接口所在Maven模块的绝对路径
     */
    String serviceModule;

    /**
     * 业务层Service接口所在包的包名
     */
    String servicePackage;

    /**
     * ServiceImpl类所在Maven模块的绝对路径
     */
    String serviceImplModule;

    /**
     * 业务层ServiceImpl类所在包的包名
     */
    String serviceImplPackage;

    /**
     * 持久层代码所在Maven模块的绝对路径
     */
    String persistenceModule;

    /**
     * 持久层mapper接口所在包的包名
     */
    String mapperPackage;

    /**
     * 持久层Entity类所在包的包名
     */
    String entityPackage;

    /**
     * Design类所在包的包名
     */
    String designPackage;

    /**
     * 持久层Mapper方法签名中Param类所在包的包名
     */
    String paramDTOPackage;

    /**
     * 持久层Mapper方法签名中Record类所在包的包名
     */
    String recordDTOPackage;

    /**
     * mapper.xml所在目录（相对于持久层module的basedir的相对路径 或 绝对路径 皆可）
     */
    List<File> mapperXmlDirs;

    /**
     * WholeDTO类所在包的包名
     */
    String wholeDTOPackage;

    /**
     * Controller层的SourceRoot绝对路径（运行时解析，不在yml中配置）
     */
    @JsonIgnore
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    Path controllerSourceRoot;

    /**
     * DTO层的SourceRoot绝对路径（运行时解析，不在yml中配置）
     */
    @JsonIgnore
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    Path dtoSourceRoot;

    /**
     * 枚举层的SourceRoot绝对路径（运行时解析，不在yml中配置）
     */
    @JsonIgnore
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    Path enumSourceRoot;

    /**
     * Service接口的SourceRoot绝对路径（运行时解析，不在yml中配置）
     */
    @JsonIgnore
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    Path serviceSourceRoot;

    /**
     * ServiceImpl的SourceRoot绝对路径（运行时解析，不在yml中配置）
     */
    @JsonIgnore
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    Path serviceImplSourceRoot;

    /**
     * 持久层的SourceRoot绝对路径（运行时解析，不在yml中配置）
     */
    @JsonIgnore
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    Path persistenceSourceRoot;

    /**
     * Apply default values to null fields.
     */
    static DomainConfig applyDefaults(DomainConfig raw) {
        DomainConfigBuilder b = raw.toBuilder();
        if (raw.mapperXmlDirs == null) {
            b.mapperXmlDirs(List.of(new File("src/main/resources/mapper")));
        }
        return b.build();
    }

    /**
     * Validate required fields and collect error messages into the provided list.
     */
    static void validate(DomainConfig dc, String prefix, List<String> errors) {
        if (dc.name == null || dc.name.isEmpty()) {
            errors.add(prefix + "name must not be empty");
        }
        if (dc.controllerModule == null || dc.controllerModule.isEmpty()) {
            errors.add(prefix + "controllerModule must not be empty");
        }
        if (dc.controllerPackage == null || dc.controllerPackage.isEmpty()) {
            errors.add(prefix + "controllerPackage must not be empty");
        }
        if (dc.dtoModule == null || dc.dtoModule.isEmpty()) {
            errors.add(prefix + "dtoModule must not be empty");
        }
        if (dc.reqDTOPackage == null || dc.reqDTOPackage.isEmpty()) {
            errors.add(prefix + "reqDTOPackage must not be empty");
        }
        if (dc.respDTOPackage == null || dc.respDTOPackage.isEmpty()) {
            errors.add(prefix + "respDTOPackage must not be empty");
        }
        if (dc.enumModule == null || dc.enumModule.isEmpty()) {
            errors.add(prefix + "enumModule must not be empty");
        }
        if (dc.enumPackage == null || dc.enumPackage.isEmpty()) {
            errors.add(prefix + "enumPackage must not be empty");
        }
        if (dc.serviceModule == null || dc.serviceModule.isEmpty()) {
            errors.add(prefix + "serviceModule must not be empty");
        }
        if (dc.servicePackage == null || dc.servicePackage.isEmpty()) {
            errors.add(prefix + "servicePackage must not be empty");
        }
        if (dc.serviceImplModule == null || dc.serviceImplModule.isEmpty()) {
            errors.add(prefix + "serviceImplModule must not be empty");
        }
        if (dc.serviceImplPackage == null || dc.serviceImplPackage.isEmpty()) {
            errors.add(prefix + "serviceImplPackage must not be empty");
        }
        if (dc.persistenceModule == null || dc.persistenceModule.isEmpty()) {
            errors.add(prefix + "persistenceModule must not be empty");
        }
        if (dc.mapperPackage == null || dc.mapperPackage.isEmpty()) {
            errors.add(prefix + "mapperPackage must not be empty");
        }
        if (dc.entityPackage == null || dc.entityPackage.isEmpty()) {
            errors.add(prefix + "entityPackage must not be empty");
        }
        if (dc.designPackage == null || dc.designPackage.isEmpty()) {
            errors.add(prefix + "designPackage must not be empty");
        }
        if (dc.paramDTOPackage == null || dc.paramDTOPackage.isEmpty()) {
            errors.add(prefix + "paramDTOPackage must not be empty");
        }
        if (dc.recordDTOPackage == null || dc.recordDTOPackage.isEmpty()) {
            errors.add(prefix + "recordDTOPackage must not be empty");
        }
        if (dc.mapperXmlDirs == null || dc.mapperXmlDirs.isEmpty()) {
            errors.add(prefix + "mapperXmlDirs must not be empty");
        }
        if (dc.wholeDTOPackage == null || dc.wholeDTOPackage.isEmpty()) {
            errors.add(prefix + "wholeDTOPackage must not be empty");
        }
    }

}
