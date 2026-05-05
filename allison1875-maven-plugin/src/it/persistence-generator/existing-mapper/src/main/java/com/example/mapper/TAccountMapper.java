package com.example.mapper;

import java.util.List;

/**
 * 账户表
 * <p>t_account
 *
 * @author test-author 2024-01-01
 * @see TAccountEntity
 */
public interface TAccountMapper {

    /**
     * 自定义方法：根据余额范围查询
     */
    List<Object> queryByBalanceRange(java.math.BigDecimal min, java.math.BigDecimal max);

    /**
     * 自定义方法：统计总余额
     */
    java.math.BigDecimal sumBalance();

}
