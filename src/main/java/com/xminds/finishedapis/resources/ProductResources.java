package com.xminds.finishedapis.resources;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

public class ProductResources {

	private Vertx vertx = null;

	public Router getAPISubRouter(Vertx vertx) {
		this.vertx = vertx;

		Router subAPIRouter = Router.router(vertx);

		subAPIRouter.route("/*").handler(BodyHandler.create());
		subAPIRouter.get("/products").handler(this::getAllProducts);
		subAPIRouter.get("/products/:id").handler(this::getProductById);
		subAPIRouter.post("/products").handler(this::addProduct);
		subAPIRouter.put("/products/:id").handler(this::updateProductById);
		subAPIRouter.delete("/products/:id").handler(this::deleteProductById);

		return subAPIRouter;
	}

	private void deleteProductById(RoutingContext routingContext) {
		final String productId = routingContext.request().getParam("id");

		/*
		 * routingContext.response() .putHeader(HttpHeaders.CONTENT_TYPE,
		 * "application/json; charset=utf-8") .setStatusCode(200) .end();
		 */
		JsonObject message = new JsonObject();
		message.put("cmd", "deleteById");
		message.put("id", productId);

		vertx.eventBus().send("com.xminds.mongoservice", message, reply -> {
			if (reply.succeeded()) {
				JsonObject replyBody = new JsonObject(reply.result().body().toString());
				System.out.println("Received response : " + replyBody.toString());
				routingContext.response().putHeader(HttpHeaders.CONTENT_TYPE, "application/json; charset = utf-8")
						.setStatusCode(200).end(Json.encodePrettily(replyBody));
			}
		});
	}

	private void updateProductById(RoutingContext routingContext) {
		final String productId = routingContext.request().getParam("id");

		JsonObject jsonBody = routingContext.getBodyAsJson();

		/*
		 * String name = jsonBody.getString("name"); String description =
		 * jsonBody.getString("description");
		 * 
		 * Product updatedProduct = new Product(productId, name, description);
		 * 
		 * routingContext.response() .putHeader(HttpHeaders.CONTENT_TYPE,
		 * "application/json; charset=utf-8") .setStatusCode(200)
		 * .end(Json.encodePrettily(updatedProduct));
		 */
		jsonBody.put("id", productId);
		jsonBody.put("cmd", "updateById");

		vertx.eventBus().send("com.xminds.mongoservice", jsonBody, reply -> {
			if (reply.succeeded()) {
				JsonObject replyBody = new JsonObject(reply.result().body().toString());
				System.out.println("Received response : " + replyBody.toString());
				routingContext.response().putHeader(HttpHeaders.CONTENT_TYPE, "application/json; charset = utf-8")
						.setStatusCode(200).end(Json.encodePrettily(replyBody));
			}
		});

	}

	private void addProduct(RoutingContext routingContext) {
		JsonObject jsonBody = routingContext.getBodyAsJson();

		/*
		 * String name = jsonBody.getString("name"); String description =
		 * jsonBody.getString("description");
		 * 
		 * Product newItem = new Product("1333333", name, description);
		 * 
		 * routingContext.response().putHeader(HttpHeaders.CONTENT_TYPE,
		 * "application/json; charset=utf-8")
		 * .setStatusCode(201).end(Json.encodePrettily(newItem));
		 */

		
		jsonBody.put("cmd", "add");
		//System.out.println("Going into bus");

		vertx.eventBus().send("com.xminds.mongoservice", jsonBody, reply -> {
			if (reply.succeeded()) {
				JsonObject replyBody = new JsonObject(reply.result().body().toString());
				System.out.println("Received response : " + replyBody.toString());
				routingContext.response().putHeader(HttpHeaders.CONTENT_TYPE, "application/json; charset = utf-8")
						.setStatusCode(201).end(Json.encodePrettily(replyBody));
			}
		});
	}

	private void getProductById(RoutingContext routingContext) {
		final String productId = routingContext.request().getParam("id");

		/*
		 * Product firstItem = new Product(productId, "1222222",
		 * "My item 1222222");
		 * 
		 * routingContext.response().putHeader(HttpHeaders.CONTENT_TYPE,
		 * "application/json; charset=utf-8")
		 * .setStatusCode(200).end(Json.encodePrettily(firstItem))
		 */;
		JsonObject message = new JsonObject();
		message.put("cmd", "findById");
		message.put("id", productId);
		vertx.eventBus().send("com.xminds.mongoservice", message, reply -> {
			if (reply.succeeded()) {
				JsonObject replyBody = new JsonObject(reply.result().body().toString());
				System.out.println("Received Response : " + replyBody.toString());
				routingContext.response().putHeader(HttpHeaders.CONTENT_TYPE, "application/json; charset = utf-8")
						.setStatusCode(200).end(Json.encodePrettily(replyBody));
			}
		});
	}

	private void getAllProducts(RoutingContext routingContext) {
		/*
		 * JsonObject jsonResponse = new JsonObject(); List<Product> products =
		 * new ArrayList<>(); Product firstItem = new Product("121", "1212",
		 * "My Item 1212"); products.add(firstItem);
		 * 
		 * Product secondItem = new Product("212", "2121", "My Item 2121");
		 * products.add(secondItem); jsonResponse.put("products", products);
		 */

		JsonObject message = new JsonObject();
		message.put("cmd", "findAll");
		vertx.eventBus().send("com.xminds.mongoservice", message, reply -> {
			if (reply.succeeded()) {
				JsonObject replyBody = new JsonObject(reply.result().body().toString());
				System.out.println("Received Response : " + replyBody.toString());
				routingContext.response().putHeader(HttpHeaders.CONTENT_TYPE, "application/json; charset=utf-8")
						.setStatusCode(200).end(Json.encodePrettily(replyBody));
			}
		});

	}

}
