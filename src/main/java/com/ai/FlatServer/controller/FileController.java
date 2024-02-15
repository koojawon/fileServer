package com.ai.FlatServer.controller;

import com.ai.FlatServer.domain.dto.ResponseFile;
import com.ai.FlatServer.domain.dto.file.FileDto;
import com.ai.FlatServer.service.FileService;
import jakarta.servlet.http.HttpServletRequest;
import java.net.MalformedURLException;
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

@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
@Slf4j
public class FileController {

    private final FileService fileService;

    @GetMapping("/pdf")
    public ResponseEntity<UrlResource> getPdf(@RequestParam String fileUid) throws MalformedURLException {
        FileDto fileDto = fileService.getPdf(fileUid);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename(fileDto.getEncodedFileName() + ".pdf")
                        .build()
                        .toString())
                .contentType(MediaType.APPLICATION_PDF)
                .body(fileDto.getFile());
    }

    @GetMapping("/list")
    public ResponseEntity<List<ResponseFile>> getAllPdf() {
        return ResponseEntity.ok().body(fileService.getAllFiles());
    }

    @GetMapping
    public ResponseEntity<UrlResource> getMxl(@RequestParam String uid) throws MalformedURLException {
        FileDto xmlFileDto;
        try {
            xmlFileDto = fileService.getXml(uid);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                            .filename(xmlFileDto.getEncodedFileName() + ".mxl")
                            .build()
                            .toString())
                    .body(xmlFileDto.getFile());
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(404).build();
        }
    }

    @PostMapping
    public ResponseEntity<String> uploadFile(HttpServletRequest httpRequest,
                                             @RequestPart(value = "multipartFile") List<MultipartFile> multipartFile) {
        try {
            for (MultipartFile m : multipartFile) {
                fileService.saveFile(m);
            }
        } catch (Exception e) {
            return ResponseEntity.status(400).build();
        }
        return ResponseEntity.status(201).build();
    }
}
