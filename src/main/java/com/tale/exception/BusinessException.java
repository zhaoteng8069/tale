package com.tale.exception;

/**
 * @ClassName BusinessException
 * @Desc 自定义业务异常
 * @Author zhaoteng
 * @Date 2020/4/11 17:47
 * @Version 1.0
 **/
public class BusinessException extends RuntimeException{

    private static final long serialVersionUID = 1L;

    private int errCode;

    /**
     * 无参构造器
     */
    public BusinessException() {

    }

    public BusinessException(String errMsg) {
        super(errMsg);
    }

    public int getErrCode() {
        return errCode;
    }

    public void setErrCode(int errCode) {
        this.errCode = errCode;
    }


}
