package com.ai.FlatServer.file.respository;

import com.ai.FlatServer.file.respository.dao.FileInfo;
import com.ai.FlatServer.user.repository.entity.User;
import java.util.List;
import java.util.Optional;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

@Repository
public interface FileInfoRepository extends JpaRepository<FileInfo, Long> {

    Optional<FileInfo> findByUid(String uid);

    @NonNull
    @Cacheable(value = "fileCache", key = "#id", condition = "")
    Optional<FileInfo> findById(@NonNull Long id);

    List<FileInfo> findAllByFav(boolean bool);

    List<FileInfo> findAllByParentFolderId(Long id);

    @Modifying
    @Query("delete from FileInfo f where f.parentFolderId in :ids")
    void deleteAllByParentFolderId(List<Long> ids);

    @Query("select * from FileInfo f where f.parentFolderId in :folderIds")
    List<FileInfo> selectAllByParentFolderId(List<Long> folderIds);

    List<FileInfo> findAllByFavAndOwner(boolean b, User user);
}
