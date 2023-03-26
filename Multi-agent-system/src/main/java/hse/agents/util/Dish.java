package hse.agents.util;

import jade.core.AID;

public class Dish {
    private String name;
    private int availability;
    private int prepTime;
    private AID infoSrc;

    public Dish(String name, int availability, int prepTime, AID infoSrc) {
        this.name = name;
        this.availability = availability;
        this.prepTime = prepTime;
        this.infoSrc = infoSrc;
    }

    @Override
    public boolean equals(Object dish) {
        if(dish instanceof Dish) 
            return ((Dish) dish).getName().equals(name);
        else
            return false;
    }

    public boolean compareStaticDetails(String dishName, int ct) {
        return dishName.equals(name) && ct == prepTime;
    }

    public void setAvailability(int availability) {
        this.availability = availability;
    }

    public void setCookingTime(int cookingTime) {
        this.prepTime = cookingTime;
    }

    public void setInfoSrc(AID infoSrc) {
        this.infoSrc = infoSrc;
    }

    public void decreaseAvailability() {
        availability--;
    }

    public String getName() {
        return name;
    }

    public int getAvailability() {
        return availability;
    }

    public AID getInfoSrc() {
        return infoSrc;
    }
}