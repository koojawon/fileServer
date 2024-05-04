package com.ai.FlatServer.file;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.ai.FlatServer.domain.file.dto.request.PdfUploadRequest;
import com.ai.FlatServer.domain.file.dto.response.FileDto;
import com.ai.FlatServer.domain.file.respository.FileInfoRepository;
import com.ai.FlatServer.domain.file.respository.dao.FileInfo;
import com.ai.FlatServer.domain.file.service.FileService;
import com.ai.FlatServer.domain.folder.service.FolderService;
import com.ai.FlatServer.global.exceptions.FlatException;
import jakarta.transaction.Transactional;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Slf4j
public class FileTest {

    @Autowired
    private FileService fileService;

    @Autowired
    private FileInfoRepository fileInfoRepository;

    @Autowired
    private FolderService folderService;

    @Nested
    @DisplayName("PDF 저장 관련")
    class pdfSaveTest {

        private String uuid;

        @AfterEach
        public void deleteFile() {
            new File(FileService.uploadPath + uuid + ".pdf").delete();
        }

        @Test
        @DisplayName("PDF 저장 테스트")
        @Transactional
        public void savePdf() throws IOException {
            //given
            MultipartFile multipartFile = new MockMultipartFile("test.pdf", "test.pdf", "pdf",
                    "123".getBytes(StandardCharsets.UTF_8));
            PdfUploadRequest pdfUploadRequest = new PdfUploadRequest(1L, 1);
            //when
            uuid = fileService.savePdf(multipartFile, pdfUploadRequest);
            //then
            assertThat(new File(FileService.uploadPath + uuid + ".pdf").exists()).isTrue();
            assertThat(fileInfoRepository.findByUid(uuid).orElseThrow().getOriginalFileName()).isEqualTo(
                    "test.pdf");
        }

        @Test
        @DisplayName("확장자 오류")
        @Transactional
        public void extentionError() throws IOException {
            //given
            MultipartFile multipartFile = new MockMultipartFile("test.pdf", "test.exe", "pdf",
                    "123".getBytes(StandardCharsets.UTF_8));
            PdfUploadRequest pdfUploadRequest = new PdfUploadRequest(1L, 1);
            //then
            assertThrows(FlatException.class, () -> fileService.savePdf(multipartFile, pdfUploadRequest));
        }
    }

    @Nested
    @DisplayName("Mxl 저장 관련")
    class mxlSaveTest {

        private String uuid;

        @BeforeEach
        public void beforeMxlTest() throws IOException {
            MultipartFile multipartFile = new MockMultipartFile("test.pdf", "test.pdf", "pdf",
                    "123".getBytes(StandardCharsets.UTF_8));
            PdfUploadRequest pdfUploadRequest = new PdfUploadRequest(1L, 1);
            uuid = fileService.savePdf(multipartFile, pdfUploadRequest);
        }

        @AfterEach
        public void deleteFile() {
            new File(FileService.uploadPath + uuid + ".mxl").delete();
            new File(FileService.uploadPath + uuid + ".pdf").delete();
        }


        @Test
        @DisplayName("Mxl 저장 테스트")
        @Transactional
        public void saveMxl() throws IOException {
            //given
            MultipartFile multipartFile = new MockMultipartFile(uuid + ".mxl", uuid + ".mxl", null, "123".getBytes(
                    StandardCharsets.UTF_8));

            //when
            fileService.saveMxl(multipartFile);

            //then
            assertThat(new File(FileService.uploadPath + uuid + ".mxl").exists()).isTrue();
            assertThat(fileInfoRepository.findByUid(uuid).orElseThrow().isMxlPresent()).isTrue();
        }

        @Test
        @DisplayName("확장자 오류")
        @Transactional
        public void wrongExtention() {
            //given
            MultipartFile multipartFile = new MockMultipartFile(uuid + ".mxl", uuid + ".tmxl", null, "123".getBytes(
                    StandardCharsets.UTF_8));

            assertThrows(FlatException.class, () -> fileService.saveMxl(multipartFile));
        }

        @Test
        @DisplayName("uuid 오류")
        @Transactional
        public void wrongUuid() {
            //given
            MultipartFile multipartFile = new MockMultipartFile(uuid + "1.mxl", uuid + ".tmxl", null, "123".getBytes(
                    StandardCharsets.UTF_8));

            assertThrows(FlatException.class, () -> fileService.saveMxl(multipartFile));
        }
    }

    @Nested
    @DisplayName("Pdf 정보 요청")
    class getPdf {
        private String uuid;

        @BeforeEach
        public void beforeTest() throws IOException {
            MultipartFile multipartFile = new MockMultipartFile("test.pdf", "test.pdf", "pdf",
                    "123".getBytes(StandardCharsets.UTF_8));
            PdfUploadRequest pdfUploadRequest = new PdfUploadRequest(1L, 1);
            uuid = fileService.savePdf(multipartFile, pdfUploadRequest);
        }

        @AfterEach
        public void afterTest() {
            new File(FileService.uploadPath + uuid + ".pdf").delete();
        }

        @Test
        @DisplayName("파일 정보 조회")
        public void getInfo() {
            assertThat(fileService.getPdf(uuid).getUid()).endsWith(uuid);
        }

        @Test
        @DisplayName("잘못된 uuid")
        public void getWrongUid() {
            assertThrows(FlatException.class, () -> fileService.getPdf(uuid + "1"));
        }
    }

    @Nested
    @DisplayName("Pdf 정보 변환")
    class encodePdf {
        private String uuid;

        @BeforeEach
        public void beforeTest() throws IOException {
            MultipartFile multipartFile = new MockMultipartFile("test.pdf", "test.pdf", "pdf",
                    "123".getBytes(StandardCharsets.UTF_8));
            PdfUploadRequest pdfUploadRequest = new PdfUploadRequest(1L, 1);
            uuid = fileService.savePdf(multipartFile, pdfUploadRequest);
        }

        @AfterEach
        public void afterTest() {
            new File(FileService.uploadPath + uuid + ".pdf").delete();
        }

        @Test
        @DisplayName("변환 성공")
        public void encodeSuccess() throws MalformedURLException {
            //given
            FileInfo fileInfo = fileInfoRepository.findByUid(uuid).orElseThrow();

            //when
            FileDto fileDto = fileService.encodePdf(fileInfo);

            //then
            assertThat(fileDto.getEncodedFileName()).contains(uuid);
            assertThat(fileDto.getFile().getURL().toString()).contains(uuid);
        }
    }
}
