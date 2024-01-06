package com.spldeolin.allison1875.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 文件已存在时的解决方式
 *
 * @author Deolin 2023-12-29
 */
@Getter
@AllArgsConstructor
public enum FileExistenceResolutionEnum {

    OVERWRITE,

    RENAME,

}
