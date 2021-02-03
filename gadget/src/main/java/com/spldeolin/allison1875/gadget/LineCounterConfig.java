package com.spldeolin.allison1875.gadget;

import java.util.Collection;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;
import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import lombok.Data;

/**
 * @author Deolin 2020-10-28
 */
@Singleton
@Data
public final class LineCounterConfig extends AbstractModule {

    /**
     * 对这些指定后缀的Java类型的进行专门的统计
     */
    @NotNull
    protected Collection<String> typePostfix;

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

    @Override
    protected void configure() {
        bind(LineCounterConfig.class).toInstance(this);
    }

}