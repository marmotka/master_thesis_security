package com.fmi.quarkus.web;


import com.fmi.quarkus.dto.TaskForm;
import com.fmi.quarkus.exception.TaskAccessException;
import com.fmi.quarkus.exception.TaskValidationException;
import com.fmi.quarkus.model.Task;
import com.fmi.quarkus.service.TaskService;
import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;
import io.quarkus.security.identity.SecurityIdentity;
import io.smallrye.common.annotation.Blocking;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.net.URI;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Path("/tasks")
@Blocking
@RolesAllowed({"USER", "ADMIN"})
@Produces(MediaType.TEXT_HTML)
public class TaskController {

    @Inject
    SecurityIdentity identity;
    @Inject
    TaskService taskService;

    @CheckedTemplate
    public static class Tpl {
        public static native TemplateInstance tasks(List<Task> tasks,
                                                    SecurityIdentity identity,
                                                    String successMessage,
                                                    String errorMessage);

        public static native TemplateInstance taskForm(Long taskId,
                                                       boolean isEdit,
                                                       Map<String, Object> form,
                                                       Map<String, String> fieldErrors,
                                                       SecurityIdentity identity);
    }

    private String currentUsername() {
        return identity.getPrincipal().getName();
    }

    // ---------- LIST ----------

    @GET
    @Path("view")
    public TemplateInstance list(@QueryParam("notice") @DefaultValue("") String notice) {
        String username = currentUsername();
        List<Task> tasks = taskService.listTasksForUser(username);

        String successMessage = "";
        String errorMessage = "";

        switch (notice) {
            case "created" -> successMessage = "Task created successfully.";
            case "updated" -> successMessage = "Task updated successfully.";
            case "deleted" -> successMessage = "Task deleted successfully.";
            case "forbidden" -> errorMessage = "You are not allowed to perform this operation.";
        }

        return Tpl.tasks(tasks, identity, successMessage, errorMessage);
    }

    // ---------- CREATE FORM ----------

    @GET
    @Path("new")
    public TemplateInstance newTaskForm() {
        Map<String, Object> form = new HashMap<>();
        form.put("id", null);
        form.put("title", "");
        form.put("description", "");
        form.put("dueDate", "");
        form.put("status", "PENDING");

        return Tpl.taskForm(null, false, form, Map.of(), identity);
    }

    // ---------- EDIT FORM ----------

    @GET
    @Path("edit/{id}")
    public TemplateInstance editTaskForm(@PathParam("id") Long id) {
        String username = currentUsername();
        var task = taskService.getTaskForUser(username, id);

        Map<String, Object> form = new HashMap<>();
        form.put("id", task.id);
        form.put("title", task.title);
        form.put("description", task.description != null ? task.description : "");
        form.put("dueDate", task.dueDate != null ? task.dueDate.format(DateTimeFormatter.ISO_DATE) : "");
        form.put("status", task.status != null ? task.status.name() : "PENDING");

        return Tpl.taskForm(task.id, true, form, Map.of(), identity);
    }

    // ---------- CREATE/UPDATE POST ----------


    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Object save(@BeanParam TaskFormBean bean) {
        String username = currentUsername();
        TaskForm formDto = new TaskForm(bean.id, bean.title, bean.description, bean.dueDate, bean.status);

        boolean isEdit = formDto.id != null;

        try {
            taskService.saveTaskForUser(username, formDto);
            String notice = isEdit ? "updated" : "created";
            return Response.seeOther(URI.create("/tasks/view?notice=" + notice)).build();

        } catch (TaskValidationException ex) {
            Map<String, Object> form = new HashMap<>();
            form.put("id", formDto.id);
            form.put("title", formDto.title != null ? formDto.title : "");
            form.put("description", formDto.description != null ? formDto.description : "");
            form.put("dueDate", formDto.dueDate != null ? formDto.dueDate : "");
            form.put("status", formDto.status != null ? formDto.status : "PENDING");

            return Tpl.taskForm(formDto.id, isEdit, form, ex.getFieldErrors(), identity);

        } catch (TaskAccessException ex) {
            return Response.seeOther(URI.create("/tasks/view?notice=forbidden")).build();
        }
    }

    // ---------- DELETE ----------

    @POST
    @Path("{id}/delete")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response delete(@PathParam("id") Long id) {
        String username = currentUsername();
        try {
            taskService.deleteTaskForUser(username, id);
            return Response.seeOther(URI.create("/tasks/view?notice=deleted")).build();
        } catch (TaskAccessException ex) {
            return Response.seeOther(URI.create("/tasks/view?notice=forbidden")).build();
        }
    }

    public static class TaskFormBean {
        @FormParam("id")
        public Long id;
        @FormParam("title")
        public String title;
        @FormParam("description")
        public String description;
        @FormParam("dueDate")
        public String dueDate;
        @FormParam("status")
        public String status;
    }

}
