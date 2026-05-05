package com.example.dto.resp;

import lombok.Data;

/**
 * @author test-author 2026-04-29
 */
@Data
public class AnimalResp {

    /**
     * ID
     */
    private Long id;

    /**
     * 动物名称
     */
    private String name;

    /**
     * 动物种类
     */
    private String species;

}
