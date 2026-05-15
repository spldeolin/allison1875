package com.example.controller;

import javax.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 嵌套DTO名以DTO结尾测试
 *
 * @author test-author 2026-04-29
 */
@RestController
@RequestMapping("/api/endingdto")
public class EndingDtoController {

    {
        String handler = "/create-record";
        String desc = "创建记录";
        class Req {
            /** 记录名 */
            @NotBlank
            String recordName;
            /** 地址DTO（类名已以DTO结尾） */
            class AddressDTO {
                /** 省份 */
                String province;
                /** 城市 */
                String city;
            }
        }
    }

}
