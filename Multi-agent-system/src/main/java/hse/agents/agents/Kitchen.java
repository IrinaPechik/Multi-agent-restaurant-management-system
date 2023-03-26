package hse.agents.agents;

import hse.agents.behaviours.ReceiveInquiry;
import hse.agents.configuration.JadeAgent;
import hse.agents.util.ReadDishes;
import jade.core.AID;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import java.util.HashMap;
@JadeAgent
public class Kitchen extends RestaurantAgent {
    private static final long serialVersionUID = 1L;
    private HashMap<String, int[]> meals;
    private static String[] dishes;

    @Override
    protected void setup() {
        role = "Kitchen";
        // Объект описания агента.
        DFAgentDescription dfAgentDescription = new DFAgentDescription();
        // Объект типа данных сервиса.
        ServiceDescription serviceDescription = new ServiceDescription();

        dfAgentDescription.setName(this.getAID());
        serviceDescription.setType("kitchen-service");
        serviceDescription.setName("mas-restaurant");

        dfAgentDescription.addServices(serviceDescription);

        // Пытаемся зарегистрировать агента-кухню.
        try {
            DFService.register(this, dfAgentDescription);
        } catch (FIPAException exception) {
            exception.printStackTrace();
        }

        meals = new HashMap<>();
        ReadDishes.read();
        dishes = ReadDishes.getDishes();
        // Получаем массив аргументов, которые были переданы этому агенту.
        Object[] args = getArguments();
        if(args!= null && args.length > 0)
            setMeals(args);
        else
            this.generateMeals();
        System.out.println("(kitchen) Kitchen " + this.getAID().getLocalName() + " at your service.");

        // Ждём запросов официантов.
        this.addBehaviour(new ReceiveInquiry(this));
    }
    public static String[] getMenu() {
        return dishes;
    }

    public HashMap<String, int[]> getMeals() {
        return meals;
    }
    private void setMeals(Object[] newMeals) {
        for (Object newMeal : newMeals) {
            String[] dishDetails = ((String) newMeal).split("-");
            meals.put(dishDetails[0], new int[]{Integer.parseInt(dishDetails[1]), Integer.parseInt(dishDetails[2]), Integer.parseInt(dishDetails[3])});
        }
    }

    private void generateMeals() {
        int prepTime, availability, price;

        for(int i = 0; i < dishes.length; i++) {
            prepTime = ReadDishes.getPrepTimes()[i];
            availability = ReadDishes.getAvailables()[i];
            price = ReadDishes.getPrices()[i];
            meals.put(dishes[i], new int[] {availability, prepTime, price});
        }
    }

    public Boolean checkMeal(String dish) {
        return meals.containsKey(dish);
    }

    public int[] getMealInfo(String dish) {
        return meals.get(dish);
    }

    protected void takeDown() {
        try {
            DFService.deregister(this);
        }
        catch(FIPAException e) {
            e.printStackTrace();
        }

        System.out.println("(kitchen) Kitchen " + this.getAID().getLocalName() + " shutting down.");
    }
    @Override
    public void addWaiters(AID[] newWaiters) {}
}
