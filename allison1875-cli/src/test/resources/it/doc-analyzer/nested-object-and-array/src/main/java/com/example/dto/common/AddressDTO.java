package com.example.dto.common;

import lombok.Data;

/**
 * 地址DTO（被多处引用的共享DTO，触发「数据结构同」引用路径）
 *
 * @author test-author 2026-04-29
 */
@Data
public class AddressDTO {

    /**
     * 省份
     */
    private String province;

    /**
     * 城市
     */
    private String city;

    /**
     * 街道
     */
    private String street;

}
