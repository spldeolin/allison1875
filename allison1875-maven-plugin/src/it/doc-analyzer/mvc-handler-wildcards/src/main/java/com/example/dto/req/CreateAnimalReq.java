package com.example.dto.req;

import lombok.Data;

/**
 * @author test-author 2026-04-29
 */
@Data
public class CreateAnimalReq {

    /**
     * 动物名称
     */
    private String name;

    /**
     * 动物种类
     */
    private String species;

}
