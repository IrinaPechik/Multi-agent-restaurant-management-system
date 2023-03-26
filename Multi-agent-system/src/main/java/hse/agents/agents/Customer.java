package hse.agents.agents;

import java.util.*;
import hse.agents.behaviours.OrderPerformer;
import hse.agents.behaviours.ServiceDiscovery;
import hse.agents.configuration.JadeAgent;
import jade.core.AID;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.proto.SimpleAchieveREInitiator;
@JadeAgent
public class Customer extends RestaurantAgent {
    private static final long serialVersionUID = 3921787877132989337L;
    // Желаемое посетителем блюдо.
    private String wantedDish;
    // Было ли выбрано блюдо.
    private boolean hasDishArgument;
    private HashSet<AID> unavailableWaiters = new HashSet<>();
    private boolean hasWaiter;
    private AID waiter;
    private ArrayList<AID> waiters = new ArrayList<>();
    private int attempts;
    private ServiceDiscovery serviceDiscovery;

    @Override
    protected void setup() {
        role = "Customer";
        printMessage("Hello! Customer: " + getAID().getLocalName() + " in a restaurant.");

        // На данный момент у него нет:
        // ни официанта, ни попыток сделать заказ, ни желаемого блюда.
        hasWaiter = false;
        attempts = 0;
        wantedDish = "";
        hasDishArgument = false;

        Object[] args = getArguments();
        if(args != null && args.length > 0) {
            wantedDish = (String) args[0];
            hasDishArgument = true;
        }

        // Посетитель пытается найти официанта каждые 1000 мс.
        serviceDiscovery = new ServiceDiscovery(this, 1000);
        addBehaviour(serviceDiscovery);
    }

    @Override
    public void addWaiters(AID[] newWaiters) {
        waiters = new ArrayList<>(Arrays.asList(newWaiters));
        if(!hasWaiter) {
            this.getAvailableWaiter();
        }
    }

    public int getAttempts() {
        return attempts;
    }

    public void increaseAttempts() {
        attempts++;
    }

    public String getWantedDish() {
        return wantedDish;
    }

    public AID getWebPage() {
        return waiter;
    }

    private AID getCurrentWebPage() {
        Random random = new Random();
        int index = random.nextInt(waiters.size());

        boolean allBusy = true;
        for (AID aid : waiters) {
            if(!unavailableWaiters.contains(aid)) {
                allBusy = false;
            }
        }
        if(allBusy) {
            return null;
        }

        while (unavailableWaiters.contains(waiters.get(index))) {
            index = random.nextInt(waiters.size());
        }
        return waiters.get(index);
    }

    // Ищем "официанта" электоронного.
    private void getAvailableWaiter() {
        AID currentWaiter = getCurrentWebPage();

        if(currentWaiter == null) {
            printMessage("All waiters are unavailable");
            doDelete();
            return;
        }

        // Вызываем доступного официанта.
        ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
        msg.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
        msg.addReceiver(currentWaiter);
        msg.setConversationId("waiter-request");
        msg.setContent("Be my waiter " + currentWaiter.getLocalName());

        printMessage("Are you available, " + currentWaiter.getLocalName() + "?");

        addBehaviour(new SimpleAchieveREInitiator(this, msg) {
            // Теперь у нас есть официант. И сейчас он занят.
            @Override
            protected void handleInform(ACLMessage inform) {
                hasWaiter = true;
                waiter = currentWaiter;
                serviceDiscovery.stop();
                orderDish();
            }

            @Override
            protected void handleRefuse(ACLMessage msg) {
                unavailableWaiters.add(currentWaiter);
            }
        });
    }

    // Выбор блюда.
    private void decideDish() {
        String oldDish = wantedDish;
        String[] dishes = Kitchen.getMenu();
        Random rand = new Random();

        wantedDish = dishes[rand.nextInt(dishes.length)];

        while(oldDish.equals(wantedDish)) {
            wantedDish = dishes[rand.nextInt(dishes.length)];
        }
    }

    // Заказываем еду.
    public void orderDish() {
        if(!hasDishArgument) {
            decideDish();
        }
        else {
            hasDishArgument = false;
        }

        printMessage("I want " + wantedDish + ".");

        ACLMessage msg = new ACLMessage(ACLMessage.CFP);
        msg.setProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);
        msg.setLanguage("English");
        msg.addReceiver(waiter);
        msg.setConversationId("order-request");
        msg.setContent(wantedDish);

        addBehaviour(new OrderPerformer(this, msg));
    }

    // Выход.
    @Override
    protected void takeDown() {
        printMessage("Going out");
    }
}

