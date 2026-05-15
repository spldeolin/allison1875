package com.example.controller;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 订单管理
 *
 * @author test-author 2026-04-29
 */
@RestController
@RequestMapping("/api/order")
public class OrderController {

    {
        String handler = "/create-order";
        String desc = "创建订单";
        class Req {
            /** 订单名称 */
            @NotBlank
            String orderName;
            /** 金额 */
            @NotNull
            Long amount;
        }
        class Resp {
            /** 订单ID */
            Long orderId;
            /** 订单状态 */
            String status;
        }
    }

}
