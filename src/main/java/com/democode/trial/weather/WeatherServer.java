package com.democode.trial.weather;

import org.glassfish.grizzly.Connection;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.server.HttpServerFilter;
import org.glassfish.grizzly.http.server.HttpServerProbe;
import org.glassfish.grizzly.http.server.Request;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import com.democode.trial.weather.web.RestWeatherCollectorEndpoint;
import com.democode.trial.weather.web.RestWeatherQueryEndpoint;

import java.io.IOException;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.lang.String.*;


/**
 * A This class could be used to test the Weather Application locally and quickly.
 */
public class WeatherServer {

	public final static Logger LOGGER = Logger.getLogger("WeatherServer");
	
    private static final String BASE_URL = "http://localhost:8080/";

    public static void main(String[] args) {
        try {
            System.out.println("Starting Weather App local testing server: " + BASE_URL);
            System.out.println("Not for production use");

            final ResourceConfig resourceConfig = new ResourceConfig();
            resourceConfig.register(RestWeatherCollectorEndpoint.class);
            resourceConfig.register(RestWeatherQueryEndpoint.class);
            final HttpServer server = GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URL), resourceConfig, false);

            Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
                @Override
                public void run() {
                    server.shutdownNow();
                }
            }));

            HttpServerProbe probe = new HttpServerProbe.Adapter() {
                public void onRequestReceiveEvent(HttpServerFilter filter, Connection connection, Request request) {
                    System.out.println(request.getRequestURI());
                }
            };

            server.getServerConfiguration().getMonitoringConfig().getWebServerConfig().addProbes(probe);
            LOGGER.info(format("Weather Server started.\n url=%s\n", BASE_URL));
            server.start();

            Thread.currentThread().join();
        } catch (IOException | InterruptedException ex) {
            Logger.getLogger(WeatherServer.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
}
