package com.example.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.dto.req.CreateAddressReq;
import com.example.dto.resp.AddressResp;

/**
 * 地址管理
 *
 * @author test-author 2026-04-29
 */
@RestController
@RequestMapping("/api/addresses")
public class AddressController {

    /**
     * 创建地址
     */
    @PostMapping
    public AddressResp createAddress(@RequestBody CreateAddressReq req) {
        return null;
    }

}
