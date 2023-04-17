package fr.skygames.sgtheapi;

import fr.skygames.sgtheapi.api.data.Rank;
import fr.skygames.sgtheapi.data.RankService;
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

import java.sql.SQLException;
import java.util.List;

public class RankResource {

    private SqlConnector connector;
    private final RankService rankService;

    public RankResource(SqlConnector connector) {
        this.rankService = new RankService(connector);
        this.connector = connector;
    }

    public Router getSubRouter(final Vertx vertx) {
        final SecureRouter subRouter = SecureRouter.router(vertx);

        // Routes
        subRouter.get("/ranks").handler(this::getAllRanks); // show all Ranks
        subRouter.securePost("/ranks").handler(this::addRank); // secure, add new Rank

        subRouter.get("/ranks/:rank").handler(this::getRank); // get rank detail
        subRouter.secureDelete("/ranks/:rank").handler(this::deleteRank); // secure, delete Rank
        subRouter.securePatch("/ranks/:rank").handler(this::updateRank); // secure, update Rank

        return subRouter;
    }

    private void getAllRanks(final RoutingContext routingContext) {
        SkyGamesTheApp.LOGGER.info(HttpLogUtils.logHttpRequestRemote(routingContext.request()));

        try {
            List<Rank> ranks = rankService.getAll();

            final JsonObject jsonResponse = new JsonObject();
            jsonResponse.put("ranks", ranks);
            routingContext.response()
                    .setStatusCode(200)
                    .putHeader("content-type", "application/json")
                    .end(Json.encode(jsonResponse));

        } catch (Exception e) {
            SkyGamesTheApp.LOGGER.error(HttpLogUtils.logHttpRequestRemote(routingContext.request()) + " Internal Server Error : RankService getAll SqlConnector Exception !", e);

            final JsonObject errorJsonResponse = new JsonObject();
            errorJsonResponse.put("error", "SqlConnector exception");

            routingContext.response()
                    .setStatusCode(500)
                    .putHeader("content-type", "application/json")
                    .end(Json.encode(errorJsonResponse));

        }
    }

    private void getRank(final RoutingContext routingContext) {
        SkyGamesTheApp.LOGGER.info(HttpLogUtils.logHttpRequestRemote(routingContext.request()) + " pathParam : " + routingContext.pathParams());
        String param = routingContext.pathParam("rank").toLowerCase();

        try {
            Rank rank = rankService.get(param);

            if (rank != null) {
                final JsonObject jsonResponse = new JsonObject();
                jsonResponse.put("rank", JsonObject.mapFrom(rank));
                routingContext.response()
                        .setStatusCode(200)
                        .putHeader("content-type", "application/json")
                        .end(Json.encode(jsonResponse));
            } else {
                SkyGamesTheApp.LOGGER.warn(HttpLogUtils.logHttpRequestRemote(routingContext.request()) + " rank[name=\"" + param + "\"] can't be found !");
                final JsonObject errorJsonResponse = new JsonObject();
                errorJsonResponse.put("error", "No rank can be found for the specified parameter : " + param);
                errorJsonResponse.put("rank", param);
                routingContext.response()
                        .setStatusCode(404)
                        .putHeader("content-type", "application/json")
                        .end(Json.encode(errorJsonResponse));
            }
        } catch (Exception e) {
            SkyGamesTheApp.LOGGER.error(HttpLogUtils.logHttpRequestRemote(routingContext.request()) + " Internal Server Error : RankService getRank SqlConnector Exception !", e);

            final JsonObject errorJsonResponse = new JsonObject();
            errorJsonResponse.put("error", "SqlConnector exception");

            routingContext.response()
                    .setStatusCode(500)
                    .putHeader("content-type", "application/json")
                    .end(Json.encode(errorJsonResponse));
        }
    }

    private void addRank(final RoutingContext routingContext) {
        SkyGamesTheApp.LOGGER.info(HttpLogUtils.logHttpRequestRemote(routingContext.request()));
        final String body = routingContext.getBodyAsString();
        try {
            final Rank decodedRank = Json.decodeValue(body, Rank.class);
            try {
                String name = decodedRank.getName().toLowerCase();
                Rank rank= rankService.get(name);
                if (rank == null) {
                    rank = this.rankService.add(decodedRank.getName(), decodedRank.getPrefix(), decodedRank.getSuffix(), decodedRank.getColor(), decodedRank.getPriority());
                    final JsonObject jsonResponse = new JsonObject();
                    jsonResponse.put("added-rank", JsonObject.mapFrom(rank));
                    routingContext.response()
                            .setStatusCode(201)
                            .putHeader("content-type", "application/json")
                            .end(Json.encode(jsonResponse));
                } else {
                    SkyGamesTheApp.LOGGER.warn(HttpLogUtils.logHttpRequestRemote(routingContext.request()) + " rank[name=\"" + decodedRank.getName() + "\"] already exist !");
                    final JsonObject errorJsonResponse = new JsonObject();
                    errorJsonResponse.put("error", "Rank already exist !");
                    errorJsonResponse.put("founded-rank", JsonObject.mapFrom(rank));

                    routingContext.response()
                            .setStatusCode(500)
                            .putHeader("content-type", "application/json")
                            .end(Json.encode(errorJsonResponse));
                }
            } catch (ClassNotFoundException | SQLException | MissingPropertyException e) {
                SkyGamesTheApp.LOGGER.error(HttpLogUtils.logHttpRequestRemote(routingContext.request()) + " Internal Server Error : RankService addRank(getRank) SqlConnector Exception !", e);

                final JsonObject errorJsonResponse = new JsonObject();
                errorJsonResponse.put("error", "SqlConnector exception");

                routingContext.response()
                        .setStatusCode(500)
                        .putHeader("content-type", "application/json")
                        .end(Json.encode(errorJsonResponse));
            }

        } catch (DecodeException e) {
            SkyGamesTheApp.LOGGER.error(HttpLogUtils.logHttpRequestRemote(routingContext.request()) + " Internal Server Error : RankService addRank Invalide body / Json decode exception !", e);
            final JsonObject errorJsonResponse = new JsonObject();
            errorJsonResponse.put("error", "Json decode exception");
            errorJsonResponse.put("invalid-body-request", body);

            routingContext.response()
                    .setStatusCode(400)
                    .putHeader("content-type", "application/json")
                    .end(Json.encode(errorJsonResponse));
        }
    }

    private void deleteRank(final RoutingContext routingContext) {
        SkyGamesTheApp.LOGGER.info(HttpLogUtils.logHttpRequestRemote(routingContext.request()) + " pathParam : " + routingContext.pathParams());
        String param = routingContext.pathParam("rank").toLowerCase();

        try {
            Rank rank = rankService.get(param);
            if (rank != null) {
                int result = this.rankService.delete(param);
                if (result == 1) {
                    final JsonObject jsonResponse = new JsonObject();
                    jsonResponse.put("result", "OK");
                    routingContext.response()
                            .setStatusCode(200)
                            .putHeader("content-type", "application/json")
                            .end(Json.encode(jsonResponse));
                } else {
                    // message bizarre
                    SkyGamesTheApp.LOGGER.warn(HttpLogUtils.logHttpRequestRemote(routingContext.request()) + " rank[name=\"" + param + "\"] sql return nothing.");
                }
            } else {
                SkyGamesTheApp.LOGGER.warn(HttpLogUtils.logHttpRequestRemote(routingContext.request()) + " rank[name=\"" + param + "\"] can't be found !");
                final JsonObject errorJsonResponse = new JsonObject();
                errorJsonResponse.put("error", "No Rank can be found for the specified parameter : " + param);
                errorJsonResponse.put("rank", param);
                routingContext.response()
                        .setStatusCode(404)
                        .putHeader("content-type", "application/json")
                        .end(Json.encode(errorJsonResponse));
            }
        } catch (ClassNotFoundException | SQLException | MissingPropertyException e) {
            SkyGamesTheApp.LOGGER.error(HttpLogUtils.logHttpRequestRemote(routingContext.request()) + "Internal Server Error : RankService deleteRank(getRank) SqlConnector Exception !", e);

            final JsonObject errorJsonResponse = new JsonObject();
            errorJsonResponse.put("error", "SqlConnector exception");

            routingContext.response()
                    .setStatusCode(500)
                    .putHeader("content-type", "application/json")
                    .end(Json.encode(errorJsonResponse));
        }
    }

    private void updateRank(final RoutingContext routingContext) {
        SkyGamesTheApp.LOGGER.info(HttpLogUtils.logHttpRequestRemote(routingContext.request()) + " pathParam : " + routingContext.pathParams());

        final String body = routingContext.getBodyAsString();
        final String param = routingContext.pathParam("rank").toLowerCase();

        try {
            final Rank decodedRank = Json.decodeValue(body, Rank.class);
            try {
                Rank rank = rankService.get(param);
                if (rank != null) {

                    rank = this.rankService.update(param, decodedRank.getPrefix(), decodedRank.getSuffix(), decodedRank.getColor(), decodedRank.getPriority());
                    final JsonObject jsonResponse = new JsonObject();
                    jsonResponse.put("updated-rank", JsonObject.mapFrom(rank));
                    routingContext.response()
                            .setStatusCode(200)
                            .putHeader("content-type", "application/json")
                            .end(Json.encode(jsonResponse));

                } else {
                    SkyGamesTheApp.LOGGER.warn(HttpLogUtils.logHttpRequestRemote(routingContext.request()) + " rank[name=\"" + param + "\"] can't be found !");
                    final JsonObject errorJsonResponse = new JsonObject();
                    errorJsonResponse.put("error", "No rank can be found for the specified parameter : " + param);
                    errorJsonResponse.put("rank", param);
                    routingContext.response()
                            .setStatusCode(404)
                            .putHeader("content-type", "application/json")
                            .end(Json.encode(errorJsonResponse));
                }
            } catch (ClassNotFoundException | SQLException | MissingPropertyException e) {
                SkyGamesTheApp.LOGGER.error(HttpLogUtils.logHttpRequestRemote(routingContext.request()) + " Internal Server Error : RankService updateRank(getRank) SqlConnector Exception !", e);

                final JsonObject errorJsonResponse = new JsonObject();
                errorJsonResponse.put("error", "SqlConnector exception");

                routingContext.response()
                        .setStatusCode(500)
                        .putHeader("content-type", "application/json")
                        .end(Json.encode(errorJsonResponse));
            }

        } catch (DecodeException e) {
            SkyGamesTheApp.LOGGER.error(HttpLogUtils.logHttpRequestRemote(routingContext.request()) + " Internal Server Error : RankService updateRank  Json decode exception !", e);

            final JsonObject errorJsonResponse = new JsonObject();
            errorJsonResponse.put("error", "Json decode exception");
            errorJsonResponse.put("invalid-body-request", body);

            routingContext.response()
                    .setStatusCode(400)
                    .putHeader("content-type", "application/json")
                    .end(Json.encode(errorJsonResponse));
        }
    }
}
