package hse.agents.behaviours;

import hse.agents.agents.Waiter;
import jade.core.AID;
import jade.core.behaviours.SimpleBehaviour;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import hse.agents.util.Dish;
import hse.agents.util.MyPair;

public class TakeFoodRequest extends SimpleBehaviour {

    private static final long serialVersionUID = 7818256748738825651L;
    private int stride = 1;
    private Waiter myWaiter;
    private int refuseProposalCounter = 0;

    public TakeFoodRequest(Waiter waiter) {
        myWaiter = waiter;
    }

    @Override
    public void action() {
        ACLMessage msg;
        MessageTemplate template;

        switch (stride) {
            case 1:
                template = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.CFP),
                        MessageTemplate.MatchConversationId("order-request"));
                msg = myWaiter.receive(template);

                if (msg != null)
                    getOrder(msg);
                else
                    block();
                break;

            case 2:
                template = MessageTemplate.and(MessageTemplate.MatchConversationId("dish-details"),
                        MessageTemplate.or(MessageTemplate.MatchPerformative(ACLMessage.FAILURE),
                                MessageTemplate.MatchPerformative(ACLMessage.INFORM)));
                msg = myWaiter.receive(template);

                if (msg != null)
                    receiveDishDetails(msg);
                else
                    block();

                break;

            case 3:
                template = MessageTemplate.MatchConversationId("dish-feedback");
                msg = myWaiter.receive(template);

                if (msg != null)
                    getCustomerFeedback(msg);
                else
                    block();

                break;

            case 4:
                template = MessageTemplate.MatchConversationId("start-dish");
                msg = myWaiter.receive(template);

                if (msg != null) //Message: <dish quantity time prep>
                    getKitchenFinalCheck(msg);
                else
                    block();
                break;

            case 5:
                template = MessageTemplate.and(
                        MessageTemplate.or(MessageTemplate.MatchPerformative(ACLMessage.AGREE), MessageTemplate.MatchPerformative(ACLMessage.REFUSE)),
                        MessageTemplate.or(MessageTemplate.MatchConversationId("dish-details"), MessageTemplate.MatchConversationId("start-dish")));

                msg = myWaiter.receive(template);

                if (msg != null)
                    receiveInfoAck(msg);
                else
                    block();
                break;
        }
    }

    @Override
    public boolean done() {
        return stride == 6 || refuseProposalCounter >= 3;
    }

    private void getKitchenFinalCheck(ACLMessage msg) {
        String[] dishInfo = msg.getContent().split(" - ");
        Dish dish = myWaiter.getDish(dishInfo[0]);

        if (dish != null && !dish.compareStaticDetails(dishInfo[0], Integer.parseInt(dishInfo[2]))) {
            MyPair<AID, Boolean> otherWaiter = myWaiter.getWaiter(dish.getInfoSrc());

            if (otherWaiter != null) {
                otherWaiter.setValue(false);
                myWaiter.printMessage("*Thinking* " + otherWaiter.getKey().getLocalName() + " was lying, I'll take note of that...");
            }

        }

        if (dish != null) {
            dish.setAvailability(Integer.parseInt(dishInfo[1]));
            dish.setCookingTime(Integer.parseInt(dishInfo[2]));
            dish.setInfoSrc(myWaiter.getKitchen());
        }

        if (msg.getPerformative() == ACLMessage.REFUSE) {
            stride = 1;
            myWaiter.printMessage("Unfortunately, we have run out of " + dishInfo[0] + ".");
            myWaiter.sendMessage(myWaiter.getCur_customerID(), ACLMessage.FAILURE, FIPANames.InteractionProtocol.FIPA_CONTRACT_NET,
                    "order-request", "unavailable");
        } else {
            stride = 6;
            myWaiter.printMessage("Your dish is already being prepared");
            myWaiter.sendMessage(myWaiter.getCur_customerID(), ACLMessage.INFORM, FIPANames.InteractionProtocol.FIPA_CONTRACT_NET,
                    "order-request", dishInfo[2] + " - " + dishInfo[3]);
            myWaiter.addBehaviour(new DeliverFood(myAgent, Long.parseLong(dishInfo[2]) * 1000,
                    myWaiter.getCur_customerID(), dishInfo));
            myWaiter.setCur_customerID(null);
        }
    }

    private void getCustomerFeedback(ACLMessage msg) {
        String[] msgDetails = msg.getContent().split(" - ");

        if (msg.getPerformative() == ACLMessage.ACCEPT_PROPOSAL
                || (msgDetails.length > 1 && msgDetails[1].equals("original"))) {
            Dish dish = myWaiter.getDish(msgDetails[0]);
            stride = 5;
            dish.decreaseAvailability();
            myWaiter.sendMessage(myWaiter.getKitchen(), ACLMessage.REQUEST, FIPANames.InteractionProtocol.FIPA_REQUEST,
                    "start-dish", dish.getName());
        } else {
            stride = 1;
            refuseProposalCounter++;
        }

    }

    private void evaluateDish(Dish dish, String infoSource) {
        if (dish.getAvailability() == 0) {
            stride = 1;
            myWaiter.printMessage("I'm sorry, it seems that we're all out of " + dish.getName() + ".");
            myWaiter.sendMessage(myWaiter.getCur_customerID(), ACLMessage.REFUSE,
                    FIPANames.InteractionProtocol.FIPA_CONTRACT_NET, "order-request", "unavailable");
        } else
            stride = 3;
            myWaiter.printMessage("Excellent choice of dishes.");
            myWaiter.sendMessage(myWaiter.getCur_customerID(), ACLMessage.PROPOSE,
                FIPANames.InteractionProtocol.FIPA_CONTRACT_NET, "order-request",
                dish.getName() + " - " + infoSource);
    }

    private void receiveInfoAck(ACLMessage msg) {
        if (msg.getPerformative() == ACLMessage.AGREE) {
            if (msg.getConversationId().equals("dish-details"))
                stride = 2;
            else
                stride = 4;
        } else {
            if (!msg.getSender().equals(myWaiter.getKitchen())) {
                AID nextAgent = myWaiter.getNextReliableWaiter();

                if (nextAgent == null) {
                    nextAgent = myWaiter.getKitchen();
                } else
                    myWaiter.printMessage("Okay... How about you " + nextAgent.getLocalName() + "?");

                stride = 5;
                myWaiter.sendMessage(nextAgent, ACLMessage.REQUEST,
                        FIPANames.InteractionProtocol.FIPA_REQUEST, "dish-details", msg.getContent());
            } else
                System.out.println("Sorry, the kitchen was unable to fulfill your request");
        }
    }

    private void receiveDishDetails(ACLMessage msg) {
        String content = msg.getContent();

        if (msg.getPerformative() == ACLMessage.FAILURE) {
            if (msg.getSender().equals(myWaiter.getKitchen())) {
                stride = 1;
                myWaiter.printMessage("I'm afraid we don't serve that dish in here. Try another one.");
                myWaiter.sendMessage(myWaiter.getCur_customerID(), ACLMessage.REFUSE,
                        FIPANames.InteractionProtocol.FIPA_CONTRACT_NET, "order-request", "not-found");
                return;
            } else {
                myWaiter.sendMessage(myWaiter.getKitchen(), ACLMessage.REQUEST,
                        FIPANames.InteractionProtocol.FIPA_REQUEST, "dish-details", msg.getContent());
                stride = 5;
                return;
            }
        }

        String[] dishDetails = content.split(" - "); //Message format: "dish - availability - cookingTime - preparationRate"

        if (dishDetails.length < 4)
            System.out.println(msg.getSender().getLocalName() + " | " + msg.getPerformative() + " | " + content);

        Dish dish = new Dish(dishDetails[0], Integer.parseInt(dishDetails[1]), Integer.parseInt(dishDetails[2]),
                msg.getSender());
        String infoSrc;

        if (myWaiter.getDishes().contains(dish))
            myWaiter.updateKnownDish(dish);
        else
            myWaiter.getDishes().add(dish);

        if (myWaiter.isDishInfoReliable(dish))
            infoSrc = "kitchen";
        else
            infoSrc = "waiter";

        evaluateDish(dish, infoSrc);
    }

    private void getOrder(ACLMessage msg) {
        String[] customerDetails = msg.getContent().split(" - "); //Message: <Dish - Mood>
        String dish = customerDetails[0];
        int index;

        myWaiter.resetWaiterIndex();
        if ((index = myWaiter.getKnownDishIndex(customerDetails[0])) == -1) {
            stride = 5;
            myWaiter.printMessage("Just a moment, I need to confirm with our kitchen team.");
            myWaiter.sendMessage(myWaiter.getKitchen(), ACLMessage.REQUEST, FIPANames.InteractionProtocol.FIPA_REQUEST,
                    "dish-details", dish);
        } else
            evaluateDish(myWaiter.getDishes().get(index), "kitchen");
    }
}