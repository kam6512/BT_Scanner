package com.rainbow.kam.bt_scanner.data.item;

import io.realm.DynamicRealm;
import io.realm.RealmMigration;
import io.realm.RealmSchema;

/**
 * Created by kam6512 on 2016-02-12.
 */
public class Migration implements RealmMigration {


    @Override
    public void migrate(DynamicRealm realm, long oldVersion, long newVersion) {

        RealmSchema schema = realm.getSchema();
        if (oldVersion == 0) {
            schema.create("RealmPrimeItem")
                    .addField("calendar", String.class)
                    .addField("step", int.class)
                    .addField("calorie", int.class)
                    .addField("distance", int.class);
            oldVersion++;
        }
        if (oldVersion == 1) {
            schema.get("RealmPrimeItem");
            oldVersion++;
        }
    }
}