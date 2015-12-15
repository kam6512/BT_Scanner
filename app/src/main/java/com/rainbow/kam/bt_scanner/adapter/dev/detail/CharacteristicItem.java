package com.rainbow.kam.bt_scanner.adapter.dev.detail;

/**
 * Created by kam6512 on 2015-10-29.
 */
public class CharacteristicItem {
    private final String title;
    private final String uuid;

    public CharacteristicItem(String title, String uuid) {
        this.title = title;
        this.uuid = uuid;
    }

    public String getTitle() {
        return this.title;
    }

    public String getUuid() {
        return this.uuid;
    }
}
