package com.ai.FlatServer.domain.file.controller;

import com.ai.FlatServer.domain.file.dto.request.FilePatchRequest;
import com.ai.FlatServer.domain.file.dto.request.PdfUploadRequest;
import com.ai.FlatServer.domain.file.dto.response.FileDto;
import com.ai.FlatServer.domain.file.dto.response.FileNameInfo;
import com.ai.FlatServer.domain.file.facade.FileFacade;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.UrlResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/file")
@RequiredArgsConstructor
@Slf4j
public class FileController {

    private final FileFacade facade;

    @GetMapping("/mxl/{fileId}")
    public ResponseEntity<UrlResource> getMxl(@PathVariable Long fileId) throws MalformedURLException {
        FileDto fileDto = facade.getMxl(fileId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename(fileDto.getEncodedFileName())
                        .build()
                        .toString())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(fileDto.getFile());
    }


    @GetMapping("/pdf/{fileUid}")
    public ResponseEntity<UrlResource> getPdf(@PathVariable String fileUid) throws MalformedURLException {
        FileDto fileDto = facade.getPdf(fileUid);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename(fileDto.getEncodedFileName() + ".pdf")
                        .build()
                        .toString())
                .contentType(MediaType.APPLICATION_PDF)
                .body(fileDto.getFile());
    }

    @GetMapping("/fav")
    public ResponseEntity<List<FileNameInfo>> getFavs() {
        return ResponseEntity.ok(facade.getFavs());
    }


    @PostMapping("/pdf")
    public ResponseEntity<ArrayList<Long>> uploadPdf(
            @RequestPart(value = "dto") PdfUploadRequest pdfUploadRequest,
            @RequestPart(value = "file") MultipartFile multipartFile) throws IOException {
        facade.uploadPdf(pdfUploadRequest, multipartFile);
        return ResponseEntity.created(URI.create("/folder/" + pdfUploadRequest.getFolderId())).build();
    }

    @PostMapping("/mxl")
    public ResponseEntity<Boolean> uploadMxl(
            @RequestPart(value = "file") MultipartFile multipartFile) throws IOException {
        facade.uploadMxl(multipartFile);
        return ResponseEntity.status(201).build();
    }

    @DeleteMapping("/{targetFileId}")
    public ResponseEntity<Boolean> deleteFile(@PathVariable Long targetFileId) {
        facade.deleteFile(targetFileId);
        return ResponseEntity.ok().build();
    }


    @PatchMapping("/{targetFileId}")
    public ResponseEntity<Boolean> patchFile(@PathVariable Long targetFileId,
                                             @RequestBody FilePatchRequest filePatchRequest) {
        facade.patchFile(targetFileId, filePatchRequest);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/list")
    @Deprecated
    public ResponseEntity<List<FileNameInfo>> getFileList() {
        return ResponseEntity.ok(facade.getFileList());
    }
}
