package com.ai.FlatServer.repository;

import com.ai.FlatServer.repository.entity.FileInfo;
import java.util.ArrayList;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FileInfoRepository extends JpaRepository<FileInfo, Long> {

    Optional<FileInfo> findByUid(String uid);

    ArrayList<FileInfo> findAllByFav(boolean bool);
}
