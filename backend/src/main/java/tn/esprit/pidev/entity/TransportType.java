package tn.esprit.pidev.entity;

public enum TransportType {
    BUS("Bus"),
    METRO("Métro"),
    TRAIN("Train"),
    LOUAGE("Louage"),
    BATEAU("Bateau");

    private final String displayName;

    TransportType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
