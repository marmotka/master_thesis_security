package com.fmi.quarkus.model;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
public class Task extends PanacheEntity {

    @Column(nullable = false)
    public String title;

    @Column(length = 2000)
    public String description;

    public LocalDate dueDate;

    @Enumerated(EnumType.STRING)
    public Status status = Status.PENDING;

    @ManyToOne(optional = false)
    public User owner;


    public String getTitle() {
        return this.title;
    }

    public String getDescription() {
        return this.description;
    }

    public LocalDate getDueDate() {
        return this.dueDate;
    }

    public Status getStatus() {
        return this.status;
    }

    public User getOwner() {
        return this.owner;
    }
}