package fr.skygames.sgtheapi.api;

import fr.skygames.sgtheapi.api.exceptions.NullApiTokenException;

public final class SGTheAPI {

	public final static SGTheAPI getAPI() {
		return getAPI(null);		
	}
	
	public final static SGTheAPI getAPI(String token) {
		return new SGTheAPI(token);
	}
	
	private String token;
	
	private SGTheAPI(String token) {
		this.token = token;
	}
	
	public String getToken() throws NullApiTokenException {
		if(token == null) {
			throw new NullApiTokenException();
		}
		return token;
	}
	
}
