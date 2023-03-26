package hse.agents.behaviours;

import hse.agents.agents.Customer;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.SimpleAchieveREResponder;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Random;

public class ReceiveMeal extends SimpleAchieveREResponder {
    private Customer customer;
    private Boolean done;

    ReceiveMeal(Customer c, MessageTemplate mt) {
        super(c, mt);
        customer = c;
        done = false;
    }

    @Override
    protected ACLMessage prepareResponse(ACLMessage request) {
        ACLMessage response = request.createReply();
        response.setPerformative(ACLMessage.AGREE);
        response.setConversationId("meal-delivering");
        response.setContent("ok");

        customer.printMessage("Thank you!");
        customer.printMessage("I will pay now on your website");
        return response;
    }

    @Override
    protected synchronized ACLMessage prepareResultNotification(ACLMessage request, ACLMessage response) {
        Random rand = new Random();
        double tip = 200 + 0.01 * rand.nextInt(99) * 100;

        BigDecimal bd = BigDecimal.valueOf(tip);
        bd = bd.setScale(2, RoundingMode.HALF_UP);
        tip = bd.doubleValue();

        ACLMessage notification = request.createReply();
        notification.setPerformative(ACLMessage.INFORM);
        notification.setConversationId("meal-delivering");
        notification.setContent(String.valueOf(tip));

        customer.printMessage("Your tip: " + tip + " rub.");

        customer.addBehaviour(new OneShotBehaviour() {
            @Override
            public void action() {
                customer.doDelete();
            }
        });

        return notification;
    }

}
