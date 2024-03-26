package com.ai.FlatServer.file.respository.dao;

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
public class FileInfo extends BaseEntity {

    @Id
    @Column(updatable = false, nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, updatable = false, name = "name")
    private String originalFileName;

    @Column(nullable = false, updatable = false, name = "uid")
    private String uid;

    @Column(nullable = false, name = "mxl")
    private boolean mxlPresent;

    @Builder.Default
    @Column(nullable = false)
    private Boolean fav = false;

    @Column(nullable = false)
    private int iconId;

    @Column(name = "parent_folder_id")
    private Long parentFolder;
}
