/**
 * Class responsible for giving structure to the Menus pulled from the server.
 * Each Menu is made up of name of pizza and how much it costs.
 * Mainly used as a list (Menu[]) for the Restaurant class.
 */

package uk.ac.ed.inf;

public class Menu {
    String name;
    int priceInPence;

    public Menu(String name, int priceInPence) {
        this.name = name;
        this.priceInPence = priceInPence;
    }

    public String[] getMenu() {
        return new String[]{name, String.valueOf(priceInPence)};
    }
}
