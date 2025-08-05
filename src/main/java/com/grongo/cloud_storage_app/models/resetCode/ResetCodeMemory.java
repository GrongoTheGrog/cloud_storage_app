package com.grongo.cloud_storage_app.models.resetCode;


import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ResetCodeMemory {

    String code;
    Date createdAt;
    boolean isChecked;

}
