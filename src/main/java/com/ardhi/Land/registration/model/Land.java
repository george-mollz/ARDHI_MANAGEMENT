package com.ardhi.Land.registration.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name="land")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Land {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String plotNo;

    @Column(nullable = false)
    private String region;

    @Column(nullable = false)
    private String landUse;

    private boolean isRegistered = false;

}
