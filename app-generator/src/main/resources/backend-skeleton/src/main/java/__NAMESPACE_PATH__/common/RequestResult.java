package __NAMESPACE__.common;

import org.slf4j.MDC;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public final class RequestResult<T> {

    String errorCode;

    T data;

    String errorMsg;

    String traceId;

    private RequestResult() {
    }

    public static RequestResult<Void> success() {
        return success(null);
    }

    public static <T> RequestResult<T> success(T data) {
        RequestResult<T> result = new RequestResult<>();
        result.setErrorCode(null);
        result.setData(data);
        result.setTraceId(resolveTraceId());
        return result;
    }

    public static RequestResult<?> failure(__NAMESPACE__.common.ErrorCode errorCode) {
        return failure(errorCode, null);
    }

    public static RequestResult<?> failure(__NAMESPACE__.common.ErrorCode errorCode, String errorMsg) {
        RequestResult<Void> result = new RequestResult<>();
        result.setErrorCode(errorCode.getCode());
        result.setErrorMsg(errorMsg != null ? errorMsg : errorCode.getDefaultMsg());
        result.setTraceId(resolveTraceId());
        return result;
    }

    private static String resolveTraceId() {
        String mdcTraceId = MDC.get("traceId");
        if (mdcTraceId == null) {
            return null;
        }
        int commaIndex = mdcTraceId.indexOf(',');
        if (commaIndex > 0) {
            return mdcTraceId.substring(0, commaIndex);
        }
        return mdcTraceId;
    }

}
