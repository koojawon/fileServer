package com.ai.FlatServer.domain.folder.repository;

import com.ai.FlatServer.domain.folder.repository.entity.Folder;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

@Repository
public interface FolderRepository extends JpaRepository<Folder, Long> {

    @Modifying
    @Query("delete from Folder f where f.id in :ids")
    @Transactional
    void deleteAllByIds(@Param("ids") List<Long> ids);

    @NonNull
    Optional<Folder> findById(@NonNull Long id);

    List<Folder> findByParentId(Long id);
}
