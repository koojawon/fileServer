package com.ai.FlatServer.domain.file.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.UrlResource;

@Data
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
public class FileDto {

    private UrlResource file;
    private String encodedFileName;
}
