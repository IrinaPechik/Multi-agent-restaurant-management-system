package hse.agents.app;

public class Restaurant {

    public static void main(String[] args) {
        // Инициализируем агентов Jade на основе классов, отмеченных аннотацией @JadeAgent.
        MainController mainController = new MainController();
        mainController.initAgents("hse.agents.agents");
    }
}
