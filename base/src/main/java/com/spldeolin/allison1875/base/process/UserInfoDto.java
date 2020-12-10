package com.spldeolin.allison1875.base.process;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import lombok.Data;

/**
 * @author Deolin 2020-12-10
 */
@Data
public class UserInfoDto {

    private String hostAddress;

    private String hostName;

    private String userLocation;

    private String allison1875Version;

    private LocalDateTime when;

    private List<String> moduleNames;

    /**
     * Config类名：Config对象
     */
    private Map<String, Object> configs;

}