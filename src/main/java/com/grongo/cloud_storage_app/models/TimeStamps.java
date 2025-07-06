package com.grongo.cloud_storage_app.models;


import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.Date;

@MappedSuperclass
@SuperBuilder
@Data
@AllArgsConstructor
@NoArgsConstructor
public abstract class TimeStamps {

    Date created_at;
    Date updated_at;

    @PrePersist
    public void prePersist(){
        Date now = new Date();
        created_at = now;
        updated_at = now;
    }

    @PreUpdate
    public void preUpdate(){
        updated_at = new Date();
    }
}
