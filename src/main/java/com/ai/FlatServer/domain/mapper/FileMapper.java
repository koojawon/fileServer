package com.ai.FlatServer.domain.mapper;

import com.ai.FlatServer.domain.dao.FileInfoDao;
import com.ai.FlatServer.domain.dto.ResponseFile;

public class FileMapper {

    public static ResponseFile FileDaoToResponseFileMapper(FileInfoDao fileInfoDao) {
        return ResponseFile.builder()
                .fileName(fileInfoDao.getOriginalFileName())
                .uid(fileInfoDao.getUid())
                .build();
    }
}
