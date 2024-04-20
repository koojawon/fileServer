package com.ai.FlatServer.domain.folder.dto.response;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SubFolderInfo {
    private Long id;
    private String folderName;
    private Long parentId;
    private LocalDateTime modDate;
}
