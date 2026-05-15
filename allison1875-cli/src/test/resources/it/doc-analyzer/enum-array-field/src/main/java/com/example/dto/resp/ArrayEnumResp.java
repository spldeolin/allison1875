package com.example.dto.resp;

import com.example.enums.PermissionEnum;
import lombok.Data;

/**
 * @author test-author 2026-04-29
 */
@Data
public class ArrayEnumResp {
    /** ID */
    private String id;
    /** 权限列表 */
    private PermissionEnum[] permissions;
}
