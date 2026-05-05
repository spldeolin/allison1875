package com.example.dto.req;

/**
 * 使用 Java Record 作为请求体
 *
 * @param title 地址标题
 * @param city 城市
 * @param zipCode 邮编
 */
public record CreateAddressReq(String title, String city, String zipCode) {

}
