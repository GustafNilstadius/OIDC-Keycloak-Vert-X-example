package com.gustafn.redpill.linpro.bbl.oidc.demo;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.ext.auth.User;
import io.vertx.ext.auth.oauth2.OAuth2Auth;
import io.vertx.ext.auth.oauth2.OAuth2Options;
import io.vertx.ext.auth.oauth2.providers.KeycloakAuth;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.*;
import io.vertx.ext.web.sstore.LocalSessionStore;


public class MainVerticle extends AbstractVerticle {

  private static final String HOST = "http://localhost:8888";
  private static final String CALLBACK_URI = "/callback";

  private OAuth2Auth oAuth2Auth;

  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    Router router = Router.router(vertx);
    LocalSessionStore localSessionStore = LocalSessionStore.create(vertx);
    SessionHandler sessionHandler = SessionHandler.create(localSessionStore);
    router.route().handler(LoggerHandler.create(LoggerFormat.SHORT));
    router.route().handler(BodyHandler.create());
    router.route().handler(sessionHandler);

    OAuth2Options clientOptions = new OAuth2Options()
      .setClientId(config().getString("OIDC_CLIENT"))
      .setClientSecret(config().getString("OIDC_CLIENT_SECRET"))
      .setSite(config().getString("OIDC_DISCOVERY_URL", "http://localhost:8989/realms/dev"));

    KeycloakAuth.discover(
      vertx,
      clientOptions)
      .onSuccess(oAuth2Auth -> {
        System.out.println("Keycloak discovery complete.");
        this.oAuth2Auth = oAuth2Auth;
        try {
          Route callbackRoute = router.get(CALLBACK_URI);
          OAuth2AuthHandler oauth2handler = OAuth2AuthHandler.create(vertx, oAuth2Auth, HOST + CALLBACK_URI)
            // Additional scopes: openid for OpenID Connect, tells the Authorization server that we are doing OIDC and not OAuth
            .withScope("openid")
            .setupCallback(callbackRoute);


          router.route("/logout").handler(this::handleLogout);
          router.route("/private/*").handler(oauth2handler);
          router.route("/private/account").handler(routingContext -> {
            System.out.println(routingContext.user().principal().encodePrettily());
            routingContext.response()
              .setStatusCode(200)
              .putHeader("Content-Type", "application/json")
              .end(routingContext.user().attributes().encodePrettily());
          });
          router.get().handler(StaticHandler.create("www"));
        } catch (Exception e) {
          e.printStackTrace();
        }


        vertx.createHttpServer().requestHandler(router).listen(8888, http -> {
          if (http.succeeded()) {
            startPromise.complete();
            System.out.println("HTTP server started on port 8888");
          } else {
            startPromise.fail(http.cause());
          }
        });
      })
      .onFailure(startPromise::fail);
  }

  private void handleLogout(RoutingContext ctx) {
    User user = ctx.user();

    ctx.session().destroy();
    ctx.response()
      .setStatusCode(302)
      .putHeader("location", oAuth2Auth.endSessionURL(user))
      .end();
  }
}
