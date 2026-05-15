package com.example.dto.resp;

/**
 * 地址响应（使用 Java Record）
 *
 * @param id ID
 * @param title 地址标题
 * @param city 城市
 * @param zipCode 邮编
 */
public record AddressResp(Long id, String title, String city, String zipCode) {

}
