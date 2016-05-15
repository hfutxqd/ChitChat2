package com.room517.chitchat;

/**
 * Created by ywwynm on 2016/5/14.
 * "侃侃"应用里的定义、常量
 */
public class Def {

    private Def() {}

    public static class Meta {

        public static final String PREFERENCE_NAME = "chitchat_preference";

    }

    public static class KEY {

        private static final String K = BuildConfig.APPLICATION_ID + ".key";

        public static final String PREF_FIRST_LAUNCH = "first_launch";

    }

    public static class DB {

    }

}
