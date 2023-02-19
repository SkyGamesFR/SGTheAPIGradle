package fr.skygames.sgtheapi;

import com.fasterxml.jackson.core.JsonParser;
import fr.skygames.sgtheapi.api.data.Discord;
import fr.skygames.sgtheapi.data.DiscordService;
import fr.skygames.sgtheapi.security.SecureRouter;
import fr.skygames.sgtheapi.sql.SqlConnector;
import fr.skygames.sgtheapi.utils.HttpLogUtils;
import fr.skygames.sgtheapi.utils.MissingPropertyException;
import io.vertx.core.Vertx;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.core.streams.ReadStream;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

import javax.sound.midi.Soundbank;
import java.sql.SQLException;

public class DiscordResource {

    private SqlConnector connector;
    private DiscordService service;

    public DiscordResource(SqlConnector connector) {
        this.service = new DiscordService(connector);
        this.connector = connector;
    }

    public Router getSubRouter(final Vertx vertx) {
        final SecureRouter subRouter = new SecureRouter(vertx);

        subRouter.secureGet("/discord/:player/token").handler(this::getDiscordToken);
        subRouter.securePost("/discord/:player/token").handler(this::setDiscordToken);

        subRouter.secureGet("/discord/:token/verify").handler(this::verifyDiscordToken);

        subRouter.securePatch("/discord/:token/id").handler(this::setDiscordID);
        subRouter.get("/discord/:player/id").handler(this::getDiscordID);

        return subRouter;
    }

    private void getDiscordToken(final RoutingContext routingContext) {
        SkyGamesTheApp.LOGGER.info(HttpLogUtils.logHttpRequestRemote(routingContext.request()) + " pathParam : " + routingContext.pathParams());
        String param = routingContext.pathParam("player").toLowerCase();

        try {
            Discord discord = service.getTokenFromUUID(param);

            if (discord != null) {
                final JsonObject jsonResponse = new JsonObject();
                jsonResponse.put("token", discord.getToken());
                routingContext.response()
                        .setStatusCode(200)
                        .putHeader("content-type", "application/json; charset=utf-8")
                        .end(jsonResponse.encodePrettily());
            } else {
                SkyGamesTheApp.LOGGER.warn(HttpLogUtils.logHttpRequestRemote(routingContext.request()) + " token[uuid=\"" + param + "\"] can't be found !");
                final JsonObject errorJsonResponse = new JsonObject();
                errorJsonResponse.put("error", "No token can be found for this specified prameter : " + param);
                errorJsonResponse.put("token", "null");
                routingContext.response()
                        .setStatusCode(404)
                        .putHeader("content-type", "application/json")
                        .end(Json.encode(errorJsonResponse));
            }
        } catch (Exception e) {
            SkyGamesTheApp.LOGGER.error(HttpLogUtils.logHttpRequestRemote(routingContext.request()) + " Internal Server Error : DiscordService getPlayer SqlConnector Exception !", e);

            final JsonObject errorJsonResponse = new JsonObject();
            errorJsonResponse.put("error", "SqlConnector exception");

            routingContext.response()
                    .setStatusCode(500)
                    .putHeader("content-type", "application/json")
                    .end(Json.encode(errorJsonResponse));
        }
    }

    private void setDiscordToken(final RoutingContext routingContext) {
        SkyGamesTheApp.LOGGER.info(HttpLogUtils.logHttpRequestRemote(routingContext.request()) + " pathParam : " + routingContext.pathParams());
        String param = routingContext.pathParam("player").toLowerCase();

        try {
            Discord discord = service.getTokenFromUUID(param);

            if (discord != null) {
                SkyGamesTheApp.LOGGER.warn(HttpLogUtils.logHttpRequestRemote(routingContext.request()) + " token[uuid=\"" + param + "\"] already exists !");
                final JsonObject errorJsonResponse = new JsonObject();
                errorJsonResponse.put("error", "Token already exists for this specified prameter : " + param);
                errorJsonResponse.put("token", discord.getToken());
                routingContext.response()
                        .setStatusCode(409)
                        .putHeader("content-type", "application/json")
                        .end(Json.encode(errorJsonResponse));
            } else {
                String token = routingContext.getBodyAsJson().getString("token");
                service.create(new Discord(param, token));
                final JsonObject jsonResponse = new JsonObject();
                jsonResponse.put("token", token);
                routingContext.response()
                        .setStatusCode(201)
                        .putHeader("content-type", "application/json; charset=utf-8")
                        .end(jsonResponse.encodePrettily());
            }
        } catch (Exception e) {
            SkyGamesTheApp.LOGGER.error(HttpLogUtils.logHttpRequestRemote(routingContext.request()) + " Internal Server Error : DiscordService getPlayer SqlConnector Exception !", e);

            final JsonObject errorJsonResponse = new JsonObject();
            errorJsonResponse.put("error", "SqlConnector exception");

            routingContext.response()
                    .setStatusCode(500)
                    .putHeader("content-type", "application/json")
                    .end(Json.encode(errorJsonResponse));
        }
    }

    private void verifyDiscordToken(RoutingContext routingContext) {
        SkyGamesTheApp.LOGGER.info(HttpLogUtils.logHttpRequestRemote(routingContext.request()) + " pathParam : " + routingContext.pathParams());
        String param = routingContext.pathParam("token").toLowerCase();

        try {
            Discord discord = service.getToken(param);

            if (discord != null) {
                final JsonObject jsonResponse = new JsonObject();
                jsonResponse.put("uuid", discord.getUuid());
                routingContext.response()
                        .setStatusCode(200)
                        .putHeader("content-type", "application/json; charset=utf-8")
                        .end(jsonResponse.encodePrettily());
            } else {
                SkyGamesTheApp.LOGGER.warn(HttpLogUtils.logHttpRequestRemote(routingContext.request()) + " token[token=\"" + param + "\"] can't be found !");
                final JsonObject errorJsonResponse = new JsonObject();
                errorJsonResponse.put("error", "No token can be found for this specified prameter : " + param);
                errorJsonResponse.put("player", "null");
                routingContext.response()
                        .setStatusCode(404)
                        .putHeader("content-type", "application/json")
                        .end(Json.encode(errorJsonResponse));
            }
        } catch (Exception e) {
            SkyGamesTheApp.LOGGER.error(HttpLogUtils.logHttpRequestRemote(routingContext.request()) + " Internal Server Error : DiscordService getPlayer SqlConnector Exception !", e);

            final JsonObject errorJsonResponse = new JsonObject();
            errorJsonResponse.put("error", "SqlConnector exception");

            routingContext.response()
                    .setStatusCode(500)
                    .putHeader("content-type", "application/json")
                    .end(Json.encode(errorJsonResponse));
        }

    }

    private void getDiscordID(RoutingContext routingContext) {
        SkyGamesTheApp.LOGGER.info(HttpLogUtils.logHttpRequestRemote(routingContext.request()) + " pathParam : " + routingContext.pathParams());
        String param = routingContext.pathParam("player").toLowerCase();

        try {
            Discord discord = service.getIDFromUUID(param);

            if (discord != null) {
                final JsonObject jsonResponse = new JsonObject();
                jsonResponse.put("id", discord.getId());
                routingContext.response()
                        .setStatusCode(200)
                        .putHeader("content-type", "application/json; charset=utf-8")
                        .end(jsonResponse.encodePrettily());
            } else {
                SkyGamesTheApp.LOGGER.warn(HttpLogUtils.logHttpRequestRemote(routingContext.request()) + " token[uuid=\"" + param + "\"] can't be found !");
                final JsonObject errorJsonResponse = new JsonObject();
                errorJsonResponse.put("error", "No token can be found for this specified prameter : " + param);
                errorJsonResponse.put("id", "null");
                routingContext.response()
                        .setStatusCode(404)
                        .putHeader("content-type", "application/json")
                        .end(Json.encode(errorJsonResponse));
            }
        } catch (Exception e) {
            SkyGamesTheApp.LOGGER.error(HttpLogUtils.logHttpRequestRemote(routingContext.request()) + " Internal Server Error : DiscordService getPlayer SqlConnector Exception !", e);

            final JsonObject errorJsonResponse = new JsonObject();
            errorJsonResponse.put("error", "SqlConnector exception");

            routingContext.response()
                    .setStatusCode(500)
                    .putHeader("content-type", "application/json")
                    .end(Json.encode(errorJsonResponse));
        }
    }

    private void setDiscordID(RoutingContext routingContext) {
        SkyGamesTheApp.LOGGER.info(HttpLogUtils.logHttpRequestRemote(routingContext.request()) + " pathParam : " + routingContext.pathParams());

        final String body = routingContext.getBodyAsString();
        final String param = routingContext.pathParam("token").toLowerCase();

        try {
            final JsonObject jsonBody = new JsonObject(body);
            final String id = jsonBody.getString("discord_id");

            try {
                Discord discord = service.setIDFromToken(param, id);
                if(discord != null) {
                    final JsonObject jsonResponse = new JsonObject();
                    jsonResponse.put("discord_id", discord.getId());
                    routingContext.response()
                            .setStatusCode(200)
                            .putHeader("content-type", "application/json; charset=utf-8")
                            .end(jsonResponse.encodePrettily());
                } else {
                    SkyGamesTheApp.LOGGER.warn(HttpLogUtils.logHttpRequestRemote(routingContext.request()) + " token[token=\"" + param + "\"] can't be found !");
                    final JsonObject errorJsonResponse = new JsonObject();
                    errorJsonResponse.put("error", "No token can be found for this specified prameter : " + param);
                    errorJsonResponse.put("id", "null");
                    routingContext.response()
                            .setStatusCode(404)
                            .putHeader("content-type", "application/json")
                            .end(Json.encode(errorJsonResponse));
                }
            } catch (ClassNotFoundException | SQLException | MissingPropertyException e) {
                SkyGamesTheApp.LOGGER.error(HttpLogUtils.logHttpRequestRemote(routingContext.request()) + " Internal Server Error : DiscordService getPlayer SqlConnector Exception !", e);

                final JsonObject errorJsonResponse = new JsonObject();
                errorJsonResponse.put("error", "SqlConnector exception");

                routingContext.response()
                        .setStatusCode(500)
                        .putHeader("content-type", "application/json")
                        .end(Json.encode(errorJsonResponse));
            }

        } catch (Exception e) {
            SkyGamesTheApp.LOGGER.error(HttpLogUtils.logHttpRequestRemote(routingContext.request()) + " Internal Server Error : DiscordService getPlayer SqlConnector Exception !", e);

            final JsonObject errorJsonResponse = new JsonObject();
            errorJsonResponse.put("error", "SqlConnector exception");

            routingContext.response()
                    .setStatusCode(500)
                    .putHeader("content-type", "application/json")
                    .end(Json.encode(errorJsonResponse));
        }
    }
}
