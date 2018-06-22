package com.gmail.jaaska.jaakko.calendarcountdown.data;

/**
 * Container for general application settings.
 *
 * Created by jaakko on 27.11.2016.
 */

public class GeneralSettings {

    public static final int SORT_BY_DAYS_LEFT = 0;
    public static final int SORT_BY_EVENT_LABEL = 1;

    private int sortOrder;

    private static GeneralSettings instance;

    /**
     * Constructor that defaults the setting to their default values.
     */
    private GeneralSettings() {
        sortOrder = SORT_BY_DAYS_LEFT;
    }

    public static GeneralSettings getInstance() {
        if (instance == null) {
            instance = new GeneralSettings();
        }

        return instance;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(int newOrder) {
        sortOrder = newOrder;
    }

}
