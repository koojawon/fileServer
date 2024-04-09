package com.ai.FlatServer.file.controller;

import com.ai.FlatServer.file.dto.request.FilePatchRequest;
import com.ai.FlatServer.file.dto.request.PdfUploadRequest;
import com.ai.FlatServer.file.dto.response.FileDto;
import com.ai.FlatServer.file.dto.response.FileNameInfo;
import com.ai.FlatServer.file.respository.dao.FileInfo;
import com.ai.FlatServer.file.service.FileService;
import com.ai.FlatServer.folder.service.FolderService;
import com.ai.FlatServer.rabbitmq.service.MessageService;
import com.ai.FlatServer.user.repository.entity.User;
import com.ai.FlatServer.user.service.UserService;
import java.io.IOException;
import java.net.MalformedURLException;
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

    private final FileService fileService;
    private final FolderService folderService;
    private final UserService userService;
    private final MessageService messageService;

    @GetMapping("/mxl/{fileId}")
    public ResponseEntity<UrlResource> getMxl(@PathVariable Long fileId) throws MalformedURLException {
        FileDto xmlFileDto = fileService.getMxl(fileId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename(xmlFileDto.getEncodedFileName())
                        .build()
                        .toString())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(xmlFileDto.getFile());
    }


    @GetMapping("/pdf/{fileUid}")
    public ResponseEntity<UrlResource> getPdf(@PathVariable String fileUid) throws MalformedURLException {
        FileInfo fileInfo = fileService.getPdf(fileUid);
        userService.checkFileAuthority(fileInfo);
        FileDto fileDto = fileService.encodePdf(fileInfo);
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
        User user = userService.getCurrentUser();
        return ResponseEntity.ok(fileService.getFavs(user));
    }


    @PostMapping("/pdf")
    public ResponseEntity<ArrayList<Long>> uploadPdf(
            @RequestPart(value = "dto") PdfUploadRequest pdfUploadRequest,
            @RequestPart(value = "file") MultipartFile multipartFile) throws IOException {
        User user = userService.getCurrentUser();
        folderService.checkFolderAuthority(user, pdfUploadRequest.getFolderId());

        String s = fileService.savePdf(multipartFile, pdfUploadRequest);
        messageService.sendTransformRequestMessage(s);
        return ResponseEntity.status(201).build();
    }

    @PostMapping("/mxl")
    public ResponseEntity<Boolean> uploadMxl(
            @RequestPart(value = "file") MultipartFile multipartFile) throws IOException {
        fileService.saveMxl(multipartFile);
        return ResponseEntity.status(201).build();
    }

    @DeleteMapping("/{targetFileId}")
    public ResponseEntity<Boolean> deleteFile(@PathVariable Long targetFileId) {
        User user = userService.getCurrentUser();
        fileService.checkFileAuthority(user, targetFileId);

        fileService.removeFile(targetFileId);
        return ResponseEntity.ok().build();
    }


    @PatchMapping("/{targetFileId}")
    public ResponseEntity<Boolean> patchFile(@PathVariable Long targetFileId,
                                             @RequestBody FilePatchRequest filePatchRequest) {
        User user = userService.getCurrentUser();
        fileService.checkFileAuthority(user, targetFileId);

        fileService.patchFile(targetFileId, filePatchRequest);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/list")
    public ResponseEntity<List<FileNameInfo>> getFileList() {
        List<FileNameInfo> fileList = fileService.getAllFilesInfo();
        return ResponseEntity.ok(fileList);
    }
}
