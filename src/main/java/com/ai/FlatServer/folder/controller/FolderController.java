package com.ai.FlatServer.folder.controller;

import com.ai.FlatServer.folder.dto.request.FolderCreationRequest;
import com.ai.FlatServer.folder.dto.request.FolderPatchRequest;
import com.ai.FlatServer.folder.dto.response.FolderInfo;
import com.ai.FlatServer.folder.service.FolderService;
import com.ai.FlatServer.user.repository.entity.User;
import com.ai.FlatServer.user.service.UserService;
import java.net.URI;
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
    private final UserService userService;

    @GetMapping("/{folderId}")
    public ResponseEntity<FolderInfo> getFolder(@PathVariable Long folderId) {
        User user = userService.getCurrentUser();
        FolderInfo folderResult = folderService.getFolderWithId(folderId);
        return ResponseEntity.ok(folderResult);
    }


    @PostMapping
    public ResponseEntity<Boolean> createFolder(@RequestBody FolderCreationRequest folderCreationRequest) {
        User user = userService.getCurrentUser();
        if (userService.checkCreateAvailability()) {
            folderService.createFolder(folderCreationRequest.getFolderName(),
                    folderCreationRequest.getCurrentFolderId(), user);
            userService.decreaseFolderCount();
        }
        return ResponseEntity.created(URI.create("")).build();
    }

    @DeleteMapping("/{targetFolderId}")
    public ResponseEntity<Boolean> deleteFolder(@PathVariable Long targetFolderId) {
        folderService.deleteFolder(targetFolderId);
        userService.increaseFolderCount();
        return ResponseEntity.ok().build();
    }


    @PatchMapping("/{targetFolderId}")
    public ResponseEntity<Boolean> changeName(@PathVariable Long targetFolderId,
                                              @RequestBody FolderPatchRequest folderPatchRequest) {
        folderService.patchUpdate(folderPatchRequest, targetFolderId);
        return ResponseEntity.ok().build();
    }
}
