package com.example.dto.req;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import javax.validation.constraints.Future;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import org.hibernate.validator.constraints.Length;
import lombok.Data;

/**
 * @author test-author 2026-04-29
 */
@Data
public class CreatePaymentReq {

    /**
     * 支付描述
     */
    @NotBlank
    @Length(min = 1, max = 100)
    private String description;

    /**
     * 支付金额
     */
    @NotNull
    @DecimalMin("0.01")
    @DecimalMax("999999.99")
    @Digits(integer = 6, fraction = 2)
    private BigDecimal amount;

    /**
     * 预计支付时间
     */
    @Future
    private Date expectedPayTime;

    /**
     * 支付笔数
     */
    @Positive
    private Integer count;

    /**
     * 标签列表
     */
    private List<@NotBlank @Length(max = 20) String> tags;

}
