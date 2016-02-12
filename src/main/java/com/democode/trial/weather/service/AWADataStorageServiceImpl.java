package com.democode.trial.weather.service;

import java.util.List;
import java.util.Map;

import com.democode.trial.weather.DataPointType;
import com.democode.trial.weather.WeatherException;
import com.democode.trial.weather.dto.AirportData;
import com.democode.trial.weather.dto.AtmosphericInformation;
import com.democode.trial.weather.dto.DataPoint;
import com.democode.trial.weather.storage.AWADataStorage;

public class AWADataStorageServiceImpl implements AWADataStorageService {
	
    /** earth radius in KM */
    public static final double R = 6372.8;

    /**
     * Records information about how often requests are made
     *
     * @param iata an iata code
     * @param radius query radius
     */
    public void updateRequestFrequency(String iata, Double radius) {
        AirportData airportData = findAirportData(iata);
        AWADataStorage.getInstance().putRequestFrequency(airportData, 
        		AWADataStorage.getInstance().getRequestFrequency().getOrDefault(airportData, 0) + 1);
        AWADataStorage.getInstance().putRadiusFreq(radius, 
        		AWADataStorage.getInstance().getRadiusFreq().getOrDefault(radius, 0));
    }

    /**
     * Given an iataCode find the airport data
     *
     * @param iataCode as a string
     * @return airport data or null if not found
     */
    public AirportData findAirportData(String iataCode) {
        return AWADataStorage.getInstance().getAirportData().stream().filter(ap -> ap.getIata().equals(iataCode))
            .findFirst().orElse(null);
    }

    /**
     * Given an iataCode find the airport data
     *
     * @param iataCode as a string
     * @return airport data or null if not found
     */
    public int getAirportDataIdx(String iataCode) {
        AirportData ad = findAirportData(iataCode);
        return AWADataStorage.getInstance().getAirportData().indexOf(ad);
    }

    /**
     * Haversine distance between two airports.
     *
     * @param ad1 airport 1
     * @param ad2 airport 2
     * @return the distance in KM
     */
    public double calculateDistance(AirportData ad1, AirportData ad2) {
        double deltaLat = Math.toRadians(ad2.getLatitude() - ad1.getLatitude());
        double deltaLon = Math.toRadians(ad2.getLongitude() - ad1.getLongitude());
        double a =  Math.pow(Math.sin(deltaLat / 2), 2) + Math.pow(Math.sin(deltaLon / 2), 2)
                * Math.cos(ad1.getLatitude()) * Math.cos(ad2.getLatitude());
        double c = 2 * Math.asin(Math.sqrt(a));
        return R * c;
    }
    
    /**
     * Update the airports weather data with the collected data.
     *
     * @param iataCode the 3 letter IATA code
     * @param pointType the point type {@link DataPointType}
     * @param dp a datapoint object holding pointType data
     *
     * @throws WeatherException if the update can not be completed
     */
    public void processDataPoint(String iataCode, String pointType, DataPoint dp) throws WeatherException {
    	AtmosphericInformation ai = getAtmosphericInformation(iataCode);
        ai =updateAtmosphericInformation(ai, pointType, dp);
        AWADataStorage.getInstance().updateAtmosphericInformation(iataCode, ai);
    }

    
    /**
     * update atmospheric information with the given data point for the given point type
     *
     * @param newAi the atmospheric information object to update
     * @param pointType the data point type as a string
     * @param dp the actual data point
     * @return updated AtmosphericInformation object
     */
    public AtmosphericInformation updateAtmosphericInformation(AtmosphericInformation newAi, String pointType, DataPoint dp) throws WeatherException {
        
        if (pointType.equalsIgnoreCase(DataPointType.WIND.name())) {
            if (dp.getMean() >= 0) {
                newAi.setWind(dp);
                newAi.setLastUpdateTime(System.currentTimeMillis());
                return newAi;
            }
        }

        if (pointType.equalsIgnoreCase(DataPointType.TEMPERATURE.name())) {
            if (dp.getMean() >= -50 && dp.getMean() < 100) {
                newAi.setTemperature(dp);
                newAi.setLastUpdateTime(System.currentTimeMillis());
                return newAi;
            }
        }

        if (pointType.equalsIgnoreCase(DataPointType.HUMIDTY.name())) {
            if (dp.getMean() >= 0 && dp.getMean() < 100) {
                newAi.setHumidity(dp);
                newAi.setLastUpdateTime(System.currentTimeMillis());
                return newAi;
            }
        }

        if (pointType.equalsIgnoreCase(DataPointType.PRESSURE.name())) {
            if (dp.getMean() >= 650 && dp.getMean() < 800) {
                newAi.setPressure(dp);
                newAi.setLastUpdateTime(System.currentTimeMillis());
                return newAi;
            }
        }

        if (pointType.equalsIgnoreCase(DataPointType.CLOUDCOVER.name())) {
            if (dp.getMean() >= 0 && dp.getMean() < 100) {
                newAi.setCloudCover(dp);
                newAi.setLastUpdateTime(System.currentTimeMillis());
                return newAi;
            }
        }

        if (pointType.equalsIgnoreCase(DataPointType.PRECIPITATION.name())) {
            if (dp.getMean() >=0 && dp.getMean() < 100) {
                newAi.setPrecipitation(dp);
                newAi.setLastUpdateTime(System.currentTimeMillis());
                return newAi;
            }
        }

        throw new IllegalStateException("couldn't update atmospheric data");
    }

    /**
     * Create a new airport record in the storage
     */
    public AirportData addAirport(String iataCode, double latitude, double longitude) {
    	AirportData airport = new AirportData();
    	airport.setIata(iataCode);
    	airport.setLatitude(latitude);
    	airport.setLongitude(longitude);
    	validateAirport(airport);
    	return AWADataStorage.getInstance().addAirport(iataCode, latitude, longitude);
    }

    public AirportData addAirport(AirportData airport) {
    	validateAirport(airport);
        return AWADataStorage.getInstance().addAirport(airport);
    }

    
    /**
     * Several storage getters below
     */
    public List<AirportData> getAirportData() {
		return AWADataStorage.getInstance().getAirportData();
	}

	public List<AtmosphericInformation> getAtmosphericInformation() {
		return AWADataStorage.getInstance().getAtmosphericInformation();
	}

	public Map<AirportData, Integer> getRequestFrequency() {
		return AWADataStorage.getInstance().getRequestFrequency();
	}

	public Map<Double, Integer> getRadiusFreq() {
		return AWADataStorage.getInstance().getRadiusFreq();
	}

	@Override
	public AtmosphericInformation getAtmosphericInformation(String iata) {
		return AWADataStorage.getInstance().getAtmosphericInformation(iata);
	}

	/**
	 * Remove particular airport record from storage
     * @param iata the Airport IATA code
     * @return 1- if airport was in storage, 0 - if airport not found in storage (nothing to remove)
	 */
	@Override
	public int removeAirport(String iata) {

		if( isIataInvalid(iata) ) {
			throw new IllegalArgumentException("IATA code is incorrect");
		}

		if(findAirportData(iata) != null) {
			AWADataStorage.getInstance().removeAirport(iata);
			return 1;
		} else {
			return 0;
		}
		
	}

	private void validateAirport(AirportData airport) {
		// IATA checks
		if( isIataInvalid(airport.getIata()) ) {
			throw new IllegalArgumentException("IATA code is incorrect");
		}
		//Latitude checks
		if( airport.getLatitude() == 0 ) {
			throw new IllegalArgumentException("Latitude should be set");
		}
		//Longitude checks
		if( airport.getLongitude() == 0 ) {
			throw new IllegalArgumentException("Longitude should be set");
		}
	}

	private boolean isIataInvalid(String iata) {
		return iata == null || iata.isEmpty() || iata.length() != 3;
	}
}
