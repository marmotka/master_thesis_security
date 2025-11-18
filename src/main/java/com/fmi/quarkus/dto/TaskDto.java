package com.fmi.quarkus.dto;


import com.fmi.quarkus.model.Status;
import lombok.Data;

@Data
public class TaskDto {

    public long id;

    public String title;

    public String description;

    public String dueDate;

    public Status status = Status.PENDING;

}
