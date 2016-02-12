package com.democode.trial.weather;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

import com.democode.trial.weather.dto.AirportData;

import java.io.*;
import java.util.logging.Logger;

/**
 * A simple airport loader which reads a file from disk and sends entries to the webservice
 */
public class AirportLoader {

	public final static Logger LOGGER = Logger.getLogger("AirportLoader");
	
    /** end point for read queries */
    private WebTarget query;

    /** end point to supply updates */
    private WebTarget collect;

    public AirportLoader() {
        Client client = ClientBuilder.newClient();
        query = client.target("http://localhost:8080/query");
        collect = client.target("http://localhost:8080/collect");
    }

    public void upload(InputStream airportDataStream) throws IOException{
        BufferedReader reader = new BufferedReader(new InputStreamReader(airportDataStream));
        String l = null;
        WebTarget target = collect.path("/airport");
        while ((l = reader.readLine()) != null) {
        	String[] parts = l.split(",");
        	AirportData ad = new AirportData();
            ad.setIata(parts[4]);
            try {
            	ad.setLatitude(Double.valueOf(parts[7]));
            } catch(Exception e) {
            	LOGGER.severe("Incorect latitude ignored");
            }
            try {
            	ad.setLongitude(Double.valueOf(parts[6]));
            } catch(Exception e) {
            	LOGGER.severe("Incorect longitude ignored");
            }
            ad.setCity(parts[2]);
            ad.setCountry(parts[3]);
            ad.setDst(parts[10]);
            ad.setIcao(parts[5]);
            ad.setName(parts[1]);
            ad.setTimezone(parts[9]);

            try{
            	target.request(MediaType.APPLICATION_JSON).post(Entity.entity(ad,MediaType.APPLICATION_JSON),AirportData.class);
            } catch(Exception ce) {
            	LOGGER.severe("Connection error. Please check if SERVER has been already started.");
            	return;
            }
        }
    }

    public static void main(String args[]) throws IOException{
        File airportDataFile = new File(args[0]);
        if (!airportDataFile.exists() || airportDataFile.length() == 0) {
        	LOGGER.severe(airportDataFile + " is not a valid input");
            System.exit(1);
        }

        AirportLoader al = new AirportLoader();
        al.upload(new FileInputStream(airportDataFile));
        System.exit(0);
    }
}
