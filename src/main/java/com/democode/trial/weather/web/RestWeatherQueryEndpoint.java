package com.democode.trial.weather.web;

import com.democode.trial.weather.dto.AirportData;
import com.democode.trial.weather.dto.AtmosphericInformation;
import com.democode.trial.weather.service.AWADataStorageService;
import com.democode.trial.weather.service.AWADataStorageServiceImpl;
import com.google.gson.Gson;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.util.*;
import java.util.logging.Logger;

/**
 * The Weather App REST endpoint allows clients to query, update and check health stats. Currently, all data is
 * held in memory. The end point deploys to a single container
 */

//TODO Need to cover all end-points by Integration tests until the next realease


@Path("/query")
public class RestWeatherQueryEndpoint implements WeatherQueryEndpoint {

    private static final String ATMOSPHERIC_INFORMATION_HAS_NOT_FOUND = "AtmosphericInformation has not found for provided iata=";
	private static final String AIRPORT_DATA_HAS_NOT_FOUND = "AirportData has not found for provided iata=";

	public final static Logger LOGGER = Logger.getLogger("WeatherQuery");
	
	/**
	 * Storage service
	 */
	AWADataStorageService awaDataStorageService = new AWADataStorageServiceImpl();

    /** shared gson json to object factory */
    public static final Gson gson = new Gson();

    //TODO Need to understand why that end-point returns String instead of Response (with Status and Entry)

    /**
     * Retrieve service health including total size of valid data points and request frequency information.
     *
     * @return health stats for the service as a string
     */
    @GET
    @Path("/ping")
    public String ping() {
        Map<String, Object> retval = new HashMap<>();

        int datasize = calculateDataSize();
        
        retval.put("datasize", datasize);

        Map<String, Double> freq = calculateIataFreq();
        retval.put("iata_freq", freq);

        int[] hist = calculateRadiusFreq();
        retval.put("radius_freq", hist);

        return gson.toJson(retval);
    }

    /**
     * Given a query in json format {'iata': CODE, 'radius': km} extracts the requested airport information and
     * return a list of matching atmosphere information.
     *
     * @param iata the iataCode
     * @param radiusString the radius in km
     *
     * @return a list of atmospheric information
     */
    @GET
    @Path("/weather/{iata}/{radius}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response get(@PathParam("iata") String iata, @PathParam("radius") String radiusString) {
        double radius = radiusString == null || radiusString.trim().isEmpty() ? 0 : Double.valueOf(radiusString);
        awaDataStorageService.updateRequestFrequency(iata, radius);

        List<AtmosphericInformation> retval = new ArrayList<>();
        if (radius == 0) {
        	AtmosphericInformation ai = awaDataStorageService.getAtmosphericInformation(iata);
            if(ai == null) {
            	return Response.status(Response.Status.NOT_FOUND).entity(AIRPORT_DATA_HAS_NOT_FOUND+iata).build();
            }
            retval.add(ai);
        } else {
            AirportData ad = awaDataStorageService.findAirportData(iata);
            if(ad == null) {
            	return Response.status(Response.Status.NOT_FOUND).entity(AIRPORT_DATA_HAS_NOT_FOUND+iata).build();
            }
            for (int i=0;i< awaDataStorageService.getAirportData().size(); i++){
                if (awaDataStorageService.calculateDistance(ad, awaDataStorageService.getAirportData().get(i)) <= radius){
                    AtmosphericInformation ai = awaDataStorageService.getAtmosphericInformation().get(i);
                    if (isAtmosfericInformationNotEmpty(ai)){
                        retval.add(ai);
                    }
                }
            }
        }
        if(retval.isEmpty()) {
        	return Response.status(Response.Status.NOT_FOUND).entity(ATMOSPHERIC_INFORMATION_HAS_NOT_FOUND+iata).build();
        }

        return Response.status(Response.Status.OK).entity(retval).build();
    }

	private boolean isAtmosfericInformationNotEmpty(AtmosphericInformation ai) {
		return ai.getCloudCover() != null || ai.getHumidity() != null || ai.getPrecipitation() != null
		   || ai.getPressure() != null || ai.getTemperature() != null || ai.getWind() != null;
	}

	private int[] calculateRadiusFreq() {
		int m = awaDataStorageService.getRadiusFreq().keySet().stream().max(Double::compare).orElse(1000.0).intValue() + 1;

        int[] hist = new int[m];
        for (Map.Entry<Double, Integer> e : awaDataStorageService.getRadiusFreq().entrySet()) {
            int i = e.getKey().intValue() % 10;
            hist[i] += e.getValue();
        }
		return hist;
	}

	private Map<String, Double> calculateIataFreq() {
		Map<String, Double> freq = new HashMap<>();
        // fraction of queries
        for (AirportData data : awaDataStorageService.getAirportData()) {
        	if(awaDataStorageService.getRequestFrequency().size() != 0) {
        		double frac = (double)awaDataStorageService.getRequestFrequency().getOrDefault(data, 0) / awaDataStorageService.getRequestFrequency().size();
                freq.put(data.getIata(), frac);
        	}
        }
		return freq;
	}

	private int calculateDataSize() {
		int datasize = 0;
        for (AtmosphericInformation ai : awaDataStorageService.getAtmosphericInformation()) {
            // we only count recent readings
            if (isAtmosfericInformationNotEmpty(ai)) {
                // updated in the last day
                if (ai.getLastUpdateTime() > System.currentTimeMillis() - 86400000) {
                    datasize++;
                }
            }
        }
		return datasize;
	}

}
