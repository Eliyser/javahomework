package com.haien.myexception;

/**
 * @Author haien
 * @Description 非文本文件异常
 * @Date 2018/11/21
 **/
public class InvalidFileException extends Exception {
    public InvalidFileException() {
    }

    public InvalidFileException(String message) {
        super(message);
    }

    public InvalidFileException(String message, Throwable cause) {
        super(message, cause);
    }
}

