package com.ai.FlatServer.domain.folder.repository.entity;

import com.ai.FlatServer.domain.folder.enums.FolderType;
import com.ai.FlatServer.global.repository.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;


@EqualsAndHashCode(callSuper = true)
@Entity(name = "Folder")
@Data
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
public class Folder extends BaseEntity {
    @Id
    @Column(updatable = false, nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, name = "name")
    private String folderName;

    private Long parentId;

    @Enumerated(EnumType.STRING)
    private FolderType type;

    private Long ownerId;
}
