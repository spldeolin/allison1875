package com.spldeolin.allison1875.gadget;

import java.util.Collection;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;
import com.google.common.collect.Lists;

/**
 * @author Deolin 2020-10-28
 */
public class LineCounterConfig {

    private static final LineCounterConfig instance = new LineCounterConfig();

    /**
     * 对这些指定后缀的Java类型的进行专门的统计
     */
    @NotNull
    private Collection<String> typePostfix = Lists.newArrayList();

    /**
     * 是否将src/test/java目录下的文件也统计在内
     */
    @NotNull
    private Boolean withTest = false;

    /**
     * 显示阈值，行数大于等于这个值时，才在排行榜中显示
     */
    @NotNull
    @PositiveOrZero
    private Integer displayThreshold = 0;

    /**
     * 危险阈值，行数大于等于这个值时，在排行榜会被标记成 [危」
     */
    @NotNull
    @PositiveOrZero
    private Integer dangerThreshold = Integer.MAX_VALUE;

    /**
     * 排行榜尺寸（0代表不打印排行榜）
     */
    @NotNull
    @PositiveOrZero
    private Integer rankListSize = 10;

    public static LineCounterConfig getInstance() {
        return LineCounterConfig.instance;
    }

    public Collection<String> getTypePostfix() {
        return typePostfix;
    }

    public void setTypePostfix(Collection<String> typePostfix) {
        this.typePostfix = typePostfix;
    }

    public Boolean getWithTest() {
        return withTest;
    }

    public void setWithTest(Boolean withTest) {
        this.withTest = withTest;
    }

    public Integer getRankListSize() {
        return rankListSize;
    }

    public void setRankListSize(Integer rankListSize) {
        this.rankListSize = rankListSize;
    }

    public Integer getDisplayThreshold() {
        return displayThreshold;
    }

    public void setDisplayThreshold(Integer displayThreshold) {
        this.displayThreshold = displayThreshold;
    }

    public Integer getDangerThreshold() {
        return dangerThreshold;
    }

    public void setDangerThreshold(Integer dangerThreshold) {
        this.dangerThreshold = dangerThreshold;
    }

}