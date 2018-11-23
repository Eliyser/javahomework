package com.haien.myexception;

/**
 * @Author haien
 * @Description 非文件异常
 * @Date 2018/11/20
 **/
public class IsDirectoryException extends Exception{

    public IsDirectoryException() {
    }

    public IsDirectoryException(String message) {
        super(message);
    }

    public IsDirectoryException(String message, Throwable cause) {
        super(message, cause);
    }
}
