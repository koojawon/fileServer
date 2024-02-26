package com.ai.FlatServer.domain.dao;

import com.ai.FlatServer.repository.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Builder
@Entity
@Data
@RequiredArgsConstructor
@AllArgsConstructor
public class FileInfoDao extends BaseEntity {

    @Id
    @Column(updatable = false, unique = true, nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, updatable = false, name = "name")
    private String originalFileName;

    @Column(nullable = false, updatable = false, name = "uid")
    private String uid;

    @Column(nullable = false, name = "xml")
    private boolean xmlPresent;
}
