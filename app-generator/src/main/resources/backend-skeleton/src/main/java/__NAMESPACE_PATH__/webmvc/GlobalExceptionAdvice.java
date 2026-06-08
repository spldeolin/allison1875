package __NAMESPACE__.webmvc;

import java.util.stream.Collectors;
import javax.servlet.ServletException;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import __NAMESPACE__.common.ErrorCode;
import __NAMESPACE__.common.RequestResult;
import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice
@Order(Ordered.LOWEST_PRECEDENCE)
@Slf4j
public class GlobalExceptionAdvice {

    @ExceptionHandler(ServletException.class)
    public RequestResult<?> handler(ServletException e) {
        log.warn("ServletException, message={}", e.getMessage());
        return RequestResult.failure(ErrorCode.BAD_REQUEST, "非法请求");
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public RequestResult<?> httpMessageNotReadable(HttpMessageNotReadableException e) {
        log.warn("请求Body非法，message={}", e.getMessage());
        return RequestResult.failure(ErrorCode.BAD_REQUEST, "非法请求");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public RequestResult<?> handle(MethodArgumentNotValidException e) {
        log.warn("请求Body参数校验未通过，invalids={}", e.getBindingResult().getFieldErrors().stream()
                .map(fe -> String.format("path=%s reason=%s value=%s", fe.getField(), fe.getDefaultMessage(),
                        fe.getRejectedValue())).collect(Collectors.joining(" | ")));
        return RequestResult.failure(ErrorCode.BAD_REQUEST, "非法请求");
    }

    @ExceptionHandler(__NAMESPACE__.common.BizException.class)
    public RequestResult<?> handle(__NAMESPACE__.common.BizException e) {
        return RequestResult.failure(e.errorCode(), e.getMessage());
    }

    @ExceptionHandler(Throwable.class)
    public RequestResult<?> handle(Throwable e) {
        log.error("WEB请求在服务端发生异常", e);
        return RequestResult.failure(ErrorCode.INTERNAL_ERROR,
                String.format("内部错误，请稍后重试（%s）", MDC.get("traceId")));
    }

}
