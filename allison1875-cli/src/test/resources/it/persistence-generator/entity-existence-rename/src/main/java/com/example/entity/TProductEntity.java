package com.example.entity;

import lombok.Data;

/**
 * 预置的旧 Entity，用于验证 RENAME 策略
 *
 * @author legacy 2020-01-01
 */
@Data
public class TProductEntity {

    /**
     * 旧实体占位标记
     */
    private String legacyMarker = "legacy";

}
