package com.ai.FlatServer.service;

import com.ai.FlatServer.domain.dao.FileInfoDao;
import com.ai.FlatServer.domain.dto.ResponseFile;
import com.ai.FlatServer.domain.dto.file.FileDto;
import com.ai.FlatServer.domain.dto.message.RequestMessageDto;
import com.ai.FlatServer.domain.mapper.FileMapper;
import com.ai.FlatServer.repository.FileInfoRepository;
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
        File file = new File(uploadPath);
        if (file.mkdirs()) {
            if (!(file.setExecutable(true) && file.setReadable(true) && file.setWritable(true))) {
                log.error("Directory Authority Set Failed!!");
            }
        } else {
            log.error("Directory creation failed!!");
        }
    }

    public void saveFile(@NotNull MultipartFile multipartFile) throws IOException {
        String originalFileName = multipartFile.getOriginalFilename();
        log.info("received file : " + originalFileName);

        switch (getExt(Objects.requireNonNull(originalFileName))) {
            case "pdf" -> savePdf(multipartFile);
            case "json" -> saveJson(multipartFile);
            case "mxl" -> saveXml(multipartFile);
            default -> throw new UnsupportedEncodingException();
        }
    }

    @Transactional
    private void savePdf(@NotNull MultipartFile multipartFile) throws IOException {
        String originalFileName = Normalizer.normalize(Objects.requireNonNull(multipartFile.getOriginalFilename()),
                Form.NFC);
        String fileUid = UUID.randomUUID().toString();
        String ext = getExt(Objects.requireNonNull(originalFileName));

        log.info(String.valueOf(multipartFile.getSize()));
        log.info(getFullPath(fileUid + "." + ext));

        multipartFile.transferTo(new File(getFullPath(fileUid + "." + ext)));

        FileInfoDao fileInfoDao = FileInfoDao.builder()
                .originalFileName(originalFileName)
                .uid(fileUid)
                .jsonPresent(false)
                .xmlPresent(false)
                .build();
        log.info(fileInfoDao.toString());
        fileInfoRepository.save(fileInfoDao);
        messageService.sendRequestMessage(RequestMessageDto.builder().fileUid(fileUid).build());
    }

    @Transactional
    private void saveJson(@NotNull MultipartFile multipartFile) throws IOException {
        String originalFileName = multipartFile.getOriginalFilename();
        multipartFile.transferTo(new File(getFullPath(originalFileName)));
        FileInfoDao fileInfoDao = fileInfoRepository.findByUid(
                        Objects.requireNonNull(originalFileName).substring(0, originalFileName.lastIndexOf(".")))
                .orElseThrow(NoSuchElementException::new);
        fileInfoDao.setJsonPresent(true);
        fileInfoRepository.save(fileInfoDao);
    }

    @Transactional
    private void saveXml(@NotNull MultipartFile multipartFile) throws IOException {
        String originalFileName = multipartFile.getOriginalFilename();

        multipartFile.transferTo(new File(getFullPath(originalFileName)));
        FileInfoDao fileInfoDao = fileInfoRepository.findByUid(
                        Objects.requireNonNull(originalFileName).substring(0, originalFileName.lastIndexOf(".")))
                .orElseThrow(NoSuchElementException::new);
        fileInfoDao.setXmlPresent(true);
        fileInfoRepository.save(fileInfoDao);
    }

    private String getExt(String originalFileName) {
        return originalFileName.substring(originalFileName.lastIndexOf(".") + 1);
    }

    private String getFullPath(String fileName) {
        log.info(uploadPath);
        return uploadPath + fileName;
    }

    public FileDto getPdf(String fileUid) throws MalformedURLException {
        FileInfoDao fileInfoDao = fileInfoRepository.findByUid(fileUid).orElseThrow(NoSuchElementException::new);
        UrlResource urlResource = new UrlResource("file:" + uploadPath + fileInfoDao.getUid() + ".pdf");
        String encodedFileName = UriUtils.encode(fileInfoDao.getUid(), StandardCharsets.UTF_8);
        return FileDto.builder()
                .file(urlResource)
                .encodedFileName(encodedFileName)
                .build();
    }

    public FileDto getXml(String uid) throws MalformedURLException {
        if (fileInfoRepository
                .findByUid(uid)
                .orElseThrow(NoSuchElementException::new)
                .isXmlPresent()) {
            UrlResource urlResource = new UrlResource("file:" + uploadPath + uid + ".mxl");
            String encodedFileName = UriUtils.encode(uid, StandardCharsets.UTF_8);

            return FileDto.builder()
                    .file(urlResource)
                    .encodedFileName(encodedFileName)
                    .build();
        }
        throw new NoSuchElementException();
    }

    public FileDto getJson(String uid) throws MalformedURLException, NoSuchElementException {
        if (fileInfoRepository
                .findByUid(uid)
                .orElseThrow(NoSuchElementException::new)
                .isJsonPresent()) {
            UrlResource urlResource = new UrlResource("file:" + uploadPath + uid + ".json");
            String encodedFileName = UriUtils.encode(uid, StandardCharsets.UTF_8);

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
