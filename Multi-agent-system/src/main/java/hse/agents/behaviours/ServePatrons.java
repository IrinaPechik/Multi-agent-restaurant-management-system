package hse.agents.behaviours;

import hse.agents.agents.Waiter;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class ServePatrons extends CyclicBehaviour {
    private Waiter myWaiter;

    public ServePatrons(Waiter waiter) {
        myWaiter = waiter;
    }

    @Override
    public void action() {
        MessageTemplate template = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
                MessageTemplate.MatchConversationId("waiter-request"));
        ACLMessage msg = myWaiter.receive(template);

        if(msg != null)
            attendCustomer(msg);
        else
            block();
    }

    private void attendCustomer(ACLMessage msg) {
        if(myWaiter.isBusy()) {
            myWaiter.printMessage("Sorry, I am busy, " + msg.getSender().getLocalName() + ".");
            myWaiter.sendMessage(msg.getSender(), ACLMessage.REFUSE, FIPANames.InteractionProtocol.FIPA_REQUEST,
                    msg.getConversationId(), "busy");
        }
        else {
            myWaiter.setCur_customerID(msg.getSender());
            myWaiter.printMessage("You can place an order here " + msg.getSender().getLocalName() + ".");
            myWaiter.sendMessage(msg.getSender(), ACLMessage.AGREE, FIPANames.InteractionProtocol.FIPA_REQUEST,
                    msg.getConversationId(), "ok");
            myWaiter.addBehaviour(new TakeFoodRequest(myWaiter));
            myWaiter.printMessage("What do you want?");
            myWaiter.sendMessage(msg.getSender(), ACLMessage.INFORM, FIPANames.InteractionProtocol.FIPA_REQUEST,
                    msg.getConversationId(), "proceed");
        }
    }
}
