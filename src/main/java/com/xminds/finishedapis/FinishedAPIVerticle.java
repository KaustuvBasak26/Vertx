package com.xminds.finishedapis;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import com.xminds.finishedapis.resources.ProductResources;

import io.vertx.config.ConfigRetriever;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Cookie;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.CookieHandler;

public class FinishedAPIVerticle extends AbstractVerticle {

	private static final Logger LOGGER = LoggerFactory.getLogger(FinishedAPIVerticle.class);

	public static void main(String[] args) {
		VertxOptions vertxOptions = new VertxOptions();
		vertxOptions.setClustered(true);
		Vertx.clusteredVertx(vertxOptions, results -> {
			if (results.succeeded()) {
				Vertx vertx = results.result();

				ConfigRetriever configRetriever = ConfigRetriever.create(vertx);
				configRetriever.getConfig(config -> {
					if (config.succeeded()) {
						JsonObject configJson = config.result();
						DeploymentOptions options = new DeploymentOptions().setConfig(configJson);
						vertx.deployVerticle(new FinishedAPIVerticle(), options);
					}
				});
			}
		});
	}

	@Override
	public void start() throws Exception {
		LOGGER.info("Verticle FinishedAPIVerticle Started.");

		Router router = Router.router(vertx);

		router.route().handler(CookieHandler.create());

		ProductResources productResources = new ProductResources();

		router.mountSubRouter("/api/", productResources.getAPISubRouter(vertx));

		router.get("/yo.html").handler(routingContext -> {
			Cookie nameCookie = routingContext.getCookie("name");

			ClassLoader classLoader = getClass().getClassLoader();
			File file = new File(classLoader.getResource("webroot/yo.html").getFile());

			String mappedHTML = " ";
			try {
			StringBuilder result = new StringBuilder(" ");
			Scanner scanner = new Scanner(file);
			
			while (scanner.hasNextLine()) {
				result.append(scanner.nextLine()).append("\n");
			}

			scanner.close();
			
			mappedHTML = result.toString();
			} catch (FileNotFoundException e) {
				
				e.printStackTrace();
			}

			String name = "Unknown";
			if (nameCookie != null) {
				name = nameCookie.getValue();
			} else {
				nameCookie = Cookie.cookie("name", "Kaustuv Basak");
				nameCookie.setPath("/");
				nameCookie.setMaxAge(365 * 24 * 60 * 60);
				routingContext.addCookie(nameCookie);
			}
			mappedHTML = replaceAllTokens(mappedHTML, "{name}", name);

			routingContext.response().putHeader(HttpHeaders.CONTENT_TYPE, "text/html").setStatusCode(200)
					.end(mappedHTML);
		});

		vertx.createHttpServer().requestHandler(router::accept).listen(config().getInteger("http.port"),
				asyncResult -> {
					if (asyncResult.succeeded()) {
						LOGGER.info("Http server started on port " + config().getInteger("http.port"));
					} else {
						LOGGER.info("Unable to start Http Server : ", asyncResult.cause());
					}
				});
	}

	private String replaceAllTokens(String input, String token, String newValue) {
		String output = input;
		while (output.indexOf(token) != -1) {
			output.replace(token, newValue);
		}
		return output;
	}

	@Override
	public void stop() throws Exception {
		LOGGER.info("Verticle FinishedAPIVerticle Stopped.");
	}
}
