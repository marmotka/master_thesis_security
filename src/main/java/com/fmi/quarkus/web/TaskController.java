package com.fmi.quarkus.web;

import io.quarkus.security.identity.SecurityIdentity;
import com.fmi.quarkus.model.Task;
import com.fmi.quarkus.service.TaskService;
import com.fmi.quarkus.service.UserService;
import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.SecurityContext;

@Path("/tasks")
@Produces(MediaType.TEXT_HTML)
//@RolesAllowed({"USER","ADMIN"})
public class TaskController {

  @Inject
  SecurityIdentity identity;
  @Inject
  UserService users;
  @Inject
  TaskService taskService;

  @CheckedTemplate
  public static class Tpl {
    public static native TemplateInstance tasks(java.util.List<Task> tasks, SecurityIdentity identity);
//    public static native TemplateInstance task_form(java.util.List<?> tasks);
  }

  @GET
  @Path("view")
  public TemplateInstance list(@Context SecurityContext ctx) {
    var username = ctx.getUserPrincipal().getName();
    var current = users.findByUsername(username).orElseThrow();
    var mine = taskService.listFor(current);
    return Tpl.tasks(mine, identity);
  }
}
