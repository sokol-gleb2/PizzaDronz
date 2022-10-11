package uk.ac.ed.inf;

public class Menu {
    String name;
    String priceInPence;

    public String[] getMenu() {
        return new String[]{name, priceInPence};
    }
}
