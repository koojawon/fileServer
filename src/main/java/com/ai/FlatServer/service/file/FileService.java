package com.ai.FlatServer.service.file;

import com.ai.FlatServer.domain.dao.FileInfoDao;
import com.ai.FlatServer.domain.dto.ResponseFile;
import com.ai.FlatServer.domain.dto.file.FileDto;
import com.ai.FlatServer.domain.mapper.FileMapper;
import com.ai.FlatServer.repository.FileInfoRepository;
import com.ai.FlatServer.service.MessageService;
import com.google.gson.JsonObject;
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
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriUtils;

@Service
@Slf4j
@RequiredArgsConstructor
public class FileService {

    private final FileInfoRepository fileInfoRepository;
    private final MessageService messageService;
    @Value("${uploadPath}")
    private String uploadPath;

    @PostConstruct
    public void init() {
        File file = new File(uploadPath.substring(0, uploadPath.length() - 1));
        if (!file.exists()) {
            if (file.mkdirs()) {
                String osName = System.getProperty("os.name").toLowerCase();
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

    public Long saveFile(@NotNull MultipartFile multipartFile) throws IOException {
        String originalFileName = multipartFile.getOriginalFilename();
        log.info("received file : " + originalFileName);

        switch (getExt(Objects.requireNonNull(originalFileName))) {
            case "pdf" -> {
                return savePdf(multipartFile);
            }
            case "mxl" -> saveMxl(multipartFile);
            default -> throw new UnsupportedEncodingException();
        }
        return -1L;
    }

    @Transactional
    private Long savePdf(@NotNull MultipartFile multipartFile) throws IOException {
        String originalFileName = Normalizer.normalize(Objects.requireNonNull(multipartFile.getOriginalFilename()),
                Form.NFC);
        String fileUid = UUID.randomUUID().toString();
        String ext = getExt(Objects.requireNonNull(originalFileName));
        multipartFile.transferTo(new File(getFullPath(fileUid + "." + ext)));
        FileInfoDao fileInfoDao = FileInfoDao.builder()
                .originalFileName(originalFileName)
                .uid(fileUid)
                .mxlPresent(false)
                .build();
        fileInfoRepository.saveAndFlush(fileInfoDao);

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("id", "File");
        jsonObject.addProperty("fileUid", fileUid);

        messageService.sendMessage(jsonObject);

        FileInfoDao savedFileInfoDao = fileInfoRepository.findByUid(fileUid).orElseThrow();
        log.info("File saved : " + savedFileInfoDao);
        return savedFileInfoDao.getId();
    }


    public boolean getMxlState(Long id) {
        return fileInfoRepository.findById(id).orElseThrow(NoSuchElementException::new).isMxlPresent();
    }

    @Transactional
    private void saveMxl(@NotNull MultipartFile multipartFile) throws IOException {
        String originalFileName = multipartFile.getOriginalFilename();

        multipartFile.transferTo(new File(getFullPath(originalFileName)));
        FileInfoDao fileInfoDao = fileInfoRepository.findByUid(
                        Objects.requireNonNull(originalFileName).substring(0, originalFileName.lastIndexOf(".")))
                .orElseThrow(NoSuchElementException::new);
        fileInfoDao.setMxlPresent(true);
        fileInfoRepository.save(fileInfoDao);
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
        FileInfoDao fileInfoDao = fileInfoRepository.findByUid(fileId).orElseThrow(NoSuchElementException::new);
        UrlResource urlResource = new UrlResource("file:" + uploadPath + fileInfoDao.getUid() + ".pdf");
        String encodedFileName = UriUtils.encode(fileInfoDao.getUid(), StandardCharsets.UTF_8);
        return FileDto.builder()
                .file(urlResource)
                .encodedFileName(encodedFileName)
                .build();
    }

    public FileDto getMxl(Long id) throws MalformedURLException {
        FileInfoDao fileInfoDao = fileInfoRepository
                .findById(id)
                .orElseThrow(NoSuchElementException::new);

        if (fileInfoDao.isMxlPresent()) {
            UrlResource urlResource = new UrlResource("file:" + uploadPath + fileInfoDao.getUid() + ".mxl");
            String encodedFileName = UriUtils.encode(subExt(fileInfoDao.getOriginalFileName()) + ".mxl",
                    StandardCharsets.UTF_8);
            return FileDto.builder()
                    .file(urlResource)
                    .encodedFileName(encodedFileName)
                    .build();
        }
        throw new NoSuchElementException();
    }

    public List<ResponseFile> getAllFiles() {
        List<FileInfoDao> fileDtoList = fileInfoRepository.findAll();
        return fileDtoList.stream().map(FileMapper::FileDaoToResponseFileMapper).toList();
    }
}
