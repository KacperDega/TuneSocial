package com.tunesocial.backend.user;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "users")
@Setter
@Getter
public class User {

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false, unique = true)
    private String username; // @username

    @Column(nullable = false)
    private String displayName;

    @JsonIgnore
    @Column(nullable = false)
    private String passwordHash;
}

