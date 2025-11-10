package com.fmi.quarkus.web;


import com.fmi.quarkus.model.Task;
import com.fmi.quarkus.model.User;
import com.fmi.quarkus.service.TaskService;
import com.fmi.quarkus.service.UserService;
import io.quarkus.qute.TemplateInstance;
import io.smallrye.common.annotation.Blocking;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.security.Principal;

import java.util.List;

    @Path("/tasks")
    @Blocking
    @RolesAllowed({"USER","ADMIN"})
    public class TaskController {

        @Inject TaskService tasks;
        @Inject UserService users;

        private User current(Principal p) { return users.findByUsername(p.getName()).orElse(null); }

        @GET
        @Path("view")
        @Produces(MediaType.TEXT_HTML)
        public TemplateInstance list(Principal principal) {
            User u = current(principal);
            List<Task> mine = tasks.listFor(u);
            return Templates.Pages.tasks(mine, null, null);
        }

        @POST
        @Path("new")
        @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
        public Object create(@FormParam("title") String title,
                             @FormParam("description") String description,
                             @FormParam("dueDate") String dueDate,
                             @FormParam("status") String status,
                             Principal principal) {
            User u = current(principal);
            tasks.create(u, title, description, dueDate, status);
            return Response.seeOther(java.net.URI.create("/tasks/view")).build();
        }

        @POST
        @Path("{id}/delete")
        public Object delete(@PathParam("id") Long id, Principal principal) {
            User u = current(principal);
            tasks.delete(id, u);
            return Response.seeOther(java.net.URI.create("/tasks/view")).build();
        }
    }