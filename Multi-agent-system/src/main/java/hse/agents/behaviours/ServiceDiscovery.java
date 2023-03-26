package hse.agents.behaviours;

import hse.agents.agents.RestaurantAgent;
import jade.core.AID;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
/*
   Класс поиска сервиса.
 */
public class ServiceDiscovery extends TickerBehaviour {
    
    private static final long serialVersionUID = -4766123904483710759L;
    private RestaurantAgent myRestaurantAgent;

    public ServiceDiscovery(RestaurantAgent a, long period) {
        // Вызывает метод onTick каждые period ms.
        super(a, period);
        myRestaurantAgent = a;
    }

    @Override
    protected void onTick() {
        // Представляет описание агента.
        DFAgentDescription template = new DFAgentDescription();
        // Моделирует тип данных сервиса.
        ServiceDescription sd = new ServiceDescription();
        sd.setType("customer-service");
        template.addServices(sd);

        try {
            // Ищем агентов-официантов
            DFAgentDescription[] result = DFService.search(myAgent, template);
            AID[] waiters = new AID[result.length];
            for(int i = 0; i < result.length; i++) {
                waiters[i] = result[i].getName();
            }

            // Если найден хотябы один официантов.
            if(result.length > 0)
                myRestaurantAgent.addWaiters(waiters);

        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
    }
}
