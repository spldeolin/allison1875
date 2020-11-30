package com.spldeolin.allison1875.gadget;

import java.util.Collection;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author Deolin 2020-10-28
 */
@Accessors(chain = true)
@Data
public class LineCounterConfig {

    /**
     * 对这些指定后缀的Java类型的进行专门的统计
     */
    @NotNull
    protected Collection<String> typePostfix;

    /**
     * 是否将src/test/java目录下的文件也统计在内
     */
    @NotNull
    protected Boolean withTest;

    /**
     * 显示阈值，行数大于等于这个值时，才在排行榜中显示
     */
    @NotNull
    @PositiveOrZero
    protected Integer displayThreshold;

    /**
     * 危险阈值，行数大于等于这个值时，在排行榜会被标记成 [危」
     */
    @NotNull
    @PositiveOrZero
    protected Integer dangerThreshold;

    /**
     * 排行榜尺寸（0代表不打印排行榜）
     */
    @NotNull
    @PositiveOrZero
    protected Integer rankListSize;

}