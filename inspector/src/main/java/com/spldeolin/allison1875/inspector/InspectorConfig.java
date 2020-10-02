package com.spldeolin.allison1875.inspector;

import java.time.LocalDateTime;
import javax.validation.constraints.NotNull;

/**
 * Allison1875[inspector]的配置
 *
 * @author Deolin 2020-02-18
 */
public final class InspectorConfig {

    private static final InspectorConfig instance = new InspectorConfig();

    /**
     * 工程所在的Git本地仓库的路径
     */
    @NotNull
    private String projectLocalGitPath;

    /**
     * 此时间之后新增的文件为靶文件，不填则代表全项目的文件均为靶文件
     */
    @NotNull
    private LocalDateTime targetFileSince;

    /**
     * 周知JSON目录的路径
     */
    private String pardonDirectoryPath;

    /**
     * 检查结果CSV文件输出目录的路径
     */
    private String lawlessDirectoryPath;

    private InspectorConfig() {
    }

    public static InspectorConfig getInstance() {
        return InspectorConfig.instance;
    }

    public @NotNull String getProjectLocalGitPath() {
        return this.projectLocalGitPath;
    }

    public @NotNull LocalDateTime getTargetFileSince() {
        return this.targetFileSince;
    }

    public String getPardonDirectoryPath() {
        return this.pardonDirectoryPath;
    }

    public String getLawlessDirectoryPath() {
        return this.lawlessDirectoryPath;
    }

    public void setProjectLocalGitPath(@NotNull String projectLocalGitPath) {
        this.projectLocalGitPath = projectLocalGitPath;
    }

    public void setTargetFileSince(@NotNull LocalDateTime targetFileSince) {
        this.targetFileSince = targetFileSince;
    }

    public void setPardonDirectoryPath(String pardonDirectoryPath) {
        this.pardonDirectoryPath = pardonDirectoryPath;
    }

    public void setLawlessDirectoryPath(String lawlessDirectoryPath) {
        this.lawlessDirectoryPath = lawlessDirectoryPath;
    }

    public boolean equals(final Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof InspectorConfig)) {
            return false;
        }
        final InspectorConfig other = (InspectorConfig) o;
        final Object this$projectLocalGitPath = this.getProjectLocalGitPath();
        final Object other$projectLocalGitPath = other.getProjectLocalGitPath();
        if (this$projectLocalGitPath == null ? other$projectLocalGitPath != null
                : !this$projectLocalGitPath.equals(other$projectLocalGitPath)) {
            return false;
        }
        final Object this$targetFileSince = this.getTargetFileSince();
        final Object other$targetFileSince = other.getTargetFileSince();
        if (this$targetFileSince == null ? other$targetFileSince != null
                : !this$targetFileSince.equals(other$targetFileSince)) {
            return false;
        }
        final Object this$pardonDirectoryPath = this.getPardonDirectoryPath();
        final Object other$pardonDirectoryPath = other.getPardonDirectoryPath();
        if (this$pardonDirectoryPath == null ? other$pardonDirectoryPath != null
                : !this$pardonDirectoryPath.equals(other$pardonDirectoryPath)) {
            return false;
        }
        final Object this$lawlessDirectoryPath = this.getLawlessDirectoryPath();
        final Object other$lawlessDirectoryPath = other.getLawlessDirectoryPath();
        if (this$lawlessDirectoryPath == null ? other$lawlessDirectoryPath != null
                : !this$lawlessDirectoryPath.equals(other$lawlessDirectoryPath)) {
            return false;
        }
        return true;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $projectLocalGitPath = this.getProjectLocalGitPath();
        result = result * PRIME + ($projectLocalGitPath == null ? 43 : $projectLocalGitPath.hashCode());
        final Object $targetFileSince = this.getTargetFileSince();
        result = result * PRIME + ($targetFileSince == null ? 43 : $targetFileSince.hashCode());
        final Object $pardonDirectoryPath = this.getPardonDirectoryPath();
        result = result * PRIME + ($pardonDirectoryPath == null ? 43 : $pardonDirectoryPath.hashCode());
        final Object $lawlessDirectoryPath = this.getLawlessDirectoryPath();
        result = result * PRIME + ($lawlessDirectoryPath == null ? 43 : $lawlessDirectoryPath.hashCode());
        return result;
    }

    public String toString() {
        return "InspectorConfig(projectLocalGitPath=" + this.getProjectLocalGitPath() + ", targetFileSince=" + this
                .getTargetFileSince() + ", pardonDirectoryPath=" + this.getPardonDirectoryPath()
                + ", lawlessDirectoryPath=" + this.getLawlessDirectoryPath() + ")";
    }

}
