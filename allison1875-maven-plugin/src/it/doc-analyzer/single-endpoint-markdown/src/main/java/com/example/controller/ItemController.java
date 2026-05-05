package com.example.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.example.dto.req.SearchReq;
import com.example.dto.resp.ItemResp;

/**
 * 物品管理
 *
 * @author test-author 2026-04-29
 */
@RestController
@RequestMapping("/items")
public class ItemController {

    /**
     * 搜索物品
     */
    @GetMapping("/search")
    public ItemResp search(@RequestParam(name = "q") String query,
            @RequestParam(value = "page", required = false, defaultValue = "1") Integer pageNo) {
        return null;
    }

    /**
     * 提交物品
     */
    @PostMapping
    public void submitItem(@RequestBody SearchReq req) {
    }

}
