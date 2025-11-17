package com.fmi.quarkus.dto;


import com.fmi.quarkus.model.Status;
import com.fmi.quarkus.model.User;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ManyToOne;
import lombok.Data;

import java.time.LocalDate;

@Data
public class TaskDto {
    
    public long id;

    public String title;

    public String description;

    public String dueDate;

    public Status status = Status.PENDING;

}
