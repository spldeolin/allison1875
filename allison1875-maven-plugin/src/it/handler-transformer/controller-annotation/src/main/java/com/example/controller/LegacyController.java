package com.example.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 旧式Controller
 *
 * @author test-author 2026-04-29
 */
@Controller
@ResponseBody
@RequestMapping("/api/legacy")
public class LegacyController {

    {
        String handler = "/do-legacy";
        String desc = "旧式Controller处理";
        class Req {
            /** 参数 */
            String param;
        }
        class Resp {
            /** 结果 */
            String result;
        }
    }

}
