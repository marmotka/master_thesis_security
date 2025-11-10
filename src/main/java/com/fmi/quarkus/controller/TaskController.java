@Path("/tasks")
@Produces(MediaType.TEXT_HTML)
@RolesAllowed({"USER","ADMIN"})
public class TaskController {
  @Inject UserService users;
  @Inject TaskService taskService;

  @CheckedTemplate
  public static class Tpl { public static native TemplateInstance tasks(java.util.List<?> tasks); }

  @GET @Path("view")
  public TemplateInstance list(@Context SecurityContext ctx) {
    var username = ctx.getUserPrincipal().getName();
    var current = users.findByUsername(username).orElseThrow();
    var mine = taskService.listFor(current);
    return Tpl.tasks(mine);
  }
}
