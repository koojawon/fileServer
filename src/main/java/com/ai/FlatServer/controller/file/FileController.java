package com.ai.FlatServer.controller.file;

import com.ai.FlatServer.domain.dto.file.FileDto;
import com.ai.FlatServer.domain.dto.file.FileNameInfo;
import com.ai.FlatServer.domain.dto.request.file.FilePatchRequest;
import com.ai.FlatServer.domain.dto.request.file.PdfUploadRequest;
import com.ai.FlatServer.service.file.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "File", description = "파일 관련 API")
@RestController
@RequestMapping("/file")
@RequiredArgsConstructor
@Slf4j
public class FileController {

    private final FileService fileService;

    @Operation(summary = "Mxl 파일 요청.", description = "Mxl 파일 id를 통해 파일을 다운로드")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공"),
            @ApiResponse(responseCode = "400", description = "존재하지 않는 파일", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content(schema = @Schema(hidden = true)))
    })
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

    @Operation(summary = "Pdf 파일 요청.", description = "Pdf 파일 uid를 통해 파일을 다운로드. 유저가 사용 금지!")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공"),
            @ApiResponse(responseCode = "400", description = "존재하지 않는 파일", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content(schema = @Schema(hidden = true)))
    })
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

    @Operation(summary = "즐겨찾기한 파일 조회", description = "즐겨찾기 한 파일 목록 요청. 비어있더라도 무조건 200을 반환합니다.(즐겨찾기 폴더를 이 api로 구현하면 좋을 것 같습니다.)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공"),
            @ApiResponse(responseCode = "500", description = "런타임 에러!", content = @Content(schema = @Schema(hidden = true)))
    })
    @GetMapping("/fav")
    public ResponseEntity<List<FileNameInfo>> getFavs() {
        try {
            return ResponseEntity.ok(fileService.getFavs());
        } catch (RuntimeException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @Operation(summary = "파일 업로드.", description = "body의 key:value 형식으로 된 'multipartFile' : 파일 형태를 받아 저장. pdf 저장 가능. 한 번에 여러개도 가능합니다. client가 mxl을 올리지 말것!!")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "성공"),
            @ApiResponse(responseCode = "400", description = "지원하지 않는 파일 확장자", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "500", description = "파일 저장 실패", content = @Content(schema = @Schema(hidden = true)))
    })
    @PostMapping
    public ResponseEntity<ArrayList<Long>> uploadPdf(
            @RequestPart(value = "dto") PdfUploadRequest pdfUploadRequest,
            @RequestPart(value = "files") List<MultipartFile> multipartFile) {
        ArrayList<Long> Ids = new ArrayList<>();
        try {
            for (MultipartFile m : multipartFile) {
                Ids.add(fileService.saveFile(m, pdfUploadRequest));
            }
        } catch (UnsupportedEncodingException | NoSuchElementException e) {
            return ResponseEntity.badRequest().build();
        } catch (IOException | RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
        return ResponseEntity.status(201).body(Ids);
    }

    @Operation(summary = "파일 삭제.", description = "특정 파일 id를 가진 파일 삭제")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "400", description = "해당하는 파일이 없음", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "500", description = "런타임 에러!!", content = @Content(schema = @Schema(hidden = true)))
    })
    @DeleteMapping("/{currentFileId}")
    public ResponseEntity<Boolean> deleteFile(@PathVariable Long currentFileId) {
        try {
            fileService.removeFile(currentFileId);
            return ResponseEntity.ok().build();
        } catch (NoSuchElementException e) {
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException e) {
            return ResponseEntity.internalServerError().build();
        }

    }

    @Operation(summary = "파일 패치.", description = "특정 파일을 다른 폴더로 이동하거나 아이콘 변경, 또는 즐겨찾기 상태 지정")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "400", description = "해당하는 파일/폴더가 없음", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "500", description = "런타임 에러!!", content = @Content(schema = @Schema(hidden = true)))
    })
    @PatchMapping("/{currentFileId}")
    public ResponseEntity<Boolean> patchFile(@PathVariable Long currentFileId,
                                             @RequestBody FilePatchRequest filePatchRequest) {
        try {
            fileService.patchFile(currentFileId, filePatchRequest);
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
