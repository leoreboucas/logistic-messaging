package com.github.leoreboucas.logisticmessaging.infra.exception;

public class BusinessException extends RuntimeException{
    public BusinessException(String message) {
        super(message);
    }
}
