package com.democode.trial.weather.web;

import com.democode.trial.weather.dto.AtmosphericInformation;
import com.democode.trial.weather.dto.DataPoint;
import com.democode.trial.weather.storage.AWADataStorage;
import com.democode.trial.weather.web.RestWeatherCollectorEndpoint;
import com.democode.trial.weather.web.RestWeatherQueryEndpoint;
import com.democode.trial.weather.web.WeatherCollector;
import com.democode.trial.weather.web.WeatherQueryEndpoint;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import javax.ws.rs.core.Response;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class WeatherEndpointTest {

    private WeatherQueryEndpoint _query;
    private WeatherCollector _update;

    private Gson _gson = new Gson();

    private DataPoint _dp;
    @Before
    public void setUp() throws Exception {
    	_query = new RestWeatherQueryEndpoint();
    	_update = new RestWeatherCollectorEndpoint();
    	
        _dp = new DataPoint.Builder().withCount(10).withFirst(10).withMedian(20).withLast(30).withMean(22).build();
        _update.updateWeather("BOS", "wind", _gson.toJson(_dp));
        _query.get("BOS", "0").getEntity();
    }

    @After
    public void clear() {
    	AWADataStorage.getInstance().clear();
    }
    
    @Test
    public void testPing() throws Exception {
        String ping = _query.ping();
        JsonElement pingResult = new JsonParser().parse(ping);
        assertEquals(1, pingResult.getAsJsonObject().get("datasize").getAsInt());
        assertEquals(5, pingResult.getAsJsonObject().get("iata_freq").getAsJsonObject().entrySet().size());
    }

    @SuppressWarnings("unchecked")
	@Test
    public void testGet() throws Exception {       
    	Response response = _query.get("BOS", "0");
        List<AtmosphericInformation> ais = (List<AtmosphericInformation>) response.getEntity();
        assertEquals(200, response.getStatus());
        assertNotNull(ais);
        assertNotNull(ais.get(0).getWind());
        assertEquals(ais.get(0).getWind(), _dp);
    }

    @SuppressWarnings("unchecked")
	@Test
    public void testGetNearby() throws Exception {
        // check datasize response
        _update.updateWeather("JFK", "wind", _gson.toJson(_dp));
        _dp.setMean(40);
        _update.updateWeather("EWR", "wind", _gson.toJson(_dp));
        _dp.setMean(30);
        _update.updateWeather("LGA", "wind", _gson.toJson(_dp));

        List<AtmosphericInformation> ais = (List<AtmosphericInformation>) _query.get("JFK", "200").getEntity();
        assertEquals(3, ais.size());
    }

    @SuppressWarnings("unchecked")
	@Test
    public void testUpdate() throws Exception {

        DataPoint windDp = new DataPoint.Builder().withCount(10).withFirst(10).withMedian(20).withLast(30).withMean(22).build();
        _update.updateWeather("BOS", "wind", _gson.toJson(windDp));
        _query.get("BOS", "0").getEntity();

        String ping = _query.ping();
        JsonElement pingResult = new JsonParser().parse(ping);
        assertEquals(1, pingResult.getAsJsonObject().get("datasize").getAsInt());

        DataPoint cloudCoverDp = new DataPoint.Builder().withCount(4).withFirst(10).withMedian(60).withLast(100).withMean(50).build();
        _update.updateWeather("BOS", "cloudcover", _gson.toJson(cloudCoverDp));

        List<AtmosphericInformation> ais = (List<AtmosphericInformation>) _query.get("BOS", "0").getEntity();
        assertEquals(ais.get(0).getWind(), windDp);
        assertEquals(ais.get(0).getCloudCover(), cloudCoverDp);
    }

    @Test
    public void testAddDelete() throws Exception {

    	_update.addAirport("BSV","1.2","2.3");
        String added = _query.ping();
        JsonElement pingResult = new JsonParser().parse(added);
        assertEquals(1, pingResult.getAsJsonObject().get("datasize").getAsInt());
        assertEquals(6, pingResult.getAsJsonObject().get("iata_freq").getAsJsonObject().entrySet().size());

    	_update.deleteAirport("BSV");
        String removed = _query.ping();
        JsonElement pingResult2 = new JsonParser().parse(removed);
        assertEquals(5, pingResult2.getAsJsonObject().get("iata_freq").getAsJsonObject().entrySet().size());
    }
    
    @Test
    public void testDeleteAlterFlow() throws Exception {

    	Response response = _update.deleteAirport("ZZZ");
    	assertEquals(404, response.getStatus()); 
    	assertEquals("Airport with iata=[ZZZ] has not found", response.getEntity().toString());

    	response = _update.deleteAirport("ZZZZ");
    	assertEquals(400, response.getStatus()); 
    	assertEquals("IATA code is incorrect", response.getEntity().toString());    
    }

    @Test
    public void testAddAlterFlow() throws Exception {

    	Response response = _update.addAirport("","1.2","2.3");
    	assertEquals(400, response.getStatus()); 
    	assertEquals("IATA code is incorrect", response.getEntity().toString());    

    	response = _update.addAirport("ZZZZ", "1","2");
    	assertEquals(400, response.getStatus()); 
    	assertEquals("IATA code is incorrect", response.getEntity().toString());    

    	response = _update.addAirport("AAA", "0","2");
    	assertEquals(400, response.getStatus()); 
    	assertEquals("Latitude should be set", response.getEntity().toString());    

    	response = _update.addAirport("AAA", "1","0");
    	assertEquals(400, response.getStatus()); 
    	assertEquals("Longitude should be set", response.getEntity().toString());    

    }


}