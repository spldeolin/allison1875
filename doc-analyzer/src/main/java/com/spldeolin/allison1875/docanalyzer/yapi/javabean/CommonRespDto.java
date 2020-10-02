package com.spldeolin.allison1875.docanalyzer.yapi.javabean;

/**
 * @author Deolin 2020-08-02
 */
public class CommonRespDto<T> {

    private Integer errcode;

    private String errmsg;

    private T data;

    public CommonRespDto() {
    }

    public Integer getErrcode() {
        return this.errcode;
    }

    public String getErrmsg() {
        return this.errmsg;
    }

    public T getData() {
        return this.data;
    }

    public void setErrcode(Integer errcode) {
        this.errcode = errcode;
    }

    public void setErrmsg(String errmsg) {
        this.errmsg = errmsg;
    }

    public void setData(T data) {
        this.data = data;
    }

    public boolean equals(final Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof CommonRespDto)) {
            return false;
        }
        final CommonRespDto<?> other = (CommonRespDto<?>) o;
        if (!other.canEqual((Object) this)) {
            return false;
        }
        final Object this$errcode = this.getErrcode();
        final Object other$errcode = other.getErrcode();
        if (this$errcode == null ? other$errcode != null : !this$errcode.equals(other$errcode)) {
            return false;
        }
        final Object this$errmsg = this.getErrmsg();
        final Object other$errmsg = other.getErrmsg();
        if (this$errmsg == null ? other$errmsg != null : !this$errmsg.equals(other$errmsg)) {
            return false;
        }
        final Object this$data = this.getData();
        final Object other$data = other.getData();
        if (this$data == null ? other$data != null : !this$data.equals(other$data)) {
            return false;
        }
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof CommonRespDto;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $errcode = this.getErrcode();
        result = result * PRIME + ($errcode == null ? 43 : $errcode.hashCode());
        final Object $errmsg = this.getErrmsg();
        result = result * PRIME + ($errmsg == null ? 43 : $errmsg.hashCode());
        final Object $data = this.getData();
        result = result * PRIME + ($data == null ? 43 : $data.hashCode());
        return result;
    }

    public String toString() {
        return "CommonRespDto(errcode=" + this.getErrcode() + ", errmsg=" + this.getErrmsg() + ", data=" + this
                .getData() + ")";
    }

}