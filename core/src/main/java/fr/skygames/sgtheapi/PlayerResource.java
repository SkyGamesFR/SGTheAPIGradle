package fr.skygames.sgtheapi;

import java.sql.SQLException;
import java.util.List;

import fr.skygames.sgtheapi.api.data.Discord;
import fr.skygames.sgtheapi.api.data.Player;
import fr.skygames.sgtheapi.data.DiscordService;
import fr.skygames.sgtheapi.data.PlayerService;
import fr.skygames.sgtheapi.data.TeamService;
import fr.skygames.sgtheapi.security.SecureRouter;
import fr.skygames.sgtheapi.sql.SqlConnector;
import fr.skygames.sgtheapi.utils.HttpLogUtils;
import fr.skygames.sgtheapi.utils.MissingPropertyException;
import io.vertx.core.Vertx;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

public class PlayerResource {

    private SqlConnector connector;
    private PlayerService playerService;

    public PlayerResource(SqlConnector connector) {
        this.playerService = new PlayerService(connector);
        this.connector = connector;
    }

    public Router getSubRouter(final Vertx vertx) {
        final SecureRouter subRouter = SecureRouter.router(vertx);

        // Routes
        subRouter.get("/players").handler(this::getAllPlayers); // show all players 
        subRouter.securePost("/players").handler(this::addPlayer); // secure, add new player (on first connect)

        subRouter.get("/players/:player").handler(this::getPlayer); // get player detail
        subRouter.secureDelete("/players/:player").handler(this::deletePlayer); // secure, delete player
        subRouter.securePatch("/players/:player").handler(this::updatePlayer); // secure, update player

        subRouter.get("/players/:player/team").handler(this::getPlayerTeam); // get player team
        subRouter.securePatch("/players/:player/team").handler(this::updatePlayerTeam); // update player team

        return subRouter;
    }

    private void getAllPlayers(final RoutingContext routingContext) {
        SkyGamesTheApp.LOGGER.info(HttpLogUtils.logHttpRequestRemote(routingContext.request()));

        try {
            List<Player> players = playerService.getAll();

            final JsonObject jsonResponse = new JsonObject();
            jsonResponse.put("players", players);
            routingContext.response()
                    .setStatusCode(200)
                    .putHeader("content-type", "application/json")
                    .end(Json.encode(jsonResponse));

        } catch (Exception e) {
            SkyGamesTheApp.LOGGER.error(HttpLogUtils.logHttpRequestRemote(routingContext.request()) + " Internal Server Error : PlayerService getAll SqlConnector Exception !", e);

            final JsonObject errorJsonResponse = new JsonObject();
            errorJsonResponse.put("error", "SqlConnector exception");

            routingContext.response()
                    .setStatusCode(500)
                    .putHeader("content-type", "application/json")
                    .end(Json.encode(errorJsonResponse));

        }
    }

    private void getPlayer(final RoutingContext routingContext) {
        SkyGamesTheApp.LOGGER.info(HttpLogUtils.logHttpRequestRemote(routingContext.request()) + " pathParam : " + routingContext.pathParams());
        String param = routingContext.pathParam("player").toLowerCase();

        try {
            Player player = playerService.get(param);

            if (player != null) {
                final JsonObject jsonResponse = new JsonObject();
                jsonResponse.put("player", JsonObject.mapFrom(player));
                routingContext.response()
                        .setStatusCode(200)
                        .putHeader("content-type", "application/json")
                        .end(Json.encode(jsonResponse));
            } else {
                SkyGamesTheApp.LOGGER.warn(HttpLogUtils.logHttpRequestRemote(routingContext.request()) + " player[uuid=\"" + param + "\"] can't be found !");
                final JsonObject errorJsonResponse = new JsonObject();
                errorJsonResponse.put("error", "No player can be found for the specified parameter : " + param);
                errorJsonResponse.put("player", param);
                routingContext.response()
                        .setStatusCode(404)
                        .putHeader("content-type", "application/json")
                        .end(Json.encode(errorJsonResponse));
            }
        } catch (Exception e) {
            SkyGamesTheApp.LOGGER.error(HttpLogUtils.logHttpRequestRemote(routingContext.request()) + " Internal Server Error : PlayerService getPlayer SqlConnector Exception !", e);

            final JsonObject errorJsonResponse = new JsonObject();
            errorJsonResponse.put("error", "SqlConnector exception");

            routingContext.response()
                    .setStatusCode(500)
                    .putHeader("content-type", "application/json")
                    .end(Json.encode(errorJsonResponse));
        }
    }

    private void addPlayer(final RoutingContext routingContext) {
        SkyGamesTheApp.LOGGER.info(HttpLogUtils.logHttpRequestRemote(routingContext.request()));
        final String body = routingContext.getBodyAsString();
        try {
            final Player decodedPlayer = Json.decodeValue(body, Player.class);
            try {
                String uuid = decodedPlayer.getUuid().toLowerCase();
                Player player = playerService.get(uuid);
                if (player == null) {
                    player = this.playerService.add(uuid, decodedPlayer.getName());
                    final JsonObject jsonResponse = new JsonObject();
                    jsonResponse.put("added-player", JsonObject.mapFrom(player));
                    routingContext.response()
                            .setStatusCode(201)
                            .putHeader("content-type", "application/json")
                            .end(Json.encode(jsonResponse));
                } else {
                    SkyGamesTheApp.LOGGER.warn(HttpLogUtils.logHttpRequestRemote(routingContext.request()) + " player[uuid=\"" + decodedPlayer.getUuid() + "\"] already exist !");
                    final JsonObject errorJsonResponse = new JsonObject();
                    errorJsonResponse.put("error", "Player already exist !");
                    errorJsonResponse.put("founded-player", JsonObject.mapFrom(player));

                    routingContext.response()
                            .setStatusCode(500)
                            .putHeader("content-type", "application/json")
                            .end(Json.encode(errorJsonResponse));
                }
            } catch (ClassNotFoundException | SQLException | MissingPropertyException e) {
                SkyGamesTheApp.LOGGER.error(HttpLogUtils.logHttpRequestRemote(routingContext.request()) + " Internal Server Error : PlayerService addPlayer(getPlayer) SqlConnector Exception !", e);

                final JsonObject errorJsonResponse = new JsonObject();
                errorJsonResponse.put("error", "SqlConnector exception");

                routingContext.response()
                        .setStatusCode(500)
                        .putHeader("content-type", "application/json")
                        .end(Json.encode(errorJsonResponse));
            }

        } catch (DecodeException e) {
            SkyGamesTheApp.LOGGER.error(HttpLogUtils.logHttpRequestRemote(routingContext.request()) + " Internal Server Error : PlayerService addPlayer Invalide body / Json decode exception !", e);
            final JsonObject errorJsonResponse = new JsonObject();
            errorJsonResponse.put("error", "Json decode exception");
            errorJsonResponse.put("invalid-body-request", body);

            routingContext.response()
                    .setStatusCode(400)
                    .putHeader("content-type", "application/json")
                    .end(Json.encode(errorJsonResponse));
        }
    }

    private void deletePlayer(final RoutingContext routingContext) {
        SkyGamesTheApp.LOGGER.info(HttpLogUtils.logHttpRequestRemote(routingContext.request()) + " pathParam : " + routingContext.pathParams());
        String param = routingContext.pathParam("player").toLowerCase();

        try {
            Player player = playerService.get(param);
            if (player != null) {
                int result = this.playerService.delete(param);
                if (result == 1) {
                    final JsonObject jsonResponse = new JsonObject();
                    jsonResponse.put("result", "OK");
                    routingContext.response()
                            .setStatusCode(200)
                            .putHeader("content-type", "application/json")
                            .end(Json.encode(jsonResponse));
                } else {
                    // message bizarre
                    SkyGamesTheApp.LOGGER.warn(HttpLogUtils.logHttpRequestRemote(routingContext.request()) + " player[uuid=\"" + param + "\"] sql return nothing.");
                }
            } else {
                SkyGamesTheApp.LOGGER.warn(HttpLogUtils.logHttpRequestRemote(routingContext.request()) + " player[uuid=\"" + param + "\"] can't be found !");
                final JsonObject errorJsonResponse = new JsonObject();
                errorJsonResponse.put("error", "No player can be found for the specified parameter : " + param);
                errorJsonResponse.put("player", param);
                routingContext.response()
                        .setStatusCode(404)
                        .putHeader("content-type", "application/json")
                        .end(Json.encode(errorJsonResponse));
            }
        } catch (ClassNotFoundException | SQLException | MissingPropertyException e) {
            SkyGamesTheApp.LOGGER.error(HttpLogUtils.logHttpRequestRemote(routingContext.request()) + "Internal Server Error : PlayerService deletePlayer(getPlayer) SqlConnector Exception !", e);

            final JsonObject errorJsonResponse = new JsonObject();
            errorJsonResponse.put("error", "SqlConnector exception");

            routingContext.response()
                    .setStatusCode(500)
                    .putHeader("content-type", "application/json")
                    .end(Json.encode(errorJsonResponse));
        }
    }

    private void updatePlayer(final RoutingContext routingContext) {
        SkyGamesTheApp.LOGGER.info(HttpLogUtils.logHttpRequestRemote(routingContext.request()) + " pathParam : " + routingContext.pathParams());

        final String body = routingContext.getBodyAsString();
        final String param = routingContext.pathParam("player").toLowerCase();

        try {
            final Player decodedPlayer = Json.decodeValue(body, Player.class);
            try {
                Player player = playerService.get(param);
                if (player != null) {

                    player = this.playerService.update(param, decodedPlayer.getName(), decodedPlayer.getLast_login());
                    final JsonObject jsonResponse = new JsonObject();
                    jsonResponse.put("updated-player", JsonObject.mapFrom(player));
                    routingContext.response()
                            .setStatusCode(200)
                            .putHeader("content-type", "application/json")
                            .end(Json.encode(jsonResponse));

                } else {
                    SkyGamesTheApp.LOGGER.warn(HttpLogUtils.logHttpRequestRemote(routingContext.request()) + " player[uuid=\"" + param + "\"] can't be found !");
                    final JsonObject errorJsonResponse = new JsonObject();
                    errorJsonResponse.put("error", "No player can be found for the specified parameter : " + param);
                    errorJsonResponse.put("player", param);
                    routingContext.response()
                            .setStatusCode(404)
                            .putHeader("content-type", "application/json")
                            .end(Json.encode(errorJsonResponse));
                }
            } catch (ClassNotFoundException | SQLException | MissingPropertyException e) {
                SkyGamesTheApp.LOGGER.error(HttpLogUtils.logHttpRequestRemote(routingContext.request()) + " Internal Server Error : PlayerService updatePlayer(getPlayer) SqlConnector Exception !", e);

                final JsonObject errorJsonResponse = new JsonObject();
                errorJsonResponse.put("error", "SqlConnector exception");

                routingContext.response()
                        .setStatusCode(500)
                        .putHeader("content-type", "application/json")
                        .end(Json.encode(errorJsonResponse));
            }

        } catch (DecodeException e) {
            SkyGamesTheApp.LOGGER.error(HttpLogUtils.logHttpRequestRemote(routingContext.request()) + " Internal Server Error : PlayerService updatePlayer  Json decode exception !", e);

            final JsonObject errorJsonResponse = new JsonObject();
            errorJsonResponse.put("error", "Json decode exception");
            errorJsonResponse.put("invalid-body-request", body);

            routingContext.response()
                    .setStatusCode(400)
                    .putHeader("content-type", "application/json")
                    .end(Json.encode(errorJsonResponse));
        }
    }

    private void getPlayerTeam(final RoutingContext routingContext) {
        SkyGamesTheApp.LOGGER.info(HttpLogUtils.logHttpRequestRemote(routingContext.request()) + " pathParam : " + routingContext.pathParams());
        String param = routingContext.pathParam("player").toLowerCase();

        try {
            String team = playerService.getTeam(param);

            if (team != null) {

                final JsonObject jsonResponse = new JsonObject();
                jsonResponse.put("team", team);
                routingContext.response()
                        .setStatusCode(200)
                        .putHeader("content-type", "application/json")
                        .end(Json.encode(jsonResponse));

            } else {
                SkyGamesTheApp.LOGGER.warn(HttpLogUtils.logHttpRequestRemote(routingContext.request()) + " player[uuid=\"" + param + "\"] can't be found !");
                final JsonObject errorJsonResponse = new JsonObject();
                errorJsonResponse.put("error", "No player can be found for the specified parameter : " + param);
                errorJsonResponse.put("player", param);
                routingContext.response()
                        .setStatusCode(404)
                        .putHeader("content-type", "application/json")
                        .end(Json.encode(errorJsonResponse));
            }
        } catch (Exception e) {
            SkyGamesTheApp.LOGGER.error(HttpLogUtils.logHttpRequestRemote(routingContext.request()) + " Internal Server Error : PlayerService getPlayerTeam SqlConnector Exception !", e);

            final JsonObject errorJsonResponse = new JsonObject();
            errorJsonResponse.put("error", "SqlConnector exception");

            routingContext.response()
                    .setStatusCode(500)
                    .putHeader("content-type", "application/json")
                    .end(Json.encode(errorJsonResponse));
        }
    }

    private void updatePlayerTeam(final RoutingContext routingContext) {
        SkyGamesTheApp.LOGGER.info(HttpLogUtils.logHttpRequestRemote(routingContext.request()) + " pathParam : " + routingContext.pathParams());

        final String body = routingContext.getBodyAsString();
        final String param = routingContext.pathParam("player").toLowerCase();

        try {
            final JsonObject jsonObject = new JsonObject(body);
            if (!jsonObject.containsKey("team")) {
                throw new DecodeException("Missing team field !");
            }
            final String decodedString = jsonObject.getString("team").toLowerCase();

            try {

                if (!decodedString.equals("none") && this.checkTeam(decodedString)) {

                    String team = playerService.getTeam(param);

                    if (team != null) {

                        team = this.playerService.updateTeam(param, decodedString);
                        final JsonObject jsonResponse = new JsonObject();
                        jsonResponse.put("updated-team", team);
                        routingContext.response()
                                .setStatusCode(200)
                                .putHeader("content-type", "application/json")
                                .end(Json.encode(jsonResponse));

                    } else {
                        SkyGamesTheApp.LOGGER.warn(HttpLogUtils.logHttpRequestRemote(routingContext.request()) + " player[uuid=\"" + param + "\"] can't be found !");
                        final JsonObject errorJsonResponse = new JsonObject();
                        errorJsonResponse.put("error", "No player can be found for the specified parameter : " + param);
                        errorJsonResponse.put("player", param);
                        routingContext.response()
                                .setStatusCode(404)
                                .putHeader("content-type", "application/json")
                                .end(Json.encode(errorJsonResponse));
                    }

                } else {
                    SkyGamesTheApp.LOGGER.warn(HttpLogUtils.logHttpRequestRemote(routingContext.request()) + " team[id=\"" + param + "\"] can't be found !");
                    final JsonObject errorJsonResponse = new JsonObject();
                    errorJsonResponse.put("error", "No team can be found for the specified parameter : " + param);
                    routingContext.response()
                            .setStatusCode(404)
                            .putHeader("content-type", "application/json")
                            .end(Json.encode(errorJsonResponse));
                }

            } catch (ClassNotFoundException | SQLException | MissingPropertyException e) {
                SkyGamesTheApp.LOGGER.error(HttpLogUtils.logHttpRequestRemote(routingContext.request()) + " Internal Server Error : TeamService/PlayerService updatePlayerTeam(getTeam/getPlayerTeam) SqlConnector Exception !", e);

                final JsonObject errorJsonResponse = new JsonObject();
                errorJsonResponse.put("error", "SqlConnector exception");

                routingContext.response()
                        .setStatusCode(500)
                        .putHeader("content-type", "application/json")
                        .end(Json.encode(errorJsonResponse));
            }

        } catch (DecodeException e) {
            SkyGamesTheApp.LOGGER.error(HttpLogUtils.logHttpRequestRemote(routingContext.request()) + " Internal Server Error : Json Point parse exception !", e);

            final JsonObject errorJsonResponse = new JsonObject();
            errorJsonResponse.put("error", "Json decode exception");
            errorJsonResponse.put("invalid-body-request", body);

            routingContext.response()
                    .setStatusCode(400)
                    .putHeader("content-type", "application/json")
                    .end(Json.encode(errorJsonResponse));
        }
    }

    private boolean checkTeam(String id) throws ClassNotFoundException, SQLException, MissingPropertyException {
        final TeamService service = new TeamService(this.connector);
        return service.get(id) != null;
    }
}