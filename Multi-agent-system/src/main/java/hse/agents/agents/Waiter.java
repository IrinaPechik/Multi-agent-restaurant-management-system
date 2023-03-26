package hse.agents.agents;

import hse.agents.behaviours.ServePatrons;
import hse.agents.behaviours.ServiceDiscovery;
import hse.agents.configuration.JadeAgent;
import jade.core.AID;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import  hse.agents.util.Dish;
import hse.agents.util.MyPair;

import java.util.ArrayList;

@JadeAgent
public class Waiter extends RestaurantAgent
{
    private static final long serialVersionUID = 7110642579660810600L;
    private AID kitchen;
    private ArrayList<Dish> knownDishes = new ArrayList<>();
    private ArrayList<MyPair<AID, Boolean>> waiters = new ArrayList<>();
    private AID cur_customerID;
    private double tips = 0;
    private int waiterIndex = 0;

    protected void setup() {
        role = "WebPage";

        DFAgentDescription dfd = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();

        dfd.setName(this.getAID());
        sd.setType("customer-service");
        sd.setName("mas-restaurant");
        dfd.addServices(sd);

        try {
            DFService.register(this, dfd);
        }
        catch(FIPAException e) {
            e.printStackTrace();
        }

        printMessage("Checking in.");

        if(!searchForKitchen())
            this.doDelete();

        this.addBehaviour(new ServiceDiscovery(this, 500));
        this.addBehaviour(new ServePatrons(this));
    }

    private boolean searchForKitchen() {
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();

        sd.setType("kitchen-service");
        template.addServices(sd);

        try {
            DFAgentDescription[] kitchenSearch = DFService.search(this, template);

            if(kitchenSearch.length > 0)
                kitchen = kitchenSearch[0].getName();
            else {
                printMessage("Couldn't find the kitchen...");
                return false;
            }    
        }
        catch(FIPAException e) {
            e.printStackTrace();
        }

        return true;
    }

    protected void takeDown() {
        deRegister();
        printMessage("Going home.");
    }

    private void deRegister()
    {
        try {
            DFService.deregister(this);
        }
        catch(FIPAException e) {
            e.printStackTrace();
        }
    }

    // Обновляет информацию о ранее известном блюде.
    public void updateKnownDish(Dish newDish) {
        int dishIndex = knownDishes.indexOf(newDish);
        Dish knownDish = knownDishes.get(dishIndex);
        
        if(isDishInfoReliable(newDish) || !isDishInfoReliable(knownDish))
            knownDishes.set(dishIndex, newDish);
        else {
                if(newDish.getAvailability() < knownDish.getAvailability())
                    knownDish.setAvailability(newDish.getAvailability());
        }    
    }

    public boolean isDishInfoReliable(Dish dish) {
        return dish.getInfoSrc().equals(kitchen);
    }

    @Override
    public void addWaiters(AID[] newWaiters) {
        boolean found;

        for(AID newWaiter : newWaiters) {
            found = false;

            if(newWaiter.equals(this.getAID()))
                continue;

            for(MyPair<AID, Boolean> waiter : waiters)
                if(waiter.getKey().equals(newWaiter)) {
                    found = true;
                    break;
                }

            if(!found)
                waiters.add(new MyPair<>(newWaiter, true));
        }
    }

    public void resetWaiterIndex() {
        for(int i = 0; i < waiters.size(); i++)
            if(waiters.get(i).getValue()) {
                waiterIndex = i;
                return;
            }
    }

    public AID getNextReliableWaiter() {
        MyPair<AID, Boolean> waiter;

        if(waiters.size() == 0 || waiterIndex >= waiters.size())
            return null;

        do {
            waiter = waiters.get(waiterIndex);
            waiterIndex++;
        }
        while(!waiter.getValue() && waiterIndex < waiters.size());

        if(waiter.getValue())
            return waiter.getKey();
        else
            return null;
    }

    public void addTip(double sum) {
        tips += sum;
    }

    public boolean isBusy() {
        return cur_customerID != null;
    }

    public AID getKitchen() {
        return kitchen;
    }

    public AID getCur_customerID() {
        return cur_customerID;
    }

    public MyPair<AID, Boolean> getWaiter(AID waiter) {
        for(MyPair<AID, Boolean> knownWaiter : waiters)
            if(knownWaiter.getKey().equals(waiter))
                return knownWaiter;

        return null;
    }

    public ArrayList<Dish> getDishes() {
        return knownDishes;
    }

    public double getTips() {
        return tips;
    }

    public int getKnownDishIndex(String dishName) {
        for(int i = 0; i < knownDishes.size(); i++)
            if(knownDishes.get(i).getName().equals(dishName))
                return i;

        return -1;
    }

    public Dish getDish(String dishName) {
        int index = getKnownDishIndex(dishName);

        if(index == -1)
            return null;
        else
            return knownDishes.get(index);
    }

    public void setCur_customerID(AID cid) {
        cur_customerID = cid;
    }
}