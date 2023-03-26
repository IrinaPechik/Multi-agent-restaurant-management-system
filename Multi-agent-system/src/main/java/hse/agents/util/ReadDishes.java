package hse.agents.util;

import com.google.gson.Gson;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ReadDishes {
    static String[] dishNamesArray = new String[0];
    static Integer[] dishAvailableArray = new Integer[0];
    static Integer[] dishPricesArray = new Integer[0];
    static Integer[] dishPrepTimeArray = new Integer[0];

    public static void read() {
        try (FileReader reader = new FileReader("sources/menu.json")) {
            Gson gson = new Gson();
            Menu menu = gson.fromJson(reader, Menu.class);

            List<String> dishNames = new ArrayList<>();
            List<Integer> dishAvailable = new ArrayList<>();
            List<Integer> dishPrices = new ArrayList<>();
            List<Integer> dishPrepTime = new ArrayList<>();

            for (Dishes dish : menu.menu) {
                dishNames.add(dish.name);
                dishAvailable.add(dish.available);
                dishPrices.add(dish.price);
                dishPrepTime.add(dish.prepTime);
            }
            dishNamesArray = dishNames.toArray(new String[0]);
            dishAvailableArray = dishAvailable.toArray(new Integer[0]);
            dishPricesArray = dishPrices.toArray(new Integer[0]);
            dishPrepTimeArray = dishPrepTime.toArray(new Integer[0]);

        } catch (FileNotFoundException e) {
            System.err.println("File not found: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error parsing JSON: " + e.getMessage());
        }
    }
    public static String[] getDishes() {
        return dishNamesArray;
    }
    public static Integer[] getAvailables() {
        return dishAvailableArray;
    }
    public static Integer[] getPrices() {
        return dishPricesArray;
    }
    public static Integer[] getPrepTimes() {
        return dishPrepTimeArray;
    }

}
class Menu {
    List<Dishes> menu;
}
class Dishes {
    int id;
    String name;
    int price;
    int available;
    int prepTime;
}
