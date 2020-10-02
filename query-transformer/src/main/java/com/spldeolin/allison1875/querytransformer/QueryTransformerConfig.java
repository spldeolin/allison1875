package com.spldeolin.allison1875.querytransformer;

import javax.validation.constraints.NotEmpty;

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

    private QueryTransformerConfig() {
    }

    public static QueryTransformerConfig getInstance() {
        return QueryTransformerConfig.instance;
    }

    public @NotEmpty String getMapperXmlDirectoryPath() {
        return this.mapperXmlDirectoryPath;
    }

    public void setMapperXmlDirectoryPath(@NotEmpty String mapperXmlDirectoryPath) {
        this.mapperXmlDirectoryPath = mapperXmlDirectoryPath;
    }

    public boolean equals(final Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof QueryTransformerConfig)) {
            return false;
        }
        final QueryTransformerConfig other = (QueryTransformerConfig) o;
        if (!other.canEqual((Object) this)) {
            return false;
        }
        final Object this$mapperXmlDirectoryPath = this.getMapperXmlDirectoryPath();
        final Object other$mapperXmlDirectoryPath = other.getMapperXmlDirectoryPath();
        if (this$mapperXmlDirectoryPath == null ? other$mapperXmlDirectoryPath != null
                : !this$mapperXmlDirectoryPath.equals(other$mapperXmlDirectoryPath)) {
            return false;
        }
        return true;
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