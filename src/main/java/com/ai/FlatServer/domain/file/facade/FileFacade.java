package com.ai.FlatServer.domain.file.facade;

import com.ai.FlatServer.domain.file.dto.request.FilePatchRequest;
import com.ai.FlatServer.domain.file.dto.request.PdfUploadRequest;
import com.ai.FlatServer.domain.file.dto.response.FileDto;
import com.ai.FlatServer.domain.file.dto.response.FileNameInfo;
import com.ai.FlatServer.domain.file.respository.dao.FileInfo;
import com.ai.FlatServer.domain.file.service.FileService;
import com.ai.FlatServer.domain.folder.service.FolderService;
import com.ai.FlatServer.domain.user.enums.Role;
import com.ai.FlatServer.domain.user.repository.entity.User;
import com.ai.FlatServer.domain.user.service.UserService;
import com.ai.FlatServer.global.exceptions.FlatErrorCode;
import com.ai.FlatServer.global.exceptions.FlatException;
import com.ai.FlatServer.global.rabbitmq.service.MessageService;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor
@Service
public class FileFacade {

    private final FileService fileService;
    private final FolderService folderService;
    private final UserService userService;
    private final MessageService messageService;

    public FileDto getMxl(Long fileId) throws MalformedURLException {
        User user = userService.getCurrentUser();
        fileService.checkFileAuthority(user, fileId);
        return fileService.getMxl(fileId);
    }

    public FileDto getPdf(String fileUid) throws MalformedURLException {
        User user = userService.getCurrentUser();
        if (!user.getRole().equals(Role.ADMIN)) {
            throw new FlatException(FlatErrorCode.NO_AUTHORITY);
        }
        FileInfo fileInfo = fileService.getPdf(fileUid);
        return fileService.encodePdf(fileInfo);
    }

    public List<FileNameInfo> getFavs() {
        User user = userService.getCurrentUser();
        return fileService.getFavs(user);
    }

    public void uploadPdf(PdfUploadRequest pdfUploadRequest, MultipartFile multipartFile) throws IOException {
        User user = userService.getCurrentUser();
        folderService.checkFolderAuthority(user, pdfUploadRequest.getFolderId());
        String s = fileService.savePdf(multipartFile, pdfUploadRequest);
        messageService.sendTransformRequestMessage(s);
    }

    public void uploadMxl(MultipartFile multipartFile) throws IOException {
        if (!userService.getCurrentUser().getRole().equals(Role.ADMIN)) {
            throw new FlatException(FlatErrorCode.NO_AUTHORITY);
        }
        fileService.saveMxl(multipartFile);
    }

    public void deleteFile(Long targetFileId) {
        User user = userService.getCurrentUser();
        fileService.checkFileAuthority(user, targetFileId);
        fileService.removeFile(targetFileId);
    }

    public void patchFile(Long targetFileId, FilePatchRequest filePatchRequest) {
        User user = userService.getCurrentUser();
        fileService.checkFileAuthority(user, targetFileId);
        fileService.patchFile(targetFileId, filePatchRequest);
    }

    public List<FileNameInfo> getFileList() {
        User user = userService.getCurrentUser();
        return fileService.getAllFilesInfo(user);
    }
}