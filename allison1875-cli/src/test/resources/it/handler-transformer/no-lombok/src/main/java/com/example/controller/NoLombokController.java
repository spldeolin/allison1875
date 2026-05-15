package com.example.controller;

import javax.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 无Lombok测试
 *
 * @author test-author 2026-04-29
 */
@RestController
@RequestMapping("/api/nolombok")
public class NoLombokController {

    {
        String handler = "/create-item";
        String desc = "创建项目";
        class Req {
            /** 项目名 */
            @NotBlank
            String itemName;
            /** 数量 */
            Integer quantity;
        }
        class Resp {
            /** 项目ID */
            Long itemId;
            /** 项目名 */
            String itemName;
        }
    }

}
