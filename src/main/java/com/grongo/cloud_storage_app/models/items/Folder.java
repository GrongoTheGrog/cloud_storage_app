package com.grongo.cloud_storage_app.models.items;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Entity
@DiscriminatorValue("FOLDER")
@SuperBuilder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Folder extends Item{

    @OneToMany(cascade = CascadeType.REMOVE)
    @JoinColumn(name = "folder_id")
    private List<Item> storedFiles;

}
