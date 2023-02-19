package fr.skygames.sgtheapi;

import java.sql.SQLException;
import java.util.List;

import fr.skygames.sgtheapi.api.data.Player;
import fr.skygames.sgtheapi.api.data.PointHistory;
import fr.skygames.sgtheapi.api.data.Team;
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

public class TeamResource {

	private TeamService teamService;

	public TeamResource(SqlConnector connector) {
		this.teamService = new TeamService(connector);
	}

	public Router getSubRouter(final Vertx vertx) {
		final SecureRouter subRouter = SecureRouter.router(vertx);
		
		// Routes
		subRouter.get("/teams").handler(this::getAllTeams); // list all team (team id,name,color)
		subRouter.securePost("/teams").handler(this::addTeam); // secure, add new team

		subRouter.get("/teams/:team").handler(this::getTeam); // team detail (id,name,color,point,players)
		subRouter.secureDelete("/teams/:team").handler(this::deleteTeam); // secure, delete team
		subRouter.securePatch("/teams/:team").handler(this::updateTeam); // secure, update team

		subRouter.get("/teams/:team/owner").handler(this::getTeamOwner); // list owner of team

		subRouter.get("/teams/:team/players").handler(this::getTeamPlayers); // team players list

		subRouter.get("/teams/:team/point").handler(this::getTeamPoint); // get team point and modification history (secure) 
		subRouter.securePatch("/teams/:team/point").handler(this::updateTeamPoint); // update team point
		subRouter.secureGet("/teams/:team/point/history").handler(this::getTeamPointHistory); // get team point modification history (secure)

		return subRouter;
	}

	private void getAllTeams(final RoutingContext routingContext) {

		SkyGamesTheApp.LOGGER.info(HttpLogUtils.logHttpRequestRemote(routingContext.request()));

		try {
			List<Team> teams = teamService.getAll();

			final JsonObject jsonResponse = new JsonObject();
			jsonResponse.put("teams", teams);
			routingContext.response()
			.setStatusCode(200)
			.putHeader("content-type", "application/json")
			.end(Json.encode(jsonResponse));

		} catch (Exception e) {
			SkyGamesTheApp.LOGGER.error(HttpLogUtils.logHttpRequestRemote(routingContext.request()) + " Internal Server Error : TeamService getAll SqlConnector Exception !", e);

			final JsonObject errorJsonResponse = new JsonObject();
			errorJsonResponse.put("error", "SqlConnector exception");

			routingContext.response()
			.setStatusCode(500)
			.putHeader("content-type", "application/json")
			.end(Json.encode(errorJsonResponse));

		}
	}

	private void getTeam(final RoutingContext routingContext) {
		SkyGamesTheApp.LOGGER.info(HttpLogUtils.logHttpRequestRemote(routingContext.request()) + " pathParam : " + routingContext.pathParams());
		String param = routingContext.pathParam("team").toLowerCase();

		try {
			Team team = teamService.get(param);

			if(team != null) {
				final JsonObject jsonResponse = new JsonObject();
				jsonResponse.put("team", JsonObject.mapFrom(team));
				routingContext.response()
				.setStatusCode(200)
				.putHeader("content-type", "application/json")
				.end(Json.encode(jsonResponse));
			}else {
				SkyGamesTheApp.LOGGER.warn(HttpLogUtils.logHttpRequestRemote(routingContext.request()) + " team[id=\"" + param + "\"] can't be found !");
				final JsonObject errorJsonResponse = new JsonObject();
				errorJsonResponse.put("error", "No team can be found for the specified parameter : " + param);
				errorJsonResponse.put("team", param);
				routingContext.response()
				.setStatusCode(404)
				.putHeader("content-type", "application/json")
				.end(Json.encode(errorJsonResponse));
			}
		} catch (Exception e) {
			SkyGamesTheApp.LOGGER.error(HttpLogUtils.logHttpRequestRemote(routingContext.request()) + " Internal Server Error : TeamService getTeam SqlConnector Exception !", e);

			final JsonObject errorJsonResponse = new JsonObject();
			errorJsonResponse.put("error", "SqlConnector exception");

			routingContext.response()
			.setStatusCode(500)
			.putHeader("content-type", "application/json")
			.end(Json.encode(errorJsonResponse));
		}
	}

	private void addTeam(final RoutingContext routingContext) {
		SkyGamesTheApp.LOGGER.info(HttpLogUtils.logHttpRequestRemote(routingContext.request()));
		final String body = routingContext.getBodyAsString();
		
		try {
			final Team decodedTeam = Json.decodeValue(body,Team.class);
			
			try {
				String id = decodedTeam.getId().toLowerCase();
				Team team = teamService.get(id);
				if(!id.equals("none") && team == null) {
					team = this.teamService.add(id, decodedTeam.getDisplay_name(), decodedTeam.getColor_code());
					final JsonObject jsonResponse = new JsonObject();
					jsonResponse.put("added-team", JsonObject.mapFrom(team));
					routingContext.response()
					.setStatusCode(201)
					.putHeader("content-type", "application/json")
					.end(Json.encode(jsonResponse));
				}else {
					SkyGamesTheApp.LOGGER.warn(HttpLogUtils.logHttpRequestRemote(routingContext.request()) + " team[id=\"" + id + "\"] already exist !");
					final JsonObject errorJsonResponse = new JsonObject();
					errorJsonResponse.put("error", "Team already exist !");
					errorJsonResponse.put("founded-team", JsonObject.mapFrom(team));

					routingContext.response()
					.setStatusCode(500)
					.putHeader("content-type", "application/json")
					.end(Json.encode(errorJsonResponse));
				}
			} catch (ClassNotFoundException | SQLException | MissingPropertyException e) {
				SkyGamesTheApp.LOGGER.error(HttpLogUtils.logHttpRequestRemote(routingContext.request()) + " Internal Server Error : TeamService addTeam(getTeam) SqlConnector Exception !", e);

				final JsonObject errorJsonResponse = new JsonObject();
				errorJsonResponse.put("error", "SqlConnector exception");

				routingContext.response()
				.setStatusCode(500)
				.putHeader("content-type", "application/json")
				.end(Json.encode(errorJsonResponse));
			}		
			
		} catch (DecodeException e) {
			SkyGamesTheApp.LOGGER.error(HttpLogUtils.logHttpRequestRemote(routingContext.request()) + " Internal Server Error : TeamService addTeam Team Json decode exception !", e);

			final JsonObject errorJsonResponse = new JsonObject();
			errorJsonResponse.put("error", "Json decode exception");
			errorJsonResponse.put("invalid-body-request", body);

			routingContext.response()
			.setStatusCode(400)
			.putHeader("content-type", "application/json")
			.end(Json.encode(errorJsonResponse));			
		}
	}

	private void deleteTeam(final RoutingContext routingContext) {
		SkyGamesTheApp.LOGGER.info(HttpLogUtils.logHttpRequestRemote(routingContext.request()) + " pathParam : " + routingContext.pathParams());
		String param = routingContext.pathParam("team").toLowerCase();
		
		try {
			Team team = teamService.get(param);
			if(!param.equals("none") && team != null) {
				
				if(this.teamService.getPlayers(param).isEmpty()) {
					int result = this.teamService.delete(param);
					if(result == 1) {
						final JsonObject jsonResponse = new JsonObject();
						jsonResponse.put("result", "OK");
						routingContext.response()
						.setStatusCode(200)
						.putHeader("content-type", "application/json")
						.end(Json.encode(jsonResponse));
					}else {
						// message bizarre
						SkyGamesTheApp.LOGGER.warn(HttpLogUtils.logHttpRequestRemote(routingContext.request()) + " team[id=\"" + param + "\"] sql return nothing.");
					}	
				}else {
					SkyGamesTheApp.LOGGER.warn(HttpLogUtils.logHttpRequestRemote(routingContext.request()) + " team[id=\"" + param + "\"] can't be delete cause not empty !");
					final JsonObject errorJsonResponse = new JsonObject();
					errorJsonResponse.put("error", "Can't be delete, cause not empty : " + param);
					errorJsonResponse.put("team", param);
					routingContext.response()
					.setStatusCode(404)
					.putHeader("content-type", "application/json")
					.end(Json.encode(errorJsonResponse));
				}
			}else {
				SkyGamesTheApp.LOGGER.warn(HttpLogUtils.logHttpRequestRemote(routingContext.request()) + " team[id=\"" + param + "\"] can't be found !");
				final JsonObject errorJsonResponse = new JsonObject();
				errorJsonResponse.put("error", "No team can be found for the specified parameter : " + param);
				errorJsonResponse.put("team", param);
				routingContext.response()
				.setStatusCode(404)
				.putHeader("content-type", "application/json")
				.end(Json.encode(errorJsonResponse));
			}
		} catch (ClassNotFoundException | SQLException | MissingPropertyException e) {
			SkyGamesTheApp.LOGGER.error(HttpLogUtils.logHttpRequestRemote(routingContext.request()) + "Internal Server Error : TeamService deleteTeam(getTeam) SqlConnector Exception !", e);

			final JsonObject errorJsonResponse = new JsonObject();
			errorJsonResponse.put("error", "SqlConnector exception");

			routingContext.response()
			.setStatusCode(500)
			.putHeader("content-type", "application/json")
			.end(Json.encode(errorJsonResponse));
		}
	}

	private void updateTeam(final RoutingContext routingContext) {
		SkyGamesTheApp.LOGGER.info(HttpLogUtils.logHttpRequestRemote(routingContext.request()) + " pathParam : " + routingContext.pathParams());
		
		final String body = routingContext.getBodyAsString();
		final String param = routingContext.pathParam("team").toLowerCase();
		
		try {
			final Team decodedTeam = Json.decodeValue(body,Team.class);
			
			try {
				Team team = teamService.get(param);
				if(!param.equals("none") && team != null) {
					
					team = this.teamService.update(param, decodedTeam.getDisplay_name(), decodedTeam.getColor_code());
					final JsonObject jsonResponse = new JsonObject();
					jsonResponse.put("updated-team", JsonObject.mapFrom(team));
					routingContext.response()
					.setStatusCode(200)
					.putHeader("content-type", "application/json")
					.end(Json.encode(jsonResponse));
					
				}else {
					SkyGamesTheApp.LOGGER.warn(HttpLogUtils.logHttpRequestRemote(routingContext.request()) + " team[id=\"" + param + "\"] can't be found !");
					final JsonObject errorJsonResponse = new JsonObject();
					errorJsonResponse.put("error", "No team can be found for the specified parameter : " + param);
					errorJsonResponse.put("team", JsonObject.mapFrom(new Team(param, decodedTeam.getDisplay_name(),decodedTeam.getColor_code())));
					routingContext.response()
					.setStatusCode(404)
					.putHeader("content-type", "application/json")
					.end(Json.encode(errorJsonResponse));
				}
			} catch (ClassNotFoundException | SQLException | MissingPropertyException e) {
				SkyGamesTheApp.LOGGER.error(HttpLogUtils.logHttpRequestRemote(routingContext.request()) + " Internal Server Error : TeamService updateTeam(getTeam) SqlConnector Exception !", e);

				final JsonObject errorJsonResponse = new JsonObject();
				errorJsonResponse.put("error", "SqlConnector exception");

				routingContext.response()
				.setStatusCode(500)
				.putHeader("content-type", "application/json")
				.end(Json.encode(errorJsonResponse));
			}		
			
		} catch (DecodeException e) {
			SkyGamesTheApp.LOGGER.error(HttpLogUtils.logHttpRequestRemote(routingContext.request()) + " Internal Server Error : Team Json decode exception !", e);

			final JsonObject errorJsonResponse = new JsonObject();
			errorJsonResponse.put("error", "Json decode exception");
			errorJsonResponse.put("invalid-body-request", body);

			routingContext.response()
			.setStatusCode(400)
			.putHeader("content-type", "application/json")
			.end(Json.encode(errorJsonResponse));			
		}
	}

	private void getTeamPlayers(final RoutingContext routingContext) {
		SkyGamesTheApp.LOGGER.info(HttpLogUtils.logHttpRequestRemote(routingContext.request()) + " pathParam : " + routingContext.pathParams());
		String param = routingContext.pathParam("team").toLowerCase();

		try {
			Team team = teamService.get(param);

			if(!param.equals("none") && team != null) {
				
				List<Player> players = this.teamService.getPlayers(param);
				
				final JsonObject jsonResponse = new JsonObject();
				jsonResponse.put("players", players);
				routingContext.response()
				.setStatusCode(200)
				.putHeader("content-type", "application/json")
				.end(Json.encode(jsonResponse));
				
				
			}else {
				SkyGamesTheApp.LOGGER.warn(HttpLogUtils.logHttpRequestRemote(routingContext.request()) + " team[id=\"" + param + "\"] can't be found !");
				final JsonObject errorJsonResponse = new JsonObject();
				errorJsonResponse.put("error", "No team can be found for the specified parameter : " + param);
				errorJsonResponse.put("team", param);
				routingContext.response()
				.setStatusCode(404)
				.putHeader("content-type", "application/json")
				.end(Json.encode(errorJsonResponse));
			}
		} catch (Exception e) {
			SkyGamesTheApp.LOGGER.error(HttpLogUtils.logHttpRequestRemote(routingContext.request()) + " Internal Server Error : TeamService getTeamPlayers(getTeam) SqlConnector Exception !", e);

			final JsonObject errorJsonResponse = new JsonObject();
			errorJsonResponse.put("error", "SqlConnector exception");

			routingContext.response()
			.setStatusCode(500)
			.putHeader("content-type", "application/json")
			.end(Json.encode(errorJsonResponse));
		}
	}

	private void getTeamOwner(final RoutingContext routingContext) {

	}

	private void getTeamPoint(final RoutingContext routingContext) {
		SkyGamesTheApp.LOGGER.info(HttpLogUtils.logHttpRequestRemote(routingContext.request()) + " pathParam : " + routingContext.pathParams());
		String param = routingContext.pathParam("team").toLowerCase();

		try {
			Integer point = teamService.getPoint(param);

			if(!param.equals("none") && point != null) {
				
				final JsonObject jsonResponse = new JsonObject();
				jsonResponse.put("point", point);
				routingContext.response()
				.setStatusCode(200)
				.putHeader("content-type", "application/json")
				.end(Json.encode(jsonResponse));
				
			}else {
				SkyGamesTheApp.LOGGER.warn(HttpLogUtils.logHttpRequestRemote(routingContext.request()) + " team[id=\"" + param + "\"] can't be found !");
				final JsonObject errorJsonResponse = new JsonObject();
				errorJsonResponse.put("error", "No team can be found for the specified parameter : " + param);
				errorJsonResponse.put("team", param);
				routingContext.response()
				.setStatusCode(404)
				.putHeader("content-type", "application/json")
				.end(Json.encode(errorJsonResponse));
			}
		} catch (Exception e) {
			SkyGamesTheApp.LOGGER.error(HttpLogUtils.logHttpRequestRemote(routingContext.request()) + " Internal Server Error : TeamService getTeamPoint SqlConnector Exception !", e);

			final JsonObject errorJsonResponse = new JsonObject();
			errorJsonResponse.put("error", "SqlConnector exception");

			routingContext.response()
			.setStatusCode(500)
			.putHeader("content-type", "application/json")
			.end(Json.encode(errorJsonResponse));
		}
	}

	private void getTeamPointHistory(final RoutingContext routingContext) {
		SkyGamesTheApp.LOGGER.info(HttpLogUtils.logHttpRequestRemote(routingContext.request()) + " pathParam : " + routingContext.pathParams());
		String param = routingContext.pathParam("team").toLowerCase();

		try {
			Integer point = teamService.getPoint(param);

			if(!param.equals("none") && point != null) {
				
				List<PointHistory> players = this.teamService.getPointHistory(param);
				
				final JsonObject jsonResponse = new JsonObject();
				jsonResponse.put("history", players);
				routingContext.response()
				.setStatusCode(200)
				.putHeader("content-type", "application/json")
				.end(Json.encode(jsonResponse));
				
				
			}else {
				SkyGamesTheApp.LOGGER.warn(HttpLogUtils.logHttpRequestRemote(routingContext.request()) + " team[id=\"" + param + "\"] can't be found !");
				final JsonObject errorJsonResponse = new JsonObject();
				errorJsonResponse.put("error", "No team can be found for the specified parameter : " + param);
				errorJsonResponse.put("team", param);
				routingContext.response()
				.setStatusCode(404)
				.putHeader("content-type", "application/json")
				.end(Json.encode(errorJsonResponse));
			}
		} catch (Exception e) {
			SkyGamesTheApp.LOGGER.error(HttpLogUtils.logHttpRequestRemote(routingContext.request()) + " Internal Server Error : TeamService getTeamPointHistory(getTeam) SqlConnector Exception !", e);

			final JsonObject errorJsonResponse = new JsonObject();
			errorJsonResponse.put("error", "SqlConnector exception");

			routingContext.response()
			.setStatusCode(500)
			.putHeader("content-type", "application/json")
			.end(Json.encode(errorJsonResponse));
		}
	}

	private void updateTeamPoint(final RoutingContext routingContext) {
		SkyGamesTheApp.LOGGER.info(HttpLogUtils.logHttpRequestRemote(routingContext.request()) + " pathParam : " + routingContext.pathParams());
		
		final String body = routingContext.getBodyAsString();
		final String param = routingContext.pathParam("team").toLowerCase();
		
		try {
			JsonObject jsonObject = new JsonObject(body);
			if(!jsonObject.containsKey("point")) {
				throw new DecodeException("Missing point field !");
			}
			final Integer decodedPoint = jsonObject.getInteger("point"); 
			
			try {
				Integer point = teamService.getPoint(param);
				
				if(!param.equals("none") && point != null) {
					
					point = this.teamService.updatePoint(param, decodedPoint);
					final JsonObject jsonResponse = new JsonObject();
					jsonResponse.put("updated-point", point);
					routingContext.response()
					.setStatusCode(200)
					.putHeader("content-type", "application/json")
					.end(Json.encode(jsonResponse));
					
				}else {
					SkyGamesTheApp.LOGGER.warn(HttpLogUtils.logHttpRequestRemote(routingContext.request()) + " team[id=\"" + param + "\"] can't be found !");
					final JsonObject errorJsonResponse = new JsonObject();
					errorJsonResponse.put("error", "No team can be found for the specified parameter : " + param);
					routingContext.response()
					.setStatusCode(404)
					.putHeader("content-type", "application/json")
					.end(Json.encode(errorJsonResponse));
				}
			} catch (ClassNotFoundException | SQLException | MissingPropertyException e) {
				SkyGamesTheApp.LOGGER.error(HttpLogUtils.logHttpRequestRemote(routingContext.request()) + " Internal Server Error : TeamService updateTeamPoint(getTeamPoint) SqlConnector Exception !", e);

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

}