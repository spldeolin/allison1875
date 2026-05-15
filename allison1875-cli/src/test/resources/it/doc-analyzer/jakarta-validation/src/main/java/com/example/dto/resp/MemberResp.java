package com.example.dto.resp;

import lombok.Data;

/**
 * @author test-author 2026-04-29
 */
@Data
public class MemberResp {

    /**
     * ID
     */
    private Long id;

    /**
     * 会员名称
     */
    private String memberName;

    /**
     * 会员等级
     */
    private Integer level;

}
