package classes;

import annotations.KeyField;
import annotations.TaggedObject;

/**
 * User: kost
 * Date: 02.03.2015
 * Time: 4:49
 */
@TaggedObject(name = "Cities")
public class City extends Locality{
    private Boolean isMegapolis;

    public City(){super();}

    public City(String n, String c, int p, Boolean b) {
        super(n, c, p);
        isMegapolis = b;
    }

    @Override
    public boolean equals (Object obj) {
        if (obj instanceof City) {
            City c = (City)obj;
            return isMegapolis.equals((c.getIsMegapolis())) &&
                    getCityName().equals(c.getCityName()) &&
                    getCountry().equals(c.getCountry()) &&
                    getPopulation() == c.getPopulation();
        }
        return false;
    }

    public City(String n, String c) {
        super(n, c);
    }

    public Boolean getIsMegapolis() {
        return isMegapolis;
    }

    public void setIsMegapolis(Boolean isMegapolis) {
        this.isMegapolis = isMegapolis;
    }
}

@TaggedObject(name = "Locality")
class Locality {
    @KeyField
    private
    String cityName;

    @KeyField
    private
    String country;

    private int population;

    public Locality () {}

    public Locality (String n, String c, int p) {
        cityName = n;
        country = c;
        population = p;
    }

    public Locality (String n, String c) {
        cityName = n;
        country = c;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public int getPopulation() {
        return population;
    }

    public void setPopulation(int population) {
        this.population = population;
    }
}
