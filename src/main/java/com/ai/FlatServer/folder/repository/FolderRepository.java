package com.ai.FlatServer.folder.repository;

import com.ai.FlatServer.folder.repository.entity.Folder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FolderRepository extends JpaRepository<Folder, Long> {
}
