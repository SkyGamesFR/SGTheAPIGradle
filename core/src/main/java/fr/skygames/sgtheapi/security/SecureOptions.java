package fr.skygames.sgtheapi.security;

import io.vertx.core.http.HttpMethod;

public class SecureOptions {

	private HttpMethod method;
	private String path;
	
	public SecureOptions(HttpMethod method, String path) {
		this.method = method;
		this.path = path;
	}

	public HttpMethod getMethod() {
		return method;
	}

	public String getPath() {
		return path;
	}
	
}
