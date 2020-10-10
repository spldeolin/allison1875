package com.spldeolin.allison1875.querytransformer;

import java.util.Map;
import javax.validation.constraints.NotEmpty;
import com.google.common.collect.Maps;

/**
 * @author Deolin 2020-08-09
 */
public class QueryTransformerConfig {

    private static final QueryTransformerConfig instance = new QueryTransformerConfig();

    /**
     * mapper.xml所在目录的相对路径（根据目标工程的情况填写）
     */
    @NotEmpty
    private String mapperXmlDirectoryPath;

    /**
     * Entity通用属性的类型
     */
    private Map<String, String> entityCommonPropertyTypes = Maps.newHashMap();

    private QueryTransformerConfig() {
    }

    public static QueryTransformerConfig getInstance() {
        return QueryTransformerConfig.instance;
    }

    public String getMapperXmlDirectoryPath() {
        return mapperXmlDirectoryPath;
    }

    public void setMapperXmlDirectoryPath(String mapperXmlDirectoryPath) {
        this.mapperXmlDirectoryPath = mapperXmlDirectoryPath;
    }

    public Map<String, String> getEntityCommonPropertyTypes() {
        return entityCommonPropertyTypes;
    }

    public void setEntityCommonPropertyTypes(Map<String, String> entityCommonPropertyTypes) {
        this.entityCommonPropertyTypes = entityCommonPropertyTypes;
    }

    public boolean equals(final Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof QueryTransformerConfig)) {
            return false;
        }
        final QueryTransformerConfig other = (QueryTransformerConfig) o;
        if (!other.canEqual(this)) {
            return false;
        }
        final Object this$mapperXmlDirectoryPath = this.getMapperXmlDirectoryPath();
        final Object other$mapperXmlDirectoryPath = other.getMapperXmlDirectoryPath();
        return this$mapperXmlDirectoryPath == null ? other$mapperXmlDirectoryPath == null
                : this$mapperXmlDirectoryPath.equals(other$mapperXmlDirectoryPath);
    }

    protected boolean canEqual(final Object other) {
        return other instanceof QueryTransformerConfig;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $mapperXmlDirectoryPath = this.getMapperXmlDirectoryPath();
        result = result * PRIME + ($mapperXmlDirectoryPath == null ? 43 : $mapperXmlDirectoryPath.hashCode());
        return result;
    }

    public String toString() {
        return "QueryTransformerConfig(mapperXmlDirectoryPath=" + this.getMapperXmlDirectoryPath() + ")";
    }

}