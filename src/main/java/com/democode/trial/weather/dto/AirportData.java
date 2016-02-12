package com.democode.trial.weather.dto;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * Basic airport information.
 *
 * @author code test administrator
 */
public class AirportData {

    /** the three letter IATA code */
    private String iata;

    /** latitude value in degrees */
    private  double latitude;

    /** longitude value in degrees */
    private  double longitude;

    private String name;
    /**
     * Main city served by airport. May be spelled differently from name.
     */

    private String city;
    /**
     * Country or territory where airport is located.
	 * (blank or "" if not assigned)
     */
    private String country;

    /**
     * 4-letter ICAO code (blank or "" if not assigned)
     * Latitude Decimal degrees, up to 6 significant digits. Negative is South, positive is North.
     */    
    private String icao;
    /**
     * Hours offset from UTC. Fractional hours are expressed as decimals. (e.g. India is 5.5)
     */
    private String timezone;
    
    /**
     * One of E (Europe), A (US/Canada), S (South America), O (Australia), Z (New Zealand), N (None) or U (Unknown)
     */
    private String dst;
    
    
    public AirportData() { }

    public String getIata() {
        return iata;
    }

    public void setIata(String iata) {
        this.iata = iata;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getIcao() {
		return icao;
	}

	public void setIcao(String icao) {
		this.icao = icao;
	}

	public String getTimezone() {
		return timezone;
	}

	public void setTimezone(String timezone) {
		this.timezone = timezone;
	}

	public String getDst() {
		return dst;
	}

	public void setDst(String dst) {
		this.dst = dst;
	}

	public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.NO_CLASS_NAME_STYLE);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof AirportData) {
        	AirportData that = (AirportData)o;
        	if(that.getIata() != null) {
        		return that.getIata().equals(this.getIata());
        	}
        }

        return false;
    }

    @Override
    public int hashCode() {
    	if(this.iata != null) {
    		return iata.hashCode(); 
    	} else {
    		return "AirportData".hashCode();
    	}    	
    }
}
