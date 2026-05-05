package com.example.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.dto.req.CreateMemberReq;
import com.example.dto.resp.MemberResp;

/**
 * 会员管理
 *
 * @author test-author 2026-04-29
 */
@RestController
@RequestMapping("/api/members")
public class MemberController {

    /**
     * 创建会员
     */
    @PostMapping
    public MemberResp createMember(@RequestBody CreateMemberReq req) {
        return null;
    }

}
