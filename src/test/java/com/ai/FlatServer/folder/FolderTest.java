package com.ai.FlatServer.folder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.ai.FlatServer.domain.folder.dto.request.FolderPatchRequest;
import com.ai.FlatServer.domain.folder.enums.FolderType;
import com.ai.FlatServer.domain.folder.repository.FolderRepository;
import com.ai.FlatServer.domain.folder.repository.entity.Folder;
import com.ai.FlatServer.domain.folder.service.FolderService;
import com.ai.FlatServer.domain.user.repository.entity.User;
import com.ai.FlatServer.global.exceptions.FlatException;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class FolderTest {
    @Autowired
    private FolderService folderService;

    @Autowired
    private FolderRepository folderRepository;


    @Test
    @DisplayName("루트 폴더 생성 테스트")
    @Transactional
    public void createRootFolder() {
        //given
        User user = User.builder()
                .id(1L)
                .build();

        //when
        folderService.createRootFolderFor(user);

        //then
        assertThat(folderRepository.findById(user.getUserRootFolderId())).isNotNull();
    }


    @Nested
    @DisplayName("폴더 삭제 관련")
    class deleteTest {
        @Test
        @DisplayName("폴더 삭제 테스트")
        public void deleteFolder() {
            //given
            Folder folder = folderRepository.saveAndFlush(
                    Folder.builder().folderName("test").parentId(1L).type(FolderType.LEAF).build());
            //when
            folderService.deleteFolder(folder.getId());
            //then
            assertThat(folderRepository.findById(folder.getId())).isNotPresent();
        }

        @Test
        @DisplayName("존재하지 않는 폴더 ID")
        @Transactional
        public void wrongIdFail() {
            //given
            Long wrongId = 993L;

            assertThrows(FlatException.class, () -> folderService.deleteFolder(wrongId));
        }
    }


    @Nested
    @DisplayName("폴더 생성 관련")
    class creationTest {
        @Test
        @DisplayName("폴더 생성 테스트")
        @Transactional
        public void creationSuccess() {
            //given
            String folderName = "testFolder";
            Long currentFolderId = 1L;
            User user = User.builder()
                    .id(1L)
                    .build();

            //when
            folderService.createFolder(folderName, currentFolderId, user);

            //then
            assertThat(folderRepository.findByParentId(1L).get(0).getFolderName()).isEqualTo(folderName);
        }

        @Test
        @DisplayName("부적절한 parent폴더 id로 인한 실패")
        @Transactional
        public void wrongParentIdFail() {
            String folderName = "testFolder";
            Long currentFolderId = 993L;
            User user = User.builder()
                    .id(1L)
                    .build();

            //then
            assertThrows(FlatException.class, () -> folderService.createFolder(folderName, currentFolderId, user));
        }
    }

    @Nested
    @DisplayName("폴더 패치 관련")
    class patchTest {

        @Test
        @DisplayName("폴더 이동")
        @Transactional
        public void moveFolder() {
            //given
            Folder folder = Folder.builder().folderName("folder").type(FolderType.LEAF).parentId(1L)
                    .build();
            Folder newFolder = Folder.builder().folderName("newFolder").type(FolderType.LEAF).parentId(1L)
                    .build();

            folderRepository.save(folder);
            folderRepository.saveAndFlush(newFolder);

            FolderPatchRequest folderPatchRequest = new FolderPatchRequest();
            folderPatchRequest.setNewParent(newFolder.getId());

            //when
            folderService.patchUpdate(folderPatchRequest, folder.getId());

            //then
            assertThat(folderRepository.findById(folder.getId()).orElseThrow().getParentId()).isEqualTo(
                    newFolder.getId());
        }

        @Test
        @DisplayName("잘못된 목적지로 폴더 이동")
        @Transactional
        public void moveWrongIdFail() {
            //given
            Folder folder = Folder.builder().folderName("folder").type(FolderType.LEAF).parentId(1L)
                    .build();

            folderRepository.saveAndFlush(folder);

            FolderPatchRequest folderPatchRequest = new FolderPatchRequest();
            folderPatchRequest.setNewParent(993L);

            assertThrows(FlatException.class, () -> folderService.patchUpdate(folderPatchRequest, folder.getId()));
        }

        @Test
        @DisplayName("폴더 이름 변경")
        @Transactional
        public void changeName() {
            //given
            Folder folder = Folder.builder().folderName("folder").type(FolderType.LEAF).parentId(1L)
                    .build();

            folderRepository.saveAndFlush(folder);

            FolderPatchRequest folderPatchRequest = new FolderPatchRequest();
            folderPatchRequest.setNewName("folder2");

            //when
            folderService.patchUpdate(folderPatchRequest, folder.getId());

            //then
            assertThat(folderRepository.findById(folder.getId()).orElseThrow().getFolderName()).isEqualTo(
                    "folder2");
        }

        @Test
        @DisplayName("잘못된 대상")
        @Transactional
        public void wrongId() {
            //given

            FolderPatchRequest folderPatchRequest = new FolderPatchRequest();
            assertThrows(FlatException.class, () -> folderService.patchUpdate(folderPatchRequest, 993L));
        }

    }

}
