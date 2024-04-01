package com.ai.FlatServer.file.respository;

import com.ai.FlatServer.file.respository.dao.FileInfo;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface FileInfoRepository extends JpaRepository<FileInfo, Long> {

    Optional<FileInfo> findByUid(String uid);

    List<FileInfo> findAllByFav(boolean bool);

    List<FileInfo> findAllByParentFolderId(Long id);

    @Modifying
    @Query("delete from FileInfo f where f.parentFolderId in :ids")
    void deleteAllByParentFolderId(List<Long> ids);
}
