package com.ai.FlatServer.folder.repository;

import com.ai.FlatServer.folder.repository.entity.Folder;
import java.util.List;
import java.util.Optional;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

@Repository
public interface FolderRepository extends JpaRepository<Folder, Long> {

    @Modifying
    @Query("delete from Folder f where f.id in :ids")
    void deleteAllByIds(List<Long> ids);

    @NonNull
    @Cacheable(cacheNames = "folderCache", key = "#id")
    Optional<Folder> findById(@NonNull Long id);

    List<Folder> findByParentId(Long id);
}
