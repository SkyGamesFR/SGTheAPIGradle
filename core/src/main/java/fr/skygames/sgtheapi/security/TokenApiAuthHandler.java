package fr.skygames.sgtheapi.security;

import fr.skygames.sgtheapi.SkyGamesTheApp;
import fr.skygames.sgtheapi.utils.HttpLogUtils;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.RoutingContext;

import java.util.Map;

public class TokenApiAuthHandler implements Handler<RoutingContext>{
	
	private SecureOptions options;
	
	public TokenApiAuthHandler(SecureOptions options) {
		this.options = options;
	}
	
	@Override
	public void handle(RoutingContext ctx) {
		Route route = ctx.currentRoute();
		if(route.getPath().equals(this.options.getPath()) && route.methods().contains(this.options.getMethod())) {
			HttpServerRequest request = ctx.request();
			MultiMap headersMap = request.headers();
			String contentType = headersMap.get("Authorization");

			if(headersMap.contains("Authorization") && TokenManager.get().checkTokenValidity(contentType.substring(7))) {
				
				// ok
				ctx.next();
				
			}else {
				// authentification needed 401
				SkyGamesTheApp.LOGGER.warn(HttpLogUtils.logHttpRequestRemote(request) + " auth error !");
				final JsonObject errorJsonResponse = new JsonObject();
				errorJsonResponse.put("error", "Authentification Error !");

				ctx.response()
				.setStatusCode(401)
				.putHeader("content-type", "application/json")
				.end(Json.encode(errorJsonResponse));
			}			
		}else {
			SkyGamesTheApp.LOGGER.error("Route matching error !");
		}
	}	
	
	public SecureOptions getOptions() {
		return options;
	}
	
}
