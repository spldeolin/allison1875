package com.spldeolin.allison1875.si;

import java.nio.file.Path;
import java.nio.file.Paths;
import com.spldeolin.allison1875.base.BaseConfig;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 【statute-inspector】的全局配置
 *
 * @author Deolin 2020-02-18
 */
@Data
@EqualsAndHashCode(callSuper = true)
public final class StatuteInspectorConfig extends BaseConfig {

    public static final StatuteInspectorConfig CONFIG = new StatuteInspectorConfig();

    private Path lawlessReportCsvPath;

    private StatuteInspectorConfig() {
        super();
        this.initLoad();
    }

    private void initLoad() {
        lawlessReportCsvPath = Paths.get(super.rawData.get("lawlessReportCsvPath"));
    }

}
