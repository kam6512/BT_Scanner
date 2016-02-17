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

        // DynamicRealm exposes an editable schema
        RealmSchema schema = realm.getSchema();

        // Migrate to version 1: Add a new class.
        // Example:
        // public Person extends RealmObject {
        //     private String name;
        //     private int age;
        //     // getters and setters left out for brevity
        // }

        if (oldVersion == 0) {
            schema.create("RealmPrimeItem")
                    .addField("calendar", String.class)
                    .addField("step", int.class)
                    .addField("calorie", int.class)
                    .addField("distance", int.class);
            oldVersion++;
        }

        // Migrate to version 2: Add a primary key + object references
        // Example:
        // public Person extends RealmObject {
        //     private String name;
        //     @PrimaryKey
        //     private int age;
        //     private Dog favoriteDog;
        //     private RealmList<Dog> dogs;
        //     // getters and setters left out for brevity
        // }
        if (oldVersion == 1) {
            schema.get("Person");
            oldVersion++;
        }
    }
}