package com.example.dto.common;

import lombok.Data;

/**
 * 客户DTO（第2层嵌套）
 *
 * @author test-author 2026-04-29
 */
@Data
public class CustomerDTO {

    /**
     * 客户姓名
     */
    private String name;

    /**
     * 联系电话
     */
    private String phone;

    /**
     * 联系地址（第3层嵌套，AddressDTO被多处引用）
     */
    private AddressDTO contactAddress;

}
