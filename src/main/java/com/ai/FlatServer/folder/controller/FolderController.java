package com.ai.FlatServer.folder.controller;

import com.ai.FlatServer.folder.dto.response.FolderInfo;
import com.ai.FlatServer.folder.dto.request.FolderCreationRequest;
import com.ai.FlatServer.folder.dto.request.FolderPatchRequest;
import com.ai.FlatServer.folder.service.FolderService;
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

@RestController
@RequestMapping(path = "/folder")
@RequiredArgsConstructor
public class FolderController {

    private final FolderService folderService;

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

    @DeleteMapping("/{targetFolderId}")
    public ResponseEntity<Boolean> deleteFolder(@PathVariable Long targetFolderId) {
        try {
            folderService.deleteFolder(targetFolderId);
            return ResponseEntity.ok().build();
        } catch (NoSuchElementException e) {
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException e) {
            return ResponseEntity.internalServerError().build();
        }

    }


    @PatchMapping("/{targetFolderId}")
    public ResponseEntity<Boolean> changeName(@PathVariable Long targetFolderId,
                                              @RequestBody FolderPatchRequest folderPatchRequest) {
        try {
            folderService.patchUpdate(folderPatchRequest, targetFolderId);
            return ResponseEntity.ok().build();
        } catch (NoSuchElementException e) {
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
