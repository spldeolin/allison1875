package com.example.dto.resp;

import lombok.Data;

/**
 * 整数子类型响应（验证Long/Short/Byte等不同整数类型渲染）
 *
 * @author test-author 2026-04-29
 */
@Data
public class IntegerSubtypeResp {

    /**
     * 包装Long类型
     */
    private Long longValue;

    /**
     * 包装Short类型
     */
    private Short shortValue;

    /**
     * 包装Byte类型
     */
    private Byte byteValue;

    /**
     * 原始long类型
     */
    private long primitiveLong;

    /**
     * 原始short类型
     */
    private short primitiveShort;

    /**
     * 原始byte类型
     */
    private byte primitiveByte;

}
