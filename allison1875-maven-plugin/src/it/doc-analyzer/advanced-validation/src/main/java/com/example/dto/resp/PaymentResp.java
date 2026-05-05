package com.example.dto.resp;

import java.math.BigDecimal;
import lombok.Data;

/**
 * @author test-author 2026-04-29
 */
@Data
public class PaymentResp {

    /**
     * ID
     */
    private Long id;

    /**
     * 支付金额
     */
    private BigDecimal amount;

    /**
     * 状态
     */
    private String status;

}
