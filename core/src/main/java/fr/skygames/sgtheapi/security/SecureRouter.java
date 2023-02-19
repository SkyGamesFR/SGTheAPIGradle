package fr.skygames.sgtheapi.security;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.impl.RouterImpl;

public class SecureRouter extends RouterImpl{
	
	public SecureRouter(Vertx vertx) {
		super(vertx);
	}
	
	public final static SecureRouter router(Vertx vertx) {
		return new SecureRouter(vertx);
	}
	
	public Route secureRoute(HttpMethod method, String path) {
		// last handler
		return this.route(method, path).handler(new TokenApiAuthHandler(new SecureOptions(method, path)));
	}
	
	public Route secureGet(String path) {
		return this.secureRoute(HttpMethod.GET, path);
	}
	
	public Route securePatch(String path) {
		return this.secureRoute(HttpMethod.PATCH, path);
	}
	
	public Route secureDelete(String path) {
		return this.secureRoute(HttpMethod.DELETE, path);
	}
	
	public Route securePost(String path) {
		return this.secureRoute(HttpMethod.POST, path);
	}
	
}
