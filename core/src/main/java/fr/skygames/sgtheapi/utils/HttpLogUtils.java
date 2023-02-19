package fr.skygames.sgtheapi.utils;

import fr.skygames.sgtheapi.SkyGamesTheApp;
import io.vertx.core.http.HttpConnection;
import io.vertx.core.http.HttpServerRequest;

public class HttpLogUtils {

	private static boolean is_proxied = false;
	
	public static void setIsProxied(boolean is_proxied) {
		SkyGamesTheApp.LOGGER.debug("Property is_proxied set to " + is_proxied);
		HttpLogUtils.is_proxied = is_proxied;
	}
	
	public static String logHttpRequestRemote(HttpServerRequest httpServerRequest) {
		
		String msg = httpServerRequest.method() + " " + httpServerRequest.uri() + " " ;
		
		HttpConnection connection = httpServerRequest.connection();
		if(is_proxied) {
			
			String addr = httpServerRequest.getHeader("X-Real-IP");
			if(addr == null) {
				addr = httpServerRequest.connection().remoteAddress().host();
			}
			String port = ":" + connection.remoteAddress().port();
			
			msg+= addr + port;
			
		}else {
			msg+= connection.remoteAddress().toString();
		}
		
		return msg;
	}
	
}
