package com.ai.FlatServer.domain.file.respository;

import com.ai.FlatServer.domain.file.respository.dao.FileInfo;
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
    @Cacheable(cacheNames = "fileCache", key = "#id")
    Optional<FileInfo> findById(@NonNull Long id);

    List<FileInfo> findAllByFav(boolean bool);

    List<FileInfo> findAllByParentFolderId(Long id);

    @Modifying(flushAutomatically = true)
    @Query("delete from FileInfo f where f.parentFolderId in :ids")
    void deleteAllByParentFolderId(List<Long> ids);

    List<FileInfo> findByParentFolderIdIn(List<Long> folderIds);

    @Query(value = "select * from FileInfo f where f.fav = :b and f.ownerId = :userId", nativeQuery = true)
    List<FileInfo> findAllByFavAndOwnerId(boolean b, Long userId);

    @Cacheable(cacheNames = "fileCache", key = "'all'+#userId")
    @Query(value = "select * from FileInfo f where f.ownerId = :userId", nativeQuery = true)
    List<FileInfo> findByOwnerId(Long userId);
}
