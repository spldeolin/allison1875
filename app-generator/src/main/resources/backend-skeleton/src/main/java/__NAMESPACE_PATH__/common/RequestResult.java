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
        result.setTraceId(MDC.get("traceId"));
        return result;
    }

    public static RequestResult<?> failure(ErrorCode errorCode) {
        RequestResult<Void> result = new RequestResult<>();
        result.setErrorCode(errorCode.code());
        result.setErrorMsg(errorCode.defaultMsg());
        result.setTraceId(MDC.get("traceId"));
        return result;
    }

    public static RequestResult<?> failure(ErrorCode errorCode, String errorMsg) {
        RequestResult<Void> result = new RequestResult<>();
        result.setErrorCode(errorCode.code());
        result.setErrorMsg(errorMsg);
        result.setTraceId(MDC.get("traceId"));
        return result;
    }

}
