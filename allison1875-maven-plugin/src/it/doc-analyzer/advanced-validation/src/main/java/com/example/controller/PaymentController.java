package com.example.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.dto.req.CreatePaymentReq;
import com.example.dto.resp.PaymentResp;

/**
 * 支付管理
 *
 * @author test-author 2026-04-29
 */
@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    /**
     * 创建支付
     */
    @PostMapping
    public PaymentResp createPayment(@RequestBody CreatePaymentReq req) {
        return null;
    }

}
