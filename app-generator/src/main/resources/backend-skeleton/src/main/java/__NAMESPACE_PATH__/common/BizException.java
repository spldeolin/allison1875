package __NAMESPACE__.common;

public class BizException extends RuntimeException {

    private static final long serialVersionUID = -4104806330438981374L;

    private final ErrorCode errorCode;

    private final String errorMsg;

    public BizException(String message) {
        super(message);
        this.errorCode = ErrorCode.GENERAL_BIZ_EXCEPTION;
        this.errorMsg = message;
    }

    public BizException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.errorMsg = message;
    }

    public final ErrorCode errorCode() {
        return errorCode;
    }

    public final String getErrorMsg() {
        return errorMsg;
    }

}
