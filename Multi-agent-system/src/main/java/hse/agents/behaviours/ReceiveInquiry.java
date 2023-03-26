package hse.agents.behaviours;

import hse.agents.agents.Kitchen;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

public class ReceiveInquiry extends CyclicBehaviour {

    private static final long serialVersionUID = 3055341223034464997L;
    private Kitchen myKitchen;

    // конструктор получает экземпляр агента кухни, на котором данный поведение будет работать
    public ReceiveInquiry(Kitchen kitchen) {
        myKitchen = kitchen;
    }

    @Override
    public void action() {
        // получаем сообщение
        ACLMessage request = myKitchen.receive();

        if(request != null) {
            // формируем ответное сообщение
            ACLMessage reply = request.createReply();
            String meal = request.getContent();
            String mealString;

            // устанавливаем параметры ответного сообщения
            reply.setConversationId(request.getConversationId());
            reply.setPerformative(ACLMessage.AGREE);
            reply.setContent("ok");
            myKitchen.send(reply);

            // если блюдо доступно на кухне, обрабатываем запрос
            if(myKitchen.checkMeal(meal)) {
                int[] mealInfo = myKitchen.getMealInfo(meal);

                // уменьшаем количество доступных порций
                if(request.getConversationId().equals("start-dish")) {
                    mealInfo[0]--;
                    myKitchen.getMeals().put(meal, mealInfo);
                }

                // формируем строку с информацией о блюде
                mealString = meal + " - " + mealInfo[0] + " - " + mealInfo[1] + " - " + mealInfo[2];

                // устанавливаем параметры ответного сообщения
                reply.setContent(mealString);
                reply.setPerformative(ACLMessage.INFORM);
                myKitchen.printMessage("Here it is for you: " + mealString);
            } else {
                // если блюда нет на кухне, отправляем сообщение о неудаче
                reply.setContent(meal);
                reply.setPerformative(ACLMessage.FAILURE);
                myKitchen.printMessage("We don't have that!");
            }

            // отправляем ответное сообщение
            myKitchen.send(reply);
        }
        else
            block();
    }
}
