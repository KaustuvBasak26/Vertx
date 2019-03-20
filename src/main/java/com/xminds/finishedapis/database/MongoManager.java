package com.xminds.finishedapis.database;

import java.util.List;

import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.mongo.MongoClient;

public class MongoManager {
	private static final Logger LOGGER = LoggerFactory.getLogger(MongoManager.class);
	
	private MongoClient mongoClient = null;

	public MongoManager(MongoClient mongoClient) {
		this.mongoClient = mongoClient;
	}
	
	public void registerConsumer(Vertx vertx) {
		vertx.eventBus().consumer("com.xminds.mongoservice", message -> {
			JsonObject messageBody = (JsonObject)message.body();
			System.out.println("Received request : "+messageBody.toString());
			System.out.println(messageBody.getString("cmd"));
			
			if(messageBody.getString("cmd").equals("findAll")){
				getAllProducts(message);
			}
			if(messageBody.getString("cmd").equals("findById")){
				String id = messageBody.getString("id");
				getProductById(message, id);
			}
			if(messageBody.getString("cmd").equals("add")){
				addProduct(message);
			}
			if(messageBody.getString("cmd").equals("updateById")){
				String id = messageBody.getString("id");
				updateProductById(message, id);
			}
			if(messageBody.getString("cmd").equals("deleteById")){
				String id = messageBody.getString("id");
				deleteProductById(message, id);
			}
		});
	}

	private void deleteProductById(Message<Object> message, String id) {
		JsonObject query = new JsonObject().put("_id", id);
		mongoClient.removeDocument("products", query, result -> {
			if(result.succeeded()){
				LOGGER.info("Successfully deleted product with id : " + id);
				JsonObject responseJson = new JsonObject();
				responseJson.put("Success", "Deleted "+id);
				
				message.reply(responseJson.toString());
			}else{
				JsonObject responseJson = new JsonObject();
				responseJson.put("error", "product not deleted");
				message.reply(responseJson.toString());
			}
		});
		
	}

	private void updateProductById(Message<Object> message, String id) {
		JsonObject messageBody = (JsonObject)message.body();
		JsonObject query = new JsonObject().put("_id", id);
		String name = messageBody.getString("name");
		String description = messageBody.getString("description");
		JsonObject update = new JsonObject();
		if(name!=null && description==null){
			update.put("name", name);
		}else if(description!=null && name==null){
			update.put("description", description);
		}else if(name!=null && description!=null){
			update.put("name", name).put("description", description);
		}
		JsonObject updateQuery = new JsonObject().put("$set", update);
		
		mongoClient.updateCollection("products", query, updateQuery, result -> {
			if(result.succeeded()){
				LOGGER.info("Updated product "+id);
				message.reply(update.put("id", id).toString());
			}else{
				JsonObject responseJson = new JsonObject();
				responseJson.put("error", "product not updated");
				message.reply(responseJson.toString());
			}
		});
		
	}

	private void addProduct(Message<Object> message) {
		//System.out.println("Inside ADD!!");
		JsonObject messageBody = (JsonObject) message.body();
		System.out.println(messageBody);
		String name = messageBody.getString("name");
		String description = messageBody.getString("description");
		JsonObject newProduct = new JsonObject().put("name", name).put("description", description);
		mongoClient.insert("products", newProduct , result -> {
			if(result.succeeded()){
				String id = result.result();
				LOGGER.info("Added new product with id : " + id);
				message.reply(newProduct.toString());
			}else{
				LOGGER.info("Unable to add new product ", result.cause());
				JsonObject responseJson = new JsonObject();
				responseJson.put("error", "product not added");
				message.reply(responseJson.toString());
			}
		});
	}

	public void getProductById(Message<Object> message, String id) {
		mongoClient.find("products", new JsonObject().put("_id", id), results -> {
			List<JsonObject> objects = results.result();
			try{
				if(objects!=null && objects.size()!=0){
					JsonObject jsonResponse = new JsonObject();
					jsonResponse.put("products", objects);
					
					message.reply(jsonResponse.toString());
				}else{
					JsonObject jsonResponse = new JsonObject();
					jsonResponse.put("error", "No items found");
					
					message.reply(jsonResponse.toString());
				}
			}catch (Exception e) {
				JsonObject jsonResponse = new JsonObject();
				jsonResponse.put("error", "Exception");
				
				message.reply(jsonResponse.toString());
			}
		});
		
	}

	public void getAllProducts(Message<Object> message) {
		//System.out.println("GET ALL PRODUCTS!!!");
		mongoClient.find("products", new JsonObject(), results -> {
			List<JsonObject> objects = results.result();
			try {
				
				if (objects != null && objects.size() != 0) {
					//System.out.println("SENDING REQUEST TO MONGO V!!!");
					System.out.println("Got some items of len = " + objects.size());
					JsonObject jsonResponse = new JsonObject();
					jsonResponse.put("products", objects);
					
					message.reply(jsonResponse.toString());
					
					/*routingContext.response().putHeader(HttpHeaders.CONTENT_TYPE, "application/json; charset=utf-8")
							.setStatusCode(200).end(Json.encodePrettily(jsonResponse));*/
				} else {
					JsonObject jsonResponse = new JsonObject();
					jsonResponse.put("error", "No items found");
					
					message.reply(jsonResponse.toString());
					
					/*routingContext.response()
							.putHeader(HttpHeaders.CONTENT_TYPE, "application/json; charset = utf-8")
							.setStatusCode(400)
							.end(Json.encodePrettily(jsonResponse));*/
				}
			} catch (Exception e) {
				LOGGER.info("getAllProducts failed with exception e = ", e.getLocalizedMessage());
				
				JsonObject jsonResponse = new JsonObject();
				jsonResponse.put("error", "Exception");
				
				message.reply(jsonResponse.toString());
				
				/*routingContext.response()
							  .putHeader(HttpHeaders.CONTENT_TYPE, "application/json; charset = utf-8")
							  .setStatusCode(400)
							  .end(Json.encodePrettily(jsonResponse));*/
			}
		});
	}
}
