package com.ai.FlatServer.controller;

import com.ai.FlatServer.domain.dto.ResponseFile;
import com.ai.FlatServer.domain.dto.file.FileDto;
import com.ai.FlatServer.service.file.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.UrlResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "File", description = "파일 관련 API")
@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
@Slf4j
public class FileController {

    private final FileService fileService;

    @Operation(summary = "특정 파일 요청.", description = "확장자와 파일 id를 통해 파일을 다운로드")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공"),
            @ApiResponse(responseCode = "400", description = "지원하지 않는 확장자")
    })
    @GetMapping
    public ResponseEntity<UrlResource> getFile(
            @Schema(description = "요청할 파일 확장자. mxl과 pdf만 허용됩니다.", requiredMode = RequiredMode.REQUIRED) @RequestParam String fileType,
            @Schema(description = "파일의 id", requiredMode = RequiredMode.REQUIRED) @RequestParam String fileId)
            throws MalformedURLException {
        switch (fileType) {
            case "mxl" -> {
                return getMxl(Long.parseLong(fileId));
            }
            case "pdf" -> {
                return getPdf(fileId);
            }
            default -> {
                return ResponseEntity.badRequest().build();
            }
        }
    }

    private ResponseEntity<UrlResource> getPdf(String fileId)
            throws MalformedURLException {
        if (fileId == null) {
            return ResponseEntity.badRequest().build();
        }
        FileDto fileDto = fileService.getPdf(fileId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename(fileDto.getEncodedFileName() + ".pdf")
                        .build()
                        .toString())
                .contentType(MediaType.APPLICATION_PDF)
                .body(fileDto.getFile());
    }

    private ResponseEntity<UrlResource> getMxl(Long id) throws MalformedURLException {
        FileDto xmlFileDto;
        try {
            xmlFileDto = fileService.getMxl(id);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                            .filename(xmlFileDto.getEncodedFileName() + ".mxl")
                            .build()
                            .toString())
                    .body(xmlFileDto.getFile());
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(403).build();
        }
    }

    @Operation(summary = "모든 PDF의 리스트 요청.", description = "파일의 ArrayList를 반환.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "성공")
    })
    @GetMapping("/list")
    public ResponseEntity<List<ResponseFile>> getAllPdf() {
        return ResponseEntity.ok().body(fileService.getAllFiles());
    }


    @Operation(summary = "파일 업로드.", description = "body의 key:value 형식으로 된 multipartFile:파일 형태를 받아 저장. pdf와 mxl만 저장 가능. 한 번에 여러개도 가능합니다. client가 mxl을 올리지 말것!!")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "성공"),
            @ApiResponse(responseCode = "400", description = "지원하지 않는 파일 확장자"),
            @ApiResponse(responseCode = "500", description = "파일 저장 실패")
    })
    @PostMapping
    public ResponseEntity<ArrayList<Long>> uploadFile(
            @RequestPart(value = "multipartFile") List<MultipartFile> multipartFile) {
        ArrayList<Long> Ids = new ArrayList<>();
        try {
            for (MultipartFile m : multipartFile) {
                Ids.add(fileService.saveFile(m));
            }
        } catch (UnsupportedEncodingException | NoSuchElementException e) {
            return ResponseEntity.badRequest().build();
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
        return ResponseEntity.status(201).body(Ids);
    }
}
