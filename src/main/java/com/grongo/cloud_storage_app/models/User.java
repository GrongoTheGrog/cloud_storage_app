package com.grongo.cloud_storage_app.models;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "t_user")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class User extends TimeStamps{

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    Long id;
    String username;
    String password;
    String picture;

    @Column(unique = true)
    String email;
}
