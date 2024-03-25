package com.ai.FlatServer.controller.file;

import com.ai.FlatServer.domain.dto.folder.FolderInfo;
import com.ai.FlatServer.domain.dto.request.folder.FolderCreationRequest;
import com.ai.FlatServer.domain.dto.request.folder.FolderPatchRequest;
import com.ai.FlatServer.service.file.FolderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.net.URI;
import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Folder", description = "폴더 관련 API")
@RestController
@RequestMapping(path = "/folder")
@RequiredArgsConstructor
public class FolderController {

    private final FolderService folderService;

    @Operation(summary = "폴더 정보 조회", description = "특정 파일 id를 가진 폴더 조회.최상위 루트 폴더의 id는 1입니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공", content = @Content(schema = @Schema(implementation = FolderInfo.class))),
            @ApiResponse(responseCode = "400", description = "해당하는 폴더가 없음", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "500", description = "런타임 에러!!", content = @Content(schema = @Schema(hidden = true)))
    })
    @GetMapping("/{folderId}")
    public ResponseEntity<FolderInfo> getFolder(@PathVariable Long folderId) {
        try {
            FolderInfo folderResult = folderService.getFolderWithId(folderId);
            return ResponseEntity.ok(folderResult);
        } catch (NoSuchElementException e) {
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @Operation(summary = "폴더 생성.", description = "특정 디렉토리에 어떤 이름의 폴더 생성")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "성공", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "400", description = "해당하는 파일이 없음", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "500", description = "런타임 에러!!", content = @Content(schema = @Schema(hidden = true)))
    })
    @PostMapping
    public ResponseEntity<Boolean> createFolder(@RequestBody FolderCreationRequest folderCreationRequest) {
        try {
            folderService.createFolderAt(folderCreationRequest.getFolderName(),
                    folderCreationRequest.getCurrentFolderId());
            return ResponseEntity.created(URI.create("")).build();
        } catch (NoSuchElementException e) {
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @Operation(summary = "폴더 삭제.", description = "특정 파일 id를 가진 폴더 삭제")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공"),
            @ApiResponse(responseCode = "400", description = "해당하는 파일이 없음", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "500", description = "런타임 에러!!", content = @Content(schema = @Schema(hidden = true)))
    })
    @DeleteMapping("/{currentFolderId}")
    public ResponseEntity<Boolean> deleteFolder(@PathVariable Long currentFolderId) {
        try {
            folderService.deleteFolder(currentFolderId);
            return ResponseEntity.ok().build();
        } catch (NoSuchElementException e) {
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException e) {
            return ResponseEntity.internalServerError().build();
        }

    }

    @Operation(summary = "폴더 패치", description = "특정 id를 가진 폴더의 이름 또는 경로 변경")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공"),
            @ApiResponse(responseCode = "400", description = "해당하는 파일이 없음", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "500", description = "런타임 에러!!", content = @Content(schema = @Schema(hidden = true)))
    })
    @PatchMapping("/{currentFolderId}")
    public ResponseEntity<Boolean> changeName(@PathVariable Long currentFolderId,
                                              @RequestBody FolderPatchRequest folderPatchRequest) {
        try {
            folderService.patchUpdate(folderPatchRequest, currentFolderId);
            return ResponseEntity.ok().build();
        } catch (NoSuchElementException e) {
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
