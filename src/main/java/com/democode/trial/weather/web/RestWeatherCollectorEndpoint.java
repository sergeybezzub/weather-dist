package com.democode.trial.weather.web;

import com.democode.trial.weather.WeatherException;
import com.democode.trial.weather.dto.AirportData;
import com.democode.trial.weather.dto.DataPoint;
import com.democode.trial.weather.service.AWADataStorageService;
import com.democode.trial.weather.service.AWADataStorageServiceImpl;
import com.google.gson.Gson;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

/**
 * A REST implementation of the WeatherCollector API.
 */

//TODO Need to cover all end-points by Integration tests until the next realease

@Path("/collect")
public class RestWeatherCollectorEndpoint implements WeatherCollector {
    public final static Logger LOGGER = Logger.getLogger("RestWeatherCollectorEndpoint");

    /** shared gson json to object factory */
    public final static Gson gson = new Gson();
    
    /**
     * Storage service
     */
    AWADataStorageService awaDataStorageService = new AWADataStorageServiceImpl();

    @GET
    @Path("/ping")
    @Override
    public Response ping() {
        return Response.status(Response.Status.OK).entity("ready").build();
    }

    @PUT
    @Path("/weather/{iata}/{pointType}")
    @Override
    public Response updateWeather(@PathParam("iata") String iataCode,
                                  @PathParam("pointType") String pointType,
                                  String datapointJson) {
        try {
        	awaDataStorageService.processDataPoint(iataCode, pointType, gson.fromJson(datapointJson, DataPoint.class));
        } catch (WeatherException e) {
           LOGGER.severe("updateWeather end-point has failed! Root case:"+e.toString());
           return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.toString()).build();
        }
        return Response.status(Response.Status.OK).build();
    }

    @GET
    @Path("/airports")
    @Produces(MediaType.APPLICATION_JSON)
    @Override
    public Response getAirports() {
        Set<String> retval = new HashSet<>();
       	for (AirportData ad : awaDataStorageService.getAirportData()) {
       		retval.add(ad.getIata());
       	}

       	if(retval.isEmpty()) {
        	return Response.status(Response.Status.NOT_FOUND).build();
        }

       	return Response.status(Response.Status.OK).entity(retval).build();
    }

    @GET
    @Path("/airport/{iata}")
    @Produces(MediaType.APPLICATION_JSON)
    @Override
    public Response getAirport(@PathParam("iata") String iata) {
        AirportData ad = awaDataStorageService.findAirportData(iata);
        if(ad == null) {
        	return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.status(Response.Status.OK).entity(ad).build();
    }

    @POST
    @Path("/airport")
    @Consumes(MediaType.APPLICATION_JSON)
    @Override
    public Response addAirport(AirportData airport) {

    	try {
    		awaDataStorageService.addAirport(airport);
    	} catch(IllegalArgumentException e) {
    		return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
    	}
    	return Response.status(Response.Status.CREATED).build();
    }

    @POST
    @Path("/airport/{iata}/{lat}/{long}")
    @Override
    public Response addAirport(@PathParam("iata") String iata,
                               @PathParam("lat") String latString,
                               @PathParam("long") String longString) {
    	try {
    		awaDataStorageService.addAirport(iata, Double.valueOf(latString), Double.valueOf(longString));
    	} catch(IllegalArgumentException e) {
    		return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
    	}
    	return Response.status(Response.Status.CREATED).build();
    }
    
    @DELETE
    @Path("/airport/{iata}")
    @Override
    public Response deleteAirport(@PathParam("iata") String iata) {
    	int retcode=0;
    	try {
    		retcode =awaDataStorageService.removeAirport(iata);
    	}  catch(IllegalArgumentException e) {
    		return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
    	} 
    	
    	if( retcode == 0 ) {
    		return Response.status(Response.Status.NOT_FOUND).entity("Airport with iata=["+iata+"] has not found").build();
    	} else {
    		return Response.status(Response.Status.OK).build();    		    		
    	}
    }

}
