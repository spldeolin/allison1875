package com.example.controller;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 收货管理
 *
 * @author test-author 2026-04-29
 */
@RestController
@RequestMapping("/api/shipping")
public class ShippingController {

    {
        String handler = "/create-shipping";
        String desc = "创建收货信息";
        class Req {
            /** 收件人 */
            @NotBlank
            String recipientName;
            /** 收货地址 */
            class Address {
                /** 省份 */
                @NotBlank
                String province;
                /** 城市 */
                @NotBlank
                String city;
                /** 详细地址 */
                @NotBlank
                String detail;
            }
        }
        class Resp {
            /** 运单ID */
            Long shippingId;
            /** 物流信息 */
            class Logistics {
                /** 物流公司 */
                String company;
                /** 运单号 */
                String trackingNo;
            }
        }
    }

}
