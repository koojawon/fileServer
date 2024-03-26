package com.ai.FlatServer.file.controller;

import com.ai.FlatServer.file.dto.response.FileDto;
import com.ai.FlatServer.file.dto.response.FileNameInfo;
import com.ai.FlatServer.file.dto.request.FilePatchRequest;
import com.ai.FlatServer.file.dto.request.PdfUploadRequest;
import com.ai.FlatServer.file.service.FileService;
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

    private final FileService fileService;

    @GetMapping("/mxl/{fileId}")
    public ResponseEntity<UrlResource> getMxl(@PathVariable Long fileId) {
        try {
            FileDto xmlFileDto = fileService.getMxl(fileId);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                            .filename(xmlFileDto.getEncodedFileName())
                            .build()
                            .toString())
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(xmlFileDto.getFile());
        } catch (NoSuchElementException e) {
            return ResponseEntity.badRequest().build();
        } catch (MalformedURLException | RuntimeException e) {
            return ResponseEntity.internalServerError().build();
        }
    }


    @GetMapping("/pdf/{fileUid}")
    public ResponseEntity<UrlResource> getPdf(@PathVariable String fileUid) {
        try {
            FileDto fileDto = fileService.getPdf(fileUid);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                            .filename(fileDto.getEncodedFileName() + ".pdf")
                            .build()
                            .toString())
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(fileDto.getFile());
        } catch (NoSuchElementException e) {
            return ResponseEntity.badRequest().build();
        } catch (MalformedURLException | RuntimeException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/fav")
    public ResponseEntity<List<FileNameInfo>> getFavs() {
        try {
            return ResponseEntity.ok(fileService.getFavs());
        } catch (RuntimeException e) {
            return ResponseEntity.internalServerError().build();
        }
    }


    @PostMapping
    public ResponseEntity<ArrayList<Long>> uploadPdf(
            @RequestPart(value = "dto", required = false) PdfUploadRequest pdfUploadRequest,
            @RequestPart(value = "files") List<MultipartFile> multipartFile) {
        ArrayList<Long> Ids = new ArrayList<>();
        try {
            for (MultipartFile m : multipartFile) {
                Ids.add(fileService.saveFile(m, pdfUploadRequest));
            }
        } catch (UnsupportedEncodingException | NoSuchElementException e) {
            return ResponseEntity.badRequest().build();
        } catch (IOException | RuntimeException e) {
            return ResponseEntity.internalServerError().build();
        }
        return ResponseEntity.status(201).body(Ids);
    }

    @DeleteMapping("/{targetFileId}")
    public ResponseEntity<Boolean> deleteFile(@PathVariable Long targetFileId) {
        try {
            fileService.removeFile(targetFileId);
            return ResponseEntity.ok().build();
        } catch (NoSuchElementException e) {
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException e) {
            return ResponseEntity.internalServerError().build();
        }

    }


    @PatchMapping("/{targetFileId}")
    public ResponseEntity<Boolean> patchFile(@PathVariable Long targetFileId,
                                             @RequestBody FilePatchRequest filePatchRequest) {
        try {
            fileService.patchFile(targetFileId, filePatchRequest);
            return ResponseEntity.ok().build();
        } catch (NoSuchElementException e) {
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/list")
    public ResponseEntity<List<FileNameInfo>> getFileList() {
        List<FileNameInfo> fileList = fileService.getAllFilesInfo();
        return ResponseEntity.ok(fileList);
    }
}
