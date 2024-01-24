package com.retailmanager.rmpaydashboard.models;

import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity @Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class UsersBusiness {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userBusinessId;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private Boolean enable;

    @OneToMany(mappedBy = "userBusiness")
    private List<UserPermission> userPermissions;

}