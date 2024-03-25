package com.ai.FlatServer.domain.dto.file;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FileNameInfo {
    private final Long id;
    private final Integer iconId;
    private final LocalDateTime modDate;
    private final String name;
}
