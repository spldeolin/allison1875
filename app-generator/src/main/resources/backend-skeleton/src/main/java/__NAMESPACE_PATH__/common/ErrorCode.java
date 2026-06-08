package __NAMESPACE__.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 错误码
 *
 * @author Deolin 2026-06-08
 */
@AllArgsConstructor
@Getter
public enum ErrorCode {

    /**
     * 参数非法
     */
    BAD_REQUEST("400", "参数非法"),

    /**
     * 认证不通过
     */
    UNAUTHORIZED("401", "认证失效"),

    /**
     * 鉴权不通过
     */
    FORBIDDEN("403", "未授权"),

    /**
     * 内部错误
     */
    INTERNAL_ERROR("500", "内部错误"),

    /**
     * 业务错误
     */
    BIZ_ERROR("501", "业务异常"),

    ;

    private final String code;

    private final String defaultMsg;

}
