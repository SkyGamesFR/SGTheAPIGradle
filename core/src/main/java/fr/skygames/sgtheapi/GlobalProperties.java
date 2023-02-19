package fr.skygames.sgtheapi;

import fr.skygames.sgtheapi.utils.DefaultProperties;

public class GlobalProperties extends DefaultProperties {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2466318655747892039L;

	@Override
	public String filename() {
		return "global.properties";
	}

	public boolean isBehindProxy() {
		final String value = "is_behind_proxy";

		if(!this.containsKey(value)) {
			SkyGamesTheApp.LOGGER.warn("Unable to find \"" + value + "\" in " + this.filename());
			return false; //return default
		}

		String property = this.getProperty(value);
		
		if(property.equalsIgnoreCase("true")) {
			return true;
		}else if(property.equalsIgnoreCase("false")) {
			return false;
		}else {
			SkyGamesTheApp.LOGGER.error("\"" + property + "\" value for \"" + value + "\" is invalid ! ( return default false )");
			return false;
		}
		
	}

	public int getPort() {
		final String value = "port";

		if(!this.containsKey(value)) {
			SkyGamesTheApp.LOGGER.warn("Unable to find \"" + value + "\" in " + this.filename());
			return 8686; //return default
		}

		String property = this.getProperty(value);
		try {
			int port = Integer.parseInt(property);
			return port;
		} catch (Exception e) {
			SkyGamesTheApp.LOGGER.error("\"" + value + "\" must be an integer ! ( invalid " + property + " )", e);
			return 8686; //return default
		}
	}

}
