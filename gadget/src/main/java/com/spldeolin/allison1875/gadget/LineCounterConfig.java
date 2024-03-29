package com.spldeolin.allison1875.gadget;

import java.util.List;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;
import com.spldeolin.allison1875.common.ancestor.Allison1875Config;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

/**
 * @author Deolin 2020-10-28
 */
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public final class LineCounterConfig extends Allison1875Config {

    /**
     * 对这些指定后缀的Java类型的进行专门的统计
     */
    @NotNull List<String> typePostfix;

    /**
     * 显示阈值，行数大于等于这个值时，才在排行榜中显示
     */
    @NotNull @PositiveOrZero Integer displayThreshold;

    /**
     * 危险阈值，行数大于等于这个值时，在排行榜会被标记成 [危」
     */
    @NotNull @PositiveOrZero Integer dangerThreshold;

    /**
     * 排行榜尺寸（0代表不打印排行榜）
     */
    @NotNull @PositiveOrZero Integer rankListSize;

}