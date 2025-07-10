package com.grongo.cloud_storage_app.models.token;


import com.grongo.cloud_storage_app.models.TimeStamps;
import com.grongo.cloud_storage_app.models.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.Date;

@EqualsAndHashCode(callSuper = true)
@Entity
@SuperBuilder
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "t_jwt_refresh")
public class JwtRefresh extends TimeStamps {

    @Id
    @Column(unique = true)
    String id;
    String token;
    @ManyToOne
    User user;
    Date expirationDate;


}
