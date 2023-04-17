package fr.skygames.sgtheapi;

import fr.skygames.sgtheapi.sql.SqlConnector;
import fr.skygames.sgtheapi.utils.HttpLogUtils;
import io.vertx.core.AbstractVerticle;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;

public class MyApiVerticle extends AbstractVerticle {
	
	private SqlConnector connector;
	private GlobalProperties globalProperties;
	
	public MyApiVerticle(SqlConnector connector) {
		
		this.connector = connector;
		
		this.globalProperties = new GlobalProperties();
		HttpLogUtils.setIsProxied(this.globalProperties.isBehindProxy());
		
	}
	
    @Override
    public void start() throws Exception {
    	
        SkyGamesTheApp.LOGGER.debug("MyApiVerticle start...");
        final Router router = Router.router(vertx);
        
        router.route("/*").handler(BodyHandler.create());
        
        final TeamResource teamResource = new TeamResource(this.connector);
        final Router teamSubRouter = teamResource.getSubRouter(vertx);
        router.mountSubRouter("/api/v1/", teamSubRouter);
        
        final PlayerResource playerResource = new PlayerResource(this.connector);
        final Router playerSubRouter = playerResource.getSubRouter(vertx);
        router.mountSubRouter("/api/v1/", playerSubRouter);

        final DiscordResource discordResource = new DiscordResource(this.connector);
        final Router discordSubRouter = discordResource.getSubRouter(vertx);
        router.mountSubRouter("/api/v1/", discordSubRouter);

        final RankResource rankResource = new RankResource(this.connector);
        final Router rankSubRouter = rankResource.getSubRouter(vertx);
        router.mountSubRouter("/api/v1/", rankSubRouter);
        
        int port = this.globalProperties.getPort();
        vertx.createHttpServer().requestHandler(router).listen(port);
        
    }
    @Override
    public void stop() throws Exception {
    	SkyGamesTheApp.LOGGER.debug("MyApiVerticle stop...");
    }
}