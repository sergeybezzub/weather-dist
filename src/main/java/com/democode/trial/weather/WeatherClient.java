package com.democode.trial.weather;

import java.util.logging.Logger;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import com.democode.trial.weather.dto.DataPoint;

/**
 * A reference implementation for the weather client. Consumers of the REST API can look at WeatherClient
 * to understand API semantics. 
 */
public class WeatherClient {

	public final static Logger LOGGER = Logger.getLogger("WeatherClient");
	
    private static final String BASE_URI = "http://localhost:8080";
    /** end point for read queries */
    private WebTarget query;

    /** end point to supply updates */
    private WebTarget collect;

    public WeatherClient() {
        Client client = ClientBuilder.newClient();
        query = client.target(BASE_URI + "/query");
        collect = client.target("http://localhost:8080/collect");
    }

    public void pingCollect() {
        WebTarget path = collect.path("/ping");
        Response response = path.request().get();
        LOGGER.info("collect.ping: " + response.readEntity(String.class) + "\n");
    }

    public void pingQuery() {
        WebTarget path = query.path("/ping");
        Response response = path.request().get();
        LOGGER.info("query.ping: " + response.readEntity(String.class));
    }

    public void populate() {
        WebTarget path = collect.path("/weather/BOS/wind");
        DataPoint dp = new DataPoint.Builder()
                .withFirst(0).withLast(10).withMean(4).withMedian(4).withCount(10)
                .build();
        Response post = path.request().post(Entity.entity(dp, "application/json"));
        LOGGER.info("Populate-Response status="+post.getStatus());
    }

    public void query() {
        WebTarget path = query.path("/weather/BOS/0");
        Response response = path.request().get();
        LOGGER.info("query.get:" + response.readEntity(String.class));
    }

    public static void main(String[] args) {
        WeatherClient wc = new WeatherClient();
        wc.pingCollect();
        wc.populate();
        wc.query();
        wc.pingQuery();
        LOGGER.info("complete");
        System.exit(0);
    }
}
