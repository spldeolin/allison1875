package __NAMESPACE__.common;

public interface ErrorCode {

    String code();

    String defaultMsg();

    ErrorCode ILLEGAL_REQUEST = new ErrorCode() {
        @Override
        public String code() {
            return "400";
        }

        @Override
        public String defaultMsg() {
            return "非法请求";
        }
    };

    ErrorCode SERVER_EXCEPTION = new ErrorCode() {
        @Override
        public String code() {
            return "500";
        }

        @Override
        public String defaultMsg() {
            return "内部错误，请稍后重试";
        }
    };

    ErrorCode GENERAL_BIZ_EXCEPTION = new ErrorCode() {
        @Override
        public String code() {
            return "1001";
        }

        @Override
        public String defaultMsg() {
            return null;
        }
    };

}
