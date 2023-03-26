package hse.agents.behaviours;

import hse.agents.agents.Waiter;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.WakerBehaviour;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.text.DecimalFormat;

public class DeliverFood extends WakerBehaviour {

    private static final long serialVersionUID = -1854723296124682854L;
    private AID customer;
    private Waiter myWebPage;
    private String dish;
    private int dishAvailability;
    private int dishCookingTime;

    DeliverFood(Agent a, long timeout, AID customer, String[] dish) {
        super(a, timeout);

        myWebPage = (Waiter) a;
        this.customer = customer;
        this.dish = dish[0];
        dishAvailability = Integer.parseInt(dish[1]);
        dishCookingTime = Integer.parseInt(dish[2]);
    }

    @Override
    public void onWake() {
        myWebPage.printMessage("A " + dish + " you can pick up at the transfer table, " + customer.getLocalName() + ".");
        myWebPage.sendMessage(customer, ACLMessage.REQUEST, FIPANames.InteractionProtocol.FIPA_REQUEST,
                "meal-delivering", dish);
        getCustomerFeedback();
    }
    private void receiveTip(ACLMessage msg) {
        String[] contents = msg.getContent().split("-");
        double tip = Double.parseDouble(contents[0]);
        myWebPage.addTip(tip);
        myWebPage.printMessage("Thank you very much " + customer.getLocalName() + " for the " + tip + " rub.!");

        DecimalFormat df = new DecimalFormat();
        df.setMaximumFractionDigits(2);

        String totalTips = df.format(myWebPage.getTips());

        myWebPage.printMessage("We have already collected " + totalTips + " rub.");
    }

    private void getCustomerFeedback() {
        MessageTemplate template = MessageTemplate.and(MessageTemplate.MatchConversationId("meal-delivering"),
                MessageTemplate.MatchPerformative(ACLMessage.AGREE));
        ACLMessage aclMessage;

        do {
            aclMessage = myWebPage.receive(template);

            if (aclMessage == null)
                block();
        }
        while (aclMessage == null);

        ACLMessage secondMessage;
        MessageTemplate secondTemplate = MessageTemplate.and(MessageTemplate.MatchConversationId("meal-delivering"),
                MessageTemplate.MatchPerformative(ACLMessage.INFORM));

        do {
            secondMessage = myWebPage.receive(secondTemplate);

            if (secondMessage == null)
                block();
        }
        while (secondMessage == null);

        receiveTip(secondMessage);
    }

}
