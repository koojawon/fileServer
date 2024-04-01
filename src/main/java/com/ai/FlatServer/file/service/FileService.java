package com.ai.FlatServer.file.service;

import com.ai.FlatServer.file.dto.request.FilePatchRequest;
import com.ai.FlatServer.file.dto.request.PdfUploadRequest;
import com.ai.FlatServer.file.dto.response.FileDto;
import com.ai.FlatServer.file.dto.response.FileNameInfo;
import com.ai.FlatServer.file.respository.FileInfoRepository;
import com.ai.FlatServer.file.respository.dao.FileInfo;
import com.ai.FlatServer.folder.dto.mapper.FolderMapper;
import com.ai.FlatServer.folder.repository.FolderRepository;
import com.ai.FlatServer.folder.repository.entity.Folder;
import com.ai.FlatServer.rabbitmq.service.MessageService;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriUtils;

@Service
@Slf4j
@RequiredArgsConstructor
public class FileService {

    private final FileInfoRepository fileInfoRepository;
    private final FolderRepository folderRepository;
    private final MessageService messageService;

    private final CacheManager cacheManager;
    @Value("${linuxUploadPath}")
    private String linuxUploadPath;
    @Value("${windowUploadPath}")
    private String winUploadPath;
    private String uploadPath;

    @PostConstruct
    public void init() {
        String osName = System.getProperty("os.name").toLowerCase();
        if (osName.contains("linux")) {
            uploadPath = linuxUploadPath;
        } else {
            uploadPath = winUploadPath;
        }

        java.io.File file = new java.io.File(uploadPath.substring(0, uploadPath.length() - 1));
        if (!file.exists()) {
            if (file.mkdirs()) {

                if (osName.contains("linux") && !(file.setExecutable(true)
                        && file.setReadable(true) && file.setWritable(true))) {
                    log.error("Directory Authority Set Failed!!");
                }
            } else {
                log.error("Directory creation failed!! : ");
            }
        } else {
            log.info("Directory exists...");
        }
    }

    public Long saveFile(@NotNull MultipartFile multipartFile, PdfUploadRequest pdfUploadRequest) throws IOException {
        String originalFileName = multipartFile.getOriginalFilename();
        log.info("received file : " + originalFileName);

        switch (getExt(Objects.requireNonNull(originalFileName))) {
            case "pdf" -> {
                try {
                    String uid = savePdf(multipartFile, pdfUploadRequest);
                    Long id = fileInfoRepository.findByUid(uid).orElseThrow(NoSuchElementException::new).getId();
                    messageService.sendTransformRequestMessage(uid);
                    return id;
                } catch (Exception e) {
                    throw new IOException();
                }
            }
            case "mxl" -> saveMxl(multipartFile);
            default -> throw new UnsupportedEncodingException();
        }
        return -1L;
    }

    @Transactional
    private String savePdf(@NotNull MultipartFile multipartFile, PdfUploadRequest pdfUploadRequest) throws IOException {
        String originalFileName = Normalizer.normalize(Objects.requireNonNull(multipartFile.getOriginalFilename()),
                Form.NFC);
        String fileUid = UUID.randomUUID().toString();
        String ext = getExt(Objects.requireNonNull(originalFileName));
        multipartFile.transferTo(new java.io.File(getFullPath(fileUid + "." + ext)));
        FileInfo fileInfo = FileInfo.builder()
                .originalFileName(originalFileName)
                .uid(fileUid)
                .mxlPresent(false)
                .build();
        fileInfoRepository.save(fileInfo);
        addPdfToParentFolder(pdfUploadRequest.getFolderId(), fileInfo);
        return fileUid;
    }


    private void addPdfToParentFolder(Long fileId, FileInfo fileInfo) {
        Folder parent = folderRepository.findById(fileId).orElseThrow(NoSuchElementException::new);
        fileInfo.setParentFolderId(parent.getId());
    }

    @Transactional
    private void saveMxl(@NotNull MultipartFile multipartFile) throws IOException {
        String originalFileName = multipartFile.getOriginalFilename();
        multipartFile.transferTo(new java.io.File(getFullPath(originalFileName)));
        FileInfo fileInfo = fileInfoRepository.findByUid(
                        Objects.requireNonNull(originalFileName).substring(0, originalFileName.lastIndexOf(".")))
                .orElseThrow(NoSuchElementException::new);
        fileInfo.setMxlPresent(true);
    }

    private String getExt(String originalFileName) {
        return originalFileName.substring(originalFileName.lastIndexOf(".") + 1);
    }

    private String subExt(String originalFileName) {
        return originalFileName.substring(0, originalFileName.lastIndexOf("."));
    }

    private String getFullPath(String fileName) {
        return uploadPath + fileName;
    }

    public FileDto getPdf(String fileId) throws MalformedURLException {
        FileInfo fileInfo = fileInfoRepository.findByUid(fileId).orElseThrow(NoSuchElementException::new);
        UrlResource urlResource = new UrlResource("file:" + uploadPath + fileInfo.getUid() + ".pdf");
        String encodedFileName = UriUtils.encode(fileInfo.getUid(), StandardCharsets.UTF_8);
        return FileDto.builder()
                .file(urlResource)
                .encodedFileName(encodedFileName)
                .build();
    }

    public FileDto getMxl(Long id) throws MalformedURLException {
        FileInfo fileInfo = fileInfoRepository
                .findById(id)
                .orElseThrow(NoSuchElementException::new);

        if (fileInfo.isMxlPresent()) {
            UrlResource urlResource = new UrlResource("file:" + uploadPath + fileInfo.getUid() + ".mxl");
            String encodedFileName = UriUtils.encode(subExt(fileInfo.getOriginalFileName()) + ".mxl",
                    StandardCharsets.UTF_8);
            return FileDto.builder()
                    .file(urlResource)
                    .encodedFileName(encodedFileName)
                    .build();
        }
        throw new NoSuchElementException();
    }

    @Cacheable(value = "fileCache", key = "'all'")
    public List<FileNameInfo> getFavs() {
        return fileInfoRepository.findAllByFav(true)
                .stream()
                .map(FolderMapper::FileInfoToFileNameInfoMapper)
                .toList();
    }

    @Transactional
    public void removeFile(Long fileId) {
        FileInfo fileInfo = fileInfoRepository.findById(fileId).orElseThrow(NoSuchElementException::new);
        if (fileInfo.getFav()) {
            Objects.requireNonNull(cacheManager.getCache("fileCache")).evict("all");
        }
        File pdf = new File(fileInfo.getUid() + ".pdf");
        if (pdf.exists()) {
            if (!pdf.delete()) {
                throw new RuntimeException("삭제 실패");
            }
            Long parentFolderId = fileInfo.getParentFolderId();
            Objects.requireNonNull(cacheManager.getCache("folderCache")).evict(parentFolderId);

        }
        if (fileInfo.isMxlPresent()) {
            File mxl = new File(fileInfo.getUid() + ".mxl");
            if (mxl.exists()) {
                if (!mxl.delete()) {
                    throw new RuntimeException("삭제 실패");
                }
            }
        }
        fileInfoRepository.delete(fileInfo);
    }

    @Transactional
    public void patchFile(Long fileId, FilePatchRequest patchRequest) {
        FileInfo fileInfo = fileInfoRepository.findById(fileId).orElseThrow(NoSuchElementException::new);
        Folder parent = folderRepository.findById(fileInfo.getParentFolderId())
                .orElseThrow(NoSuchElementException::new);
        Objects.requireNonNull(cacheManager.getCache("folderCache")).evict(parent.getId());
        if (patchRequest.getIconId() != null) {
            fileInfo.setIconId(patchRequest.getIconId());
        }
        if (patchRequest.getNewFolderId() != null) {
            Folder newParent = folderRepository.findById(patchRequest.getNewFolderId())
                    .orElseThrow(NoSuchElementException::new);
            Objects.requireNonNull(cacheManager.getCache("folderCache")).evict(newParent.getId());
            fileInfo.setParentFolderId(newParent.getId());
        }
        if (patchRequest.getIsFav() != null) {
            fileInfo.setFav(patchRequest.getIsFav());
            Objects.requireNonNull(cacheManager.getCache("folderCache")).evict("all");
        }
    }

    public List<FileNameInfo> getAllFilesInfo() {
        List<FileNameInfo> fileList = new ArrayList<>();
        for (FileInfo f : fileInfoRepository.findAll()) {
            fileList.add(FolderMapper.FileInfoToFileNameInfoMapper(f));
        }
        return fileList;
    }
}
