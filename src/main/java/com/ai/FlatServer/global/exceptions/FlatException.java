package com.ai.FlatServer.global.exceptions;

import lombok.Getter;

@Getter
public class FlatException extends RuntimeException {
    private final FlatErrorCode flatErrorCode;

    public FlatException(FlatErrorCode flatErrorCode) {
        this.flatErrorCode = flatErrorCode;
    }
}
