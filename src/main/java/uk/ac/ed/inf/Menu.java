package uk.ac.ed.inf;

public class Menu {
    String name;
    int priceInPence;
    // Is this supposed to be int??

    public String[] getMenu() {
        return new String[]{name, String.valueOf(priceInPence)};
    }
}
