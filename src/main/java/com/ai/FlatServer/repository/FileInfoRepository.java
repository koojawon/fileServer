package com.ai.FlatServer.repository;

import com.ai.FlatServer.domain.dao.FileInfoDao;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FileInfoRepository extends JpaRepository<FileInfoDao, Long> {

    Optional<FileInfoDao> findByUid(String uid);
}
