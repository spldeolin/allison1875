package com.external.dto;

/**
 * 外部定义的基础信息DTO
 *
 * <p>这个类定义在 external-dto 目录中，通过 dependencyDirsOrJavaFilePath 配置加载
 */
public class BaseInfo {

    /**
     * 外部创建者名称
     */
    private String creatorName;

    /**
     * 外部创建时间
     */
    private String createTime;

    public String getCreatorName() {
        return creatorName;
    }

    public void setCreatorName(String creatorName) {
        this.creatorName = creatorName;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

}
