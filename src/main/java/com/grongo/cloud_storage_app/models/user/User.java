package com.grongo.cloud_storage_app.models.user;


import com.grongo.cloud_storage_app.models.TimeStamps;
import com.grongo.cloud_storage_app.models.items.Item;
import com.grongo.cloud_storage_app.models.sharedItems.SharedItem;
import com.grongo.cloud_storage_app.models.tag.Tag;
import com.grongo.cloud_storage_app.models.token.JwtRefresh;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Cascade;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "t_user")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class User extends TimeStamps {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    Long id;
    String username;
    String password;
    String picture;

    @Column(unique = true)
    String email;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "owner")
    List<SharedItem> sharedItems;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "user")
    List<SharedItem> notOwnedSharedItems;


}
