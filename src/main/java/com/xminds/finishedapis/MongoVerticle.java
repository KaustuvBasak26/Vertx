package com.xminds.finishedapis;

import com.xminds.finishedapis.database.MongoManager;

import io.vertx.config.ConfigRetriever;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.mongo.MongoClient;

public class MongoVerticle extends AbstractVerticle{
	
		private static final Logger LOGGER = LoggerFactory.getLogger(MongoVerticle.class);
		
		public static MongoClient mongoClient = null;
		
		public static void main(String[] args) {
			VertxOptions vertxOptions = new VertxOptions();
			vertxOptions.setClustered(true);
			Vertx.clusteredVertx(vertxOptions, results -> {
				Vertx vertx = results.result();
				
				ConfigRetriever configRetriever = ConfigRetriever.create(vertx);
				configRetriever.getConfig(config -> {
					if(config.succeeded()){
						JsonObject configJson = config.result();
						DeploymentOptions options = new DeploymentOptions().setConfig(configJson);
						vertx.deployVerticle(new MongoVerticle(), options);
					}
				});
			});
		}
		
		@Override
		public void start() throws Exception {
			LOGGER.info("Verticle MongoVerticle Started.");
			
			JsonObject dbConfig = new JsonObject();
			dbConfig.put("connection_string", "mongodb://" + config().getString("mongo.host") + ":"+ config().getInteger("mongo.port") + "/" + config().getString("mongo.database"));
			dbConfig.put("useObjectId", true);
			
			mongoClient = MongoClient.createShared(vertx, dbConfig);
			
			MongoManager mongoManager = new MongoManager(mongoClient);
			mongoManager.registerConsumer(vertx);
		}
		
		@Override
		public void stop() throws Exception {
			LOGGER.info("Verticle MongoVerticle Stopped.");
		}
}
