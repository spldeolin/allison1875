package __NAMESPACE__.common;

public class BizException extends RuntimeException {

    private static final long serialVersionUID = -4104806330438981374L;

    private final __NAMESPACE__.common.ErrorCode errorCode;

    private final String errorMsg;

    public BizException(String message) {
        super(message);
        this.errorCode = __NAMESPACE__.common.ErrorCode.BIZ_ERROR;
        this.errorMsg = message;
    }

    public BizException(__NAMESPACE__.common.ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.errorMsg = message;
    }

    public final __NAMESPACE__.common.ErrorCode errorCode() {
        return errorCode;
    }

    public final String getErrorMsg() {
        return errorMsg;
    }

}
