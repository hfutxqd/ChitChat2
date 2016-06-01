package com.room517;

import de.greenrobot.daogenerator.DaoGenerator;
import de.greenrobot.daogenerator.Entity;
import de.greenrobot.daogenerator.Schema;

public class DatabaseGenerator {

    public static void main(String... args) throws Exception {
        Schema schema = new Schema(1, "com.room517.chitchat.db.bean");
        schema.setDefaultJavaPackageDao("com.room517.chitchat.db.dao");

        addTableUser(schema);

        new DaoGenerator().generateAll(schema, "app/src/main/java-gen");
    }

    private static void addTableUser(Schema schema) {
        Entity user = schema.addEntity("User");

        user.addStringProperty("id").primaryKey();
        user.addStringProperty("user").notNull();
        user.addIntProperty("sex").notNull();
        user.addStringProperty("avatar");
        user.addStringProperty("tag");
        user.addDoubleProperty("longitude").notNull();
        user.addDoubleProperty("latitude").notNull();
        user.addLongProperty("createTime").notNull();
    }

}
