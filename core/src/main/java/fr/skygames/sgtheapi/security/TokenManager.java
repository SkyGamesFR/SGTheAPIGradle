package fr.skygames.sgtheapi.security;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import fr.skygames.sgtheapi.SkyGamesTheApp;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class TokenManager {

	private static TokenManager instance;
	
	public static void init() throws DecodeException, IOException{
		
		final String filename = "tokens.json";
		File file = new File(filename);
		
		TokenManager manager = new TokenManager(file);
		
		if(!file.exists()) {
			file.createNewFile();
			String token = manager.createNewToken();
			manager.save();
			SkyGamesTheApp.LOGGER.warn("Tokens store not found !");
			SkyGamesTheApp.LOGGER.warn("First token added : " + token);
		}else {
			manager.load();
		}
		
		TokenManager.instance = manager;
		
	}
	
	public static TokenManager get() {
		return TokenManager.instance;
	}

	private List<String> tokens;
	private File file;
	
	private TokenManager(File file) {
		this.file = file;
		this.tokens = new ArrayList<String>();
	}
	
	private void load() throws IOException, DecodeException {
		String content = new String(Files.readAllBytes(this.file.toPath()));	
		JsonObject jsonObject = new JsonObject(content);
		
		JsonArray array = jsonObject.getJsonArray("tokens");
		
		int count = 0;
		
		for (Object object : array) {
			this.tokens.add((String) object);
			count+=1;
		}
		
		SkyGamesTheApp.LOGGER.debug(count + " token(s) loaded !");
		
	}
	
	private String createNewToken() throws IOException {
		String token = UUID.randomUUID().toString();
		this.tokens.add(token);
		this.save();
		return token;
	}

	public boolean checkTokenValidity(String apiKey) {
		return this.tokens.contains(apiKey);
	}

	public void save() throws IOException {
		FileWriter fileWriter = new FileWriter(this.file);
		
		JsonObject jsonObject = new JsonObject();
		jsonObject.put("tokens", this.tokens);
		
		fileWriter.write(jsonObject.encodePrettily());
		fileWriter.flush();
		fileWriter.close();
	}
	
	
}
