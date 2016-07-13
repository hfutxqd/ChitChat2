package com.room517.chitchat.helpers;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.orhanobut.logger.Logger;
import com.room517.chitchat.App;

import static com.room517.chitchat.Def.DB.*;

/**
 * Created by ywwynm on 2016/5/24.
 * 数据库的帮助类
 */
public class DBHelper extends SQLiteOpenHelper {

    private static final String SQL_CREATE_TABLE_USER =
            "create table " + TableUser.TableName + " ("
            + TableUser.ID          + " text    primary key, "
            + TableUser.NAME        + " text    not null, "
            + TableUser.SEX         + " integer not null, "
            + TableUser.AVATAR      + " text, "
            + TableUser.TAG         + " text, "
            + TableUser.LONGITUDE   + " double  not null, "
            + TableUser.LATITUDE    + " double  not null, "
            + TableUser.CREATE_TIME + " integer not null, "
            + TableUser.COVER_URL + " text)";

    private static final String SQL_CREATE_TABLE_CHAT =
            "create table " + TableChat.TableName + " ("
            + TableChat.USER_ID + " text primary key, "
            + TableChat.TYPE    + " integer, "
            + "foreign key("
                + TableChat.USER_ID
            + ") references "
            + TableUser.ID + "("
                + TableUser.TableName
            + "))";

    public static final String SQL_CREATE_TABLE_CHAT_DETAIL =
            "create table " + TableChatDetail.TableName + " ("
            + TableChatDetail.ID      + " text    primary key, "
            + TableChatDetail.FROM_ID + " text    not null, "
            + TableChatDetail.TO_ID   + " text    not null, "
            + TableChatDetail.TYPE    + " integer not null, "
            + TableChatDetail.STATE   + " integer, "
            + TableChatDetail.CONTENT + " text    not null, "
            + TableChatDetail.TIME    + " integer not null, "
            + "foreign key("
                + TableChatDetail.FROM_ID
            + ") references "
            + TableUser.ID + "("
                + TableUser.TableName
            + "), "
            + "foreign key("
                + TableChatDetail.TO_ID
            + ") references "
            + TableUser.ID + "("
                + TableUser.TableName
            + "))";

    public static final String UPGRADE_VERSION_1_TO_VERSION_2 =
            "alter table " + TableUser.TableName  + " add "
                    + TableUser.COVER_URL + " text";

    public DBHelper() {
        super(App.getApp(), NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Logger.i(SQL_CREATE_TABLE_USER);
        Logger.i(SQL_CREATE_TABLE_CHAT);
        Logger.i(SQL_CREATE_TABLE_CHAT_DETAIL);

        db.execSQL(SQL_CREATE_TABLE_USER);
        db.execSQL(SQL_CREATE_TABLE_CHAT);
        db.execSQL(SQL_CREATE_TABLE_CHAT_DETAIL);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if(oldVersion == 1 && newVersion == 2) {
            db.execSQL(UPGRADE_VERSION_1_TO_VERSION_2);
        }
    }
}
