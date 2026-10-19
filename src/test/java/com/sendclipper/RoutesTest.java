package com.sendclipper;

import static com.sendclipper.FakeApi.list;
import static com.sendclipper.FakeApi.map;
import static com.sendclipper.FakeApi.ok;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.sendclipper.model.CreateContactParams;
import com.sendclipper.model.CreateDomainParams;
import com.sendclipper.model.CreateMailboxParams;
import com.sendclipper.model.CreateSuppressionParams;
import com.sendclipper.model.CreateWebhookParams;
import com.sendclipper.model.ExportContactsParams;
import com.sendclipper.model.ListMailboxMessagesParams;
import com.sendclipper.model.ListSandboxMessagesParams;
import com.sendclipper.model.ListTemplatesParams;
import com.sendclipper.model.ListWebhookDispatchesParams;
import com.sendclipper.model.Region;
import com.sendclipper.model.ReplayAllParams;
import com.sendclipper.model.SandboxParams;
import com.sendclipper.model.UpdateMailboxParams;
import com.sendclipper.model.UpdateTemplateParams;
import com.sendclipper.model.UpdateWebhookParams;
import com.sendclipper.model.WebhookEventType;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

/** Every method reaches the route the API serves, and every write carries an Idempotency-Key. */
class RoutesTest {
  /** Routes that answer a JSON array on GET. */
  static final List<String> LIST_ROUTES =
      Arrays.asList(
          "/v1/domains", "/v1/templates", "/v1/templates/projects", "/v1/templates/tags", "/v1/templates/assets",
          "/v1/sandboxes", "/v1/sandboxes/s1/messages", "/v1/webhook-endpoints", "/v1/webhook-deliveries",
          "/v1/api-keys", "/v1/mailboxes");

  static final class Route {
    final String method;
    final String path;
    final Consumer<SendClipper> call;

    Route(String method, String path, Consumer<SendClipper> call) {
      this.method = method;
      this.path = path;
      this.call = call;
    }
  }

  static Route r(String method, String path, Consumer<SendClipper> call) {
    return new Route(method, path, call);
  }

  @TestFactory
  Stream<DynamicTest> everyMethodReachesTheRouteTheApiServes() {
    List<Route> routes =
        Arrays.asList(
            r("GET", "/v1/emails/e1", c -> c.emails().get("e1")),
            r("GET", "/v1/emails", c -> c.emails().list(null)),
            r("GET", "/v1/emails/stats/summary", c -> c.emails().stats()),
            r("GET", "/v1/domains", c -> c.domains().list()),
            r("POST", "/v1/domains", c -> c.domains().create(CreateDomainParams.builder().name("send.acme.io").region(Region.EU_WEST_1).build())),
            r("GET", "/v1/domains/d1/records", c -> c.domains().records("d1")),
            r("POST", "/v1/domains/d1/verify", c -> c.domains().verify("d1")),
            r("POST", "/v1/domains/d1/receiving", c -> c.domains().enableReceiving("d1")),
            r("DELETE", "/v1/domains/d1/receiving", c -> c.domains().disableReceiving("d1")),
            r("PATCH", "/v1/domains/d1/region", c -> c.domains().setRegion("d1", Region.US_EAST_1)),
            r("GET", "/v1/domains/d1/deletion", c -> c.domains().deletionPreview("d1")),
            r("DELETE", "/v1/domains/d1", c -> c.domains().remove("d1")),
            r("GET", "/v1/templates", c -> c.templates().list(ListTemplatesParams.builder().tag("t1").build())),
            r("GET", "/v1/templates/t1", c -> c.templates().get("t1")),
            r("GET", "/v1/templates/t1/html", c -> c.templates().html("t1")),
            r("POST", "/v1/templates", c -> c.templates().create(null)),
            r("PATCH", "/v1/templates/t1", c -> c.templates().update("t1", UpdateTemplateParams.builder().name("n").build())),
            r("DELETE", "/v1/templates/t1", c -> c.templates().remove("t1")),
            r("POST", "/v1/templates/t1/duplicate", c -> c.templates().duplicate("t1")),
            r("POST", "/v1/templates/t1/publish", c -> c.templates().publish("t1")),
            r("POST", "/v1/templates/t1/unpublish", c -> c.templates().unpublish("t1")),
            r("POST", "/v1/templates/t1/preview", c -> c.templates().preview("t1", map("first_name", "Ada"))),
            r("PUT", "/v1/templates/t1/tags", c -> c.templates().setTags("t1", Collections.singletonList("g1"))),
            r("GET", "/v1/templates/projects", c -> c.templates().projects()),
            r("GET", "/v1/templates/tags", c -> c.templates().tags().list()),
            r("POST", "/v1/templates/tags", c -> c.templates().tags().create("n", "blue")),
            r("PATCH", "/v1/templates/tags/g1", c -> c.templates().tags().update("g1", null, "red")),
            r("DELETE", "/v1/templates/tags/g1", c -> c.templates().tags().remove("g1")),
            r("GET", "/v1/templates/assets", c -> c.templates().assets().list()),
            r("GET", "/v1/sandboxes", c -> c.sandboxes().list()),
            r("GET", "/v1/sandboxes/s1", c -> c.sandboxes().get("s1")),
            r("POST", "/v1/sandboxes", c -> c.sandboxes().create(SandboxParams.builder().name("n").build())),
            r("PATCH", "/v1/sandboxes/s1", c -> c.sandboxes().update("s1", SandboxParams.builder().name("n").build())),
            r("DELETE", "/v1/sandboxes/s1", c -> c.sandboxes().remove("s1")),
            r("GET", "/v1/sandboxes/s1/credentials", c -> c.sandboxes().credentials("s1")),
            r("POST", "/v1/sandboxes/s1/credentials", c -> c.sandboxes().resetCredentials("s1")),
            r("GET", "/v1/sandboxes/s1/messages", c -> c.sandboxes().messages().list("s1", ListSandboxMessagesParams.builder().search("x").build())),
            r("GET", "/v1/sandboxes/s1/messages/m1", c -> c.sandboxes().messages().get("s1", "m1")),
            r("GET", "/v1/sandboxes/s1/messages/m1/raw", c -> c.sandboxes().messages().raw("s1", "m1")),
            r("DELETE", "/v1/sandboxes/s1/messages/m1", c -> c.sandboxes().messages().remove("s1", "m1")),
            r("DELETE", "/v1/sandboxes/s1/messages", c -> c.sandboxes().messages().clear("s1")),
            r("POST", "/v1/sandboxes/s1/read-all", c -> c.sandboxes().messages().markAllRead("s1")),
            r("GET", "/v1/webhook-endpoints", c -> c.webhooks().list()),
            r("POST", "/v1/webhook-endpoints", c -> c.webhooks().create(CreateWebhookParams.builder().url("https://x.io").events(WebhookEventType.EMAIL_DELIVERED).build())),
            r("PATCH", "/v1/webhook-endpoints/w1", c -> c.webhooks().update("w1", UpdateWebhookParams.builder().active(false).build())),
            r("DELETE", "/v1/webhook-endpoints/w1", c -> c.webhooks().remove("w1")),
            r("POST", "/v1/webhook-endpoints/w1/rotate-secret", c -> c.webhooks().rotateSecret("w1")),
            r("GET", "/v1/webhook-deliveries", c -> c.webhooks().deliveries(null)),
            r("GET", "/v1/webhook-dispatches", c -> c.webhooks().dispatches().list(ListWebhookDispatchesParams.builder().state("all").build())),
            r("GET", "/v1/webhook-dispatches/x1", c -> c.webhooks().dispatches().get("x1")),
            r("POST", "/v1/webhook-dispatches/x1/replay", c -> c.webhooks().dispatches().replay("x1")),
            r("POST", "/v1/webhook-dispatches/replay-all", c -> c.webhooks().dispatches().replayAll(ReplayAllParams.builder().endpointId("w1").build())),
            r("GET", "/v1/contacts", c -> c.contacts().list(null)),
            r("POST", "/v1/contacts", c -> c.contacts().create(CreateContactParams.builder().name("Ada").email("a@x.io").build())),
            r("GET", "/v1/contacts/export", c -> c.contacts().export(ExportContactsParams.builder().format("vcf").build())),
            r("GET", "/v1/audiences", c -> c.audiences().list()),
            r("GET", "/v1/suppressions", c -> c.suppressions().list(null)),
            r("POST", "/v1/suppressions", c -> c.suppressions().create(CreateSuppressionParams.builder().email("a@x.io").build())),
            r("GET", "/v1/suppressions/export", c -> c.suppressions().export(null)),
            r("GET", "/v1/mailboxes", c -> c.mailboxes().list()),
            r("GET", "/v1/mailboxes/mb1", c -> c.mailboxes().get("mb1")),
            r("POST", "/v1/mailboxes", c -> c.mailboxes().create(CreateMailboxParams.builder().domainId("d1").localPart("support").build())),
            r("PATCH", "/v1/mailboxes/mb1", c -> c.mailboxes().update("mb1", UpdateMailboxParams.builder().archived(true).build())),
            r("DELETE", "/v1/mailboxes/mb1", c -> c.mailboxes().archive("mb1")),
            r("GET", "/v1/mailboxes/mb1/messages", c -> c.mailboxes().messages().list("mb1", ListMailboxMessagesParams.builder().folder("inbox").build())),
            r("GET", "/v1/mailboxes/mb1/messages/m1", c -> c.mailboxes().messages().get("mb1", "m1")),
            r("POST", "/v1/automations/a1/enrollments", c -> c.automations().enroll("a1", "ada@x.io")),
            r("GET", "/v1/workspace", c -> c.workspace().get()),
            r("GET", "/v1/api-keys", c -> c.apiKeys().list()));
    return routes.stream()
        .map(
            route ->
                DynamicTest.dynamicTest(
                    route.method + " " + route.path,
                    () -> {
                      Object body = route.method.equals("GET") && LIST_ROUTES.contains(route.path) ? list() : map();
                      try (FakeApi f = new FakeApi(ok(body))) {
                        route.call.accept(f.client());
                        assertEquals(route.method, f.call(0).method);
                        assertEquals(route.path, f.call(0).path);
                        if (!route.method.equals("GET")) {
                          assertNotNull(f.call(0).header("Idempotency-Key"), "no Idempotency-Key on a write");
                        }
                      }
                    }));
  }
}
