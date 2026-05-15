package com.example.dto.req;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import javax.validation.constraints.FutureOrPresent;
import javax.validation.constraints.Negative;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.PastOrPresent;
import lombok.Data;

/**
 * @author test-author 2026-04-29
 */
@Data
public class AdvancedValidReq {

    /**
     * 标签列表（@NotEmpty集合校验）
     */
    @NotEmpty
    private List<String> tags;

    /**
     * 负数值（@Negative）
     */
    @Negative
    private Integer negativeValue;

    /**
     * 开始日期（@FutureOrPresent）
     */
    @FutureOrPresent
    private Date startDate;

    /**
     * 结束日期（@PastOrPresent）
     */
    @PastOrPresent
    private LocalDate endDate;

}
