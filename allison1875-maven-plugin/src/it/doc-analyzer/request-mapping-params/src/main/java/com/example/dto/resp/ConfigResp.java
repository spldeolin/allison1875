package com.example.dto.resp;

import lombok.Data;

/**
 * @author test-author 2026-04-29
 */
@Data
public class ConfigResp {

    /**
     * 配置键
     */
    private String configKey;

    /**
     * 配置值
     */
    private String configValue;

}
