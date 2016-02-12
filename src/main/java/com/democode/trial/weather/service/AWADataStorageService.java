package com.democode.trial.weather.service;

import java.util.List;
import java.util.Map;

import com.democode.trial.weather.WeatherException;
import com.democode.trial.weather.dto.AirportData;
import com.democode.trial.weather.dto.AtmosphericInformation;
import com.democode.trial.weather.dto.DataPoint;

public interface AWADataStorageService {
	
	AirportData addAirport(String iataCode, double latitude, double longitude);
	AirportData addAirport(AirportData airport);
    List<AirportData> getAirportData();
	List<AtmosphericInformation> getAtmosphericInformation();
	Map<AirportData, Integer> getRequestFrequency();
	Map<Double, Integer> getRadiusFreq();
    void updateRequestFrequency(String iata, Double radius);
    AirportData findAirportData(String iataCode);
    int getAirportDataIdx(String iataCode);
    double calculateDistance(AirportData ad1, AirportData ad2);
    AtmosphericInformation updateAtmosphericInformation(AtmosphericInformation ai, String pointType, DataPoint dp) throws WeatherException;
    void processDataPoint(String iataCode, String pointType, DataPoint dp) throws WeatherException;
    AtmosphericInformation getAtmosphericInformation(String iata);
    int removeAirport(String iata);
}
