package com.grongo.cloud_storage_app.models.items.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.MediaType;

import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QueryItemDto {

    private String name;
    private Date minDate;
    private Date maxDate;
    private Long minBytes;
    private Long maxBytes;
    private List<MediaType> type;
    private List<Long> tagIds;
    private Long parentId = -1L;

}
