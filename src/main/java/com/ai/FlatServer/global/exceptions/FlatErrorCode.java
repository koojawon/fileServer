package com.ai.FlatServer.global.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum FlatErrorCode {

    NO_SUCH_FOLDER_ID(HttpStatus.BAD_REQUEST.value(), "해당 ID의 폴더가 존재하지 않습니다."),
    NO_SUCH_FILE_ID(HttpStatus.BAD_REQUEST.value(), "해당 ID의 파일이 존재하지 않습니다."),
    NO_SUCH_FILE_UID(HttpStatus.BAD_REQUEST.value(), "해당 UID의 파일이 존재하지 않습니다."),
    MXL_NOT_READY(HttpStatus.BAD_REQUEST.value(), "해당 PDF의 MXL 파일이 존재하지 않습니다."),
    UNSUPPORTED_EXTENSION(HttpStatus.BAD_REQUEST.value(), "지원하지 않는 확장자입니다."),
    NO_AUTHORITY(HttpStatus.FORBIDDEN.value(), "권한이 없습니다."),
    DUPLICATED_EMAIL(HttpStatus.BAD_REQUEST.value(), "이메일이 중복됩니다."),
    FOLDER_CREATION_LIMIT(HttpStatus.BAD_REQUEST.value(), "폴더 갯수의 상한에 도달했습니다.");
    private final HttpStatusCode statusCode;
    private final String statusMessage;

    FlatErrorCode(int scBadRequest, String s) {
        statusCode = HttpStatusCode.valueOf(scBadRequest);
        statusMessage = s;
    }
}
