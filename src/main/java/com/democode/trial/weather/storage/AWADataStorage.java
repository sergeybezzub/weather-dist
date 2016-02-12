package com.democode.trial.weather.storage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.democode.trial.weather.dto.AirportData;
import com.democode.trial.weather.dto.AtmosphericInformation;

//TODO This implementation of storage for demonstration purposes only. For next releases could be considered the In-Memory-Data-Grid solution as the airoportData storage
public class AWADataStorage {

	private static final String AIRPORTS_DATA = "airports.dat";

	private static AWADataStorage instance = null;
	
    /** all known airports */
    private List<AirportData> airportData = new ArrayList<>();

    /** atmospheric information for each airport, idx corresponds with airportData */
    private Map<String,AtmosphericInformation> atmosphericInformation = new HashMap<>();
    
    /**
     * Internal performance counter to better understand most requested information, this map can be improved but
     * for now provides the basis for future performance optimizations. Due to the stateless deployment architecture
     * we don't want to write this to disk, but will pull it off using a REST request and aggregate with other
     * performance metrics {@link #ping()}
     */
    private Map<AirportData, Integer> requestFrequency = new HashMap<AirportData, Integer>();
    private Map<Double, Integer> radiusFreq = new HashMap<Double, Integer>();

    public final static Logger LOGGER = Logger.getLogger("AWADataStorage");

    private AWADataStorage() {
    	
    }
    
    public static synchronized AWADataStorage getInstance() {
    	if(instance == null) {
    		instance = new AWADataStorage();   		
    		instance.initData();    		
    	}

    	return instance;
    }
   
    public List<AirportData> getAirportData() {
		return new ArrayList<AirportData>(airportData);
	}


	public List<AtmosphericInformation> getAtmosphericInformation() {
		return new ArrayList<AtmosphericInformation>(atmosphericInformation.values());
	}

	public Map<AirportData, Integer> getRequestFrequency() {
		Map<AirportData, Integer> clone = new HashMap<AirportData, Integer>();
		clone.putAll(requestFrequency);
		return clone;
	}

	public Map<Double, Integer> getRadiusFreq() {
		Map<Double, Integer> clone = new HashMap<Double, Integer>();
		clone.putAll(radiusFreq);
		return clone;
	}

	
	public void addAtmosphericInformation(String key, AtmosphericInformation atmosphericInformation) {
		this.atmosphericInformation.put(key,atmosphericInformation);
	}

	public void addAirportData(AirportData airportData) {
		this.airportData.add(airportData);
	}


	public void putRequestFrequency(AirportData key, Integer value) {
		this.requestFrequency.put(key, value);
	}


	public void putRadiusFreq(Double key, Integer value) {
		this.radiusFreq.put(key, value);
	}
	
	/**
	 * Clear all storage
	 */
	public void clear() {
        airportData.clear(); 
        atmosphericInformation.clear(); 
        requestFrequency.clear();
        instance = null;
	}

	
	/**
	 * Load test data
	 */
	public void initData() {
		InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(AIRPORTS_DATA);
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		String l = null;

		try {
		    while ( (l = br.readLine()) != null) {
		        String[] split = l.split(",");
		        addAirport(split[0], Double.valueOf(split[1]), Double.valueOf(split[2]));
		    }
		} catch (IOException e) {
			LOGGER.severe("AirportData has not initialized! Root case:"+e);
		}
	}
	
	public synchronized void updateAtmosphericInformation(String key, AtmosphericInformation aiNew ) {
		atmosphericInformation.put(key, aiNew);
	}
	
	/**
	 * Create a new Airport in the storage
	 * @param iataCode 
	 * @param latitude
	 * @param longitude
	 * @return new AirportData object
	 */
	public AirportData addAirport(String iataCode, double latitude, double longitude) {
        AirportData ad = new AirportData();
        ad.setIata(iataCode);
        ad.setLatitude(latitude);
        ad.setLongitude(longitude);
        airportData.add(ad);

        AtmosphericInformation ai = new AtmosphericInformation();
        atmosphericInformation.put(ad.getIata(),ai);
        return ad;
    }


	public AirportData addAirport(AirportData ad) {
        AirportData adNew = new AirportData();
        adNew.setIata(ad.getIata());
        adNew.setLatitude(ad.getLatitude());
        adNew.setLongitude(ad.getLongitude());
        adNew.setCity(ad.getCity());
        adNew.setCountry(ad.getCountry());
        adNew.setDst(ad.getDst());
        adNew.setIcao(ad.getIcao());
        adNew.setName(ad.getName());
        adNew.setTimezone(ad.getTimezone());
        
        airportData.add(adNew);

        AtmosphericInformation ai = new AtmosphericInformation();
        atmosphericInformation.put(adNew.getIata(),ai);
        return adNew;
    }

	public AtmosphericInformation getAtmosphericInformation(String iata) {
		return atmosphericInformation.get(iata);
	}
	
    /**
     * Given an iataCode find the airport data
     *
     * @param iataCode as a string
     * @return airport data or null if not found
     */
    public AirportData findAirportData(String iataCode) {
        return airportData.stream().filter(ap -> ap.getIata().equals(iataCode)).findFirst().orElse(null);
    }

	public void removeAirport(String iata) {
		AirportData ad =findAirportData(iata);
		airportData.remove(ad);
		
		atmosphericInformation.remove(iata);
	}
}
