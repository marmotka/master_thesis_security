package com.fmi.quarkus.model;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;
import lombok.Getter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDate;

@Getter
@Entity
public class Task extends PanacheEntity {

    @Column(nullable = false)
    public String title;

    @Column(length = 2000)
    public String description;

    public LocalDate dueDate;

    @Enumerated(EnumType.STRING)
    public Status status = Status.PENDING;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    public User owner;


}