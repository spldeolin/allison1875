package com.example.dto.resp;

import lombok.Data;

/**
 * 物品响应
 *
 * @author test-author 2026-04-29
 */
@Data
public class ItemResp {

    /**
     * 物品ID
     */
    private Long id;

    /**
     * 物品名称
     */
    private String name;

}
