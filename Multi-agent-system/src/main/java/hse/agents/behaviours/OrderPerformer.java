package hse.agents.behaviours;

import hse.agents.agents.Customer;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.ContractNetInitiator;

public class OrderPerformer extends ContractNetInitiator {
    private static final long serialVersionUID = 2897989135282380056L;
    private Customer current_customer;
    public OrderPerformer(Customer c, ACLMessage cfp) {
        super(c, cfp);
        current_customer = c;
    }

    // Этот метод вызывается каждый раз при получении сообщения,
    // которое не соответствует последовательности в соответствии
    // с правилами протокола.
    @Override
    protected void handleOutOfSequence(ACLMessage aclMessage) {
        switch(aclMessage.getPerformative()) {
            case ACLMessage.FAILURE:
                handleFailure(aclMessage);
                break;
            case ACLMessage.REFUSE:
                handleRefuse(aclMessage);
                break;
            case ACLMessage.INFORM:
                handleInform(aclMessage);
                break;
            case ACLMessage.PROPOSE:
                handlePropose(aclMessage);
                break;
            default:
                break;
        }
    }

    private void orderAgain() {
        if (current_customer.getAttempts() >= 3) {
            current_customer.printMessage("Too much attempts for me. Sorry, I will go!");
            current_customer.doDelete();
        }
        else {
            current_customer.printMessage("Okay, I'll try to choose something else!");
            current_customer.increaseAttempts();
            current_customer.orderDish();
        }
    }

    // Если блюдо недоступно для заказв.
    @Override
    protected void handleRefuse(ACLMessage msg) {
        orderAgain();
    }

    // Вытягиваем рекомендации по выбору блюда.
    private void handlePropose(ACLMessage propose) {
        current_customer.sendMessage(propose.getSender(),
                ACLMessage.ACCEPT_PROPOSAL, FIPANames.InteractionProtocol.FIPA_CONTRACT_NET,
                "dish-feedback", current_customer.getWantedDish() + " - original");
    }

    @Override
    protected void handleFailure(ACLMessage failure) {
        handleRefuse(failure);
    }

    @Override
    protected void handleInform(ACLMessage inform) {
        // поведение, которое будет ждать сообщение от официанта о доставке еды.
        ReceiveMeal receiveMeal = new ReceiveMeal(current_customer, MessageTemplate.and(MessageTemplate.MatchSender(current_customer.getWebPage()), MessageTemplate.and(MessageTemplate.MatchConversationId("meal-delivering"), MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST))));
        current_customer.addBehaviour(receiveMeal);
        // сбрасывает текущий протокол взаимодействия и готовится к обработке новых сообщений.
        this.reset();
    }
}
