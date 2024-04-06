package com.ai.FlatServer.folder.repository;

import com.ai.FlatServer.folder.repository.entity.Folder;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface FolderRepository extends JpaRepository<Folder, Long> {

    @Modifying
    @Query("delete from Folder f where f.id in :ids")
    void deleteAllByIds(List<Long> ids);

}
