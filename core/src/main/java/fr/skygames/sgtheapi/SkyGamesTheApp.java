package fr.skygames.sgtheapi;

import fr.skygames.sgtheapi.security.TokenManager;
import fr.skygames.sgtheapi.sql.SqlConnector;
import fr.skygames.sgtheapi.utils.MissingPropertyException;
import io.vertx.core.Vertx;
import io.vertx.core.json.DecodeException;

import java.io.IOException;
import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class SkyGamesTheApp {
	
	public static final Logger LOGGER = LogManager.getLogger(SkyGamesTheApp.class);
	
	public static void main(String[] args) {

		Thread currentThread = Thread.currentThread();
		currentThread.setName(SkyGamesTheApp.class.getSimpleName());
		
		LOGGER.info("Starting App...");
		
		LOGGER.debug("SQL init...");
		SqlConnector connector;
		try {
			connector = new SqlConnector();
		} catch (ClassNotFoundException | MissingPropertyException | SQLException | IOException e) {
			LOGGER.error("SqlConnector Exception !", e);
			System.exit(1);
			return;
		}
		
		LOGGER.debug("TokenManager init...");
		try {
			TokenManager.init();
		} catch (DecodeException | IOException e) {
			LOGGER.error("TokenManager Exception !", e);
			System.exit(1);
			return;
		}		
		
		LOGGER.debug("Vertx HTTP handler init...");
		final Vertx vertx = Vertx.vertx();
		vertx.deployVerticle(new MyApiVerticle(connector));

		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			
			@Override
			public void run() {
					LOGGER.info("Shutdown App...");
					vertx.close(); 
					LogManager.shutdown();				
			}
		}, SkyGamesTheApp.class.getSimpleName() + "-shuthook"));
	}
}