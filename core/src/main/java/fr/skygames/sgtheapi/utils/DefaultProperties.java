package fr.skygames.sgtheapi.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Properties;

import fr.skygames.sgtheapi.SkyGamesTheApp;

public abstract class DefaultProperties extends Properties{

	/**
	 * 
	 */
	private static final long serialVersionUID = -7474515633562421485L;

	public DefaultProperties() {
		super();
		try {

			File file = new File(this.filename());
			InputStream input = SkyGamesTheApp.class.getClassLoader().getResourceAsStream(this.filename());
			
			if (input == null) {
				SkyGamesTheApp.LOGGER.error("Sorry, unable to find " + this.filename() + " in JAR Resources");
				return;
			}
			
			if(!file.exists()) {
				// create file
				Files.copy(input, file.toPath());
				SkyGamesTheApp.LOGGER.debug( this.filename() + " successfuly imported from JAR Resources !");
			}

			input = new FileInputStream(file);
			this.load(input);

		} catch (IOException e) {
			SkyGamesTheApp.LOGGER.error("Exception during SqlProperties initialization !", e);
		}
	}
	
	public abstract String filename();
	
}
