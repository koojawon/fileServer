package com.ai.FlatServer.domain.file.service;

import com.ai.FlatServer.domain.file.dto.request.FilePatchRequest;
import com.ai.FlatServer.domain.file.dto.request.PdfUploadRequest;
import com.ai.FlatServer.domain.file.dto.response.FileDto;
import com.ai.FlatServer.domain.file.dto.response.FileNameInfo;
import com.ai.FlatServer.domain.file.respository.FileInfoRepository;
import com.ai.FlatServer.domain.file.respository.dao.FileInfo;
import com.ai.FlatServer.domain.folder.dto.mapper.FolderMapper;
import com.ai.FlatServer.domain.folder.repository.FolderRepository;
import com.ai.FlatServer.domain.folder.repository.entity.Folder;
import com.ai.FlatServer.domain.user.enums.Role;
import com.ai.FlatServer.domain.user.repository.entity.User;
import com.ai.FlatServer.global.exceptions.FlatErrorCode;
import com.ai.FlatServer.global.exceptions.FlatException;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriUtils;

@Service
@Slf4j
@RequiredArgsConstructor
public class FileService {

    public static String uploadPath;
    private final FileInfoRepository fileInfoRepository;
    private final FolderRepository folderRepository;
    private final CacheManager cacheManager;
    @Value("${linuxUploadPath}")
    private String linuxUploadPath;
    @Value("${windowUploadPath}")
    private String winUploadPath;

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
                    return;
                }
                return;
            }
            log.error("Directory creation failed!! : ");
        }
        log.info("Directory exists...");
    }

    @Transactional
    public String savePdf(@NotNull MultipartFile multipartFile, PdfUploadRequest pdfUploadRequest) throws IOException {
        String originalFileName = Normalizer.normalize(Objects.requireNonNull(multipartFile.getOriginalFilename()),
                Form.NFC);
        String fileUid = UUID.randomUUID().toString();
        String ext = getExt(Objects.requireNonNull(originalFileName));

        if (!ext.equals("pdf")) {
            throw new FlatException(FlatErrorCode.UNSUPPORTED_EXTENSION);
        }

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
        Folder parent = folderRepository.findById(fileId)
                .orElseThrow(() -> new FlatException(FlatErrorCode.NO_SUCH_FILE_ID));
        fileInfo.setParentFolderId(parent.getId());
    }

    @Transactional
    public void saveMxl(@NotNull MultipartFile multipartFile) throws IOException {
        String originalFileName = multipartFile.getOriginalFilename();
        String ext = getExt(Objects.requireNonNull(originalFileName));
        if (!ext.equals("mxl")) {
            throw new FlatException(FlatErrorCode.UNSUPPORTED_EXTENSION);
        }

        multipartFile.transferTo(new java.io.File(getFullPath(originalFileName)));

        FileInfo fileInfo = fileInfoRepository.findByUid(
                        Objects.requireNonNull(originalFileName).substring(0, originalFileName.lastIndexOf(".")))
                .orElseThrow(() -> new FlatException(FlatErrorCode.NO_SUCH_FILE_ID));
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

    public FileInfo getPdf(String fileUid) {
        return fileInfoRepository.findByUid(fileUid)
                .orElseThrow(() -> new FlatException(FlatErrorCode.NO_SUCH_FILE_UID));
    }

    public FileDto encodePdf(FileInfo fileInfo) throws MalformedURLException {
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
                .orElseThrow(() -> new FlatException(FlatErrorCode.NO_SUCH_FILE_ID));

        if (fileInfo.isMxlPresent()) {
            UrlResource urlResource = new UrlResource("file:" + uploadPath + fileInfo.getUid() + ".mxl");
            String encodedFileName = UriUtils.encode(subExt(fileInfo.getOriginalFileName()) + ".mxl",
                    StandardCharsets.UTF_8);
            return FileDto.builder()
                    .file(urlResource)
                    .encodedFileName(encodedFileName)
                    .build();
        }
        throw new FlatException(FlatErrorCode.MXL_NOT_READY);
    }

    public List<FileNameInfo> getFavs(User user) {
        return fileInfoRepository.findAllByFavAndOwnerId(true, user.getId())
                .stream()
                .map(FolderMapper::FileInfoToFileNameInfoMapper)
                .toList();
    }

    @Transactional
    public void removeFile(Long fileId) {
        FileInfo fileInfo = fileInfoRepository.findById(fileId)
                .orElseThrow(() -> new FlatException(FlatErrorCode.NO_SUCH_FILE_ID));
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
        FileInfo fileInfo = fileInfoRepository.findById(fileId)
                .orElseThrow(() -> new FlatException(FlatErrorCode.NO_SUCH_FILE_ID));
        Folder parent = folderRepository.findById(fileInfo.getParentFolderId())
                .orElseThrow(() -> new FlatException(FlatErrorCode.NO_SUCH_FILE_ID));
        Objects.requireNonNull(cacheManager.getCache("folderCache")).evict("folderInfoCache" + parent.getId());
        if (patchRequest.getIconId() != null) {
            fileInfo.setIconId(patchRequest.getIconId());
        }
        if (patchRequest.getNewFolderId() != null) {
            Folder newParent = folderRepository.findById(patchRequest.getNewFolderId())
                    .orElseThrow(() -> new FlatException(FlatErrorCode.NO_SUCH_FILE_ID));
            Objects.requireNonNull(cacheManager.getCache("folderCache")).evict("folderInfoCache" + newParent.getId());
            fileInfo.setParentFolderId(newParent.getId());
        }
        if (patchRequest.getIsFav() != null) {
            fileInfo.setFav(patchRequest.getIsFav());
        }
    }

    public List<FileNameInfo> getAllFilesInfo(User user) {
        List<FileNameInfo> fileList = new ArrayList<>();
        for (FileInfo f : fileInfoRepository.findByOwnerId(user.getId())) {
            fileList.add(FolderMapper.FileInfoToFileNameInfoMapper(f));
        }
        return fileList;
    }


    public void checkFileAuthority(User user, Long targetFileId) {
        FileInfo fileInfo = fileInfoRepository.findById(targetFileId)
                .orElseThrow(() -> new FlatException(FlatErrorCode.NO_SUCH_FILE_ID));
        if (fileInfo.getOwnerId() == null || (!fileInfo.getOwnerId().equals(user.getId()) && !user.getRole()
                .equals(Role.ADMIN))) {
            throw new FlatException(FlatErrorCode.NO_AUTHORITY);
        }
    }

}
