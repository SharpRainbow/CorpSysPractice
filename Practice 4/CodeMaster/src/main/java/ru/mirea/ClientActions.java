package ru.mirea;

public enum ClientActions {

    READY("ready");

    private final String name;

    ClientActions(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

}
