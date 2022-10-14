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
