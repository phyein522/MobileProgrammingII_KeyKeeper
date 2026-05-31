package com.cookandroid.keykeeper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "KeyKeeperDB.db";
    private static final int DB_VERSION = 1;

    public static final String TABLE_ACCOUNT = "account";
    public static final String COL_ID = "id";
    public static final String COL_NAME = "name";
    public static final String COL_URL = "url";
    public static final String COL_PW = "pw";
    public static final String COL_LAST_CHANGED_DATE = "last_changed_date";
    public static final String COL_CHANGE_CYCLE = "change_cycle";
    public static final String COL_MEMO = "memo";
    public static final String COL_IV = "iv";

    public static final String TABLE_LINKED_ACCOUNT = "linked_account";
    public static final String COL_ACCOUNT_ID = "account_id";
    public static final String COL_LINKED_ACCOUNT_ID = "linked_account_id";

    public static final String TABLE_WARNING = "warning";
    public static final String COL_URL_WARNING = "url_warning";
    public static final String COL_PW_WARNING = "pw_warning";

    public static final String TABLE_PHISHING_URL = "phishing_url";

    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // account 테이블
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_ACCOUNT + "("
                + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT"
                + ", " + COL_NAME + " TEXT NOT NULL"
                + ", " + COL_URL + " TEXT"
                + ", " + COL_PW + " TEXT NOT NULL"
                + ", " + COL_LAST_CHANGED_DATE + " TEXT DEFAULT (DATETIME('now', 'localtime'))"
                + ", " + COL_CHANGE_CYCLE + " TEXT DEFAULT '3'"
                + ", " + COL_MEMO + " TEXT"
                + ", " + COL_IV + " TEXT NOT NULL"
                + ");"
        );
        /*
        CREATE TABLE IF NOT EXISTS account(
            id INTEGER PRIMARY KEY AUTOINCREMENT
            , name TEXT NOT NULL
            , url TEXT
            , pw TEXT NOT NULL
            , last_changed_date TEXT DEFAULT (DATETIME('now', 'localtime'))
            , change_cycle TEXT DEFAULT '3'
            , memo TEXT
            , iv TEXT NOT NULL
        );
        */

        // linked_account 테이블
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_LINKED_ACCOUNT + "("
                + COL_ACCOUNT_ID + " INTEGER"
                + ", " + COL_LINKED_ACCOUNT_ID + " INTEGER"
                + ", PRIMARY KEY(" + COL_ACCOUNT_ID + ", " + COL_LINKED_ACCOUNT_ID + ")"
                + ", FOREIGN KEY(" + COL_ACCOUNT_ID + ") REFERENCES " + TABLE_ACCOUNT + "(" + COL_ID + ") ON DELETE CASCADE"
                + ", FOREIGN KEY(" + COL_LINKED_ACCOUNT_ID + ") REFERENCES " + TABLE_ACCOUNT + "(" + COL_ID + ") ON DELETE CASCADE"
                + ", CHECK(" + COL_ACCOUNT_ID + " <> " + COL_LINKED_ACCOUNT_ID + ")"
                + ");"
        );
        /*
        CREATE TABLE IF NOT EXISTS linked_account(
            account_id INTEGER
            , linked_account_id INTEGER
            , PRIMARY KEY(account_id, linked_account_id)
            , FOREIGN KEY(account_id) REFERENCES account(id) ON DELETE CASCADE
            , FOREIGN KEY(linked_account_id) REFERENCES account(id) ON DELETE CASCADE
            , CHECK(account_id <> linked_account_id)
           );
        */

        // warning 테이블
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_WARNING + " ("
                + COL_ACCOUNT_ID + " INTEGER"
                + ", " + COL_URL_WARNING + " INTEGER"
                + ", " + COL_PW_WARNING + " INTEGER NOT NULL"
                + ", PRIMARY KEY(" + COL_ACCOUNT_ID + ")"
                + ", FOREIGN KEY(" + COL_ACCOUNT_ID + ") REFERENCES " + TABLE_ACCOUNT + "(" + COL_ID + ") ON DELETE CASCADE"
                + ");");
        /*
        CREATE TABLE IF NOT EXISTS warning (
            account_id INTEGER
            , url_warning INTEGER
            , pw_warning INTEGER NOT NULL
            , PRIMARY KEY(account_id)
            , FOREIGN KEY(account_id) REFERENCES account(id) ON DELETE CASCADE
        );
        */

        // phishing_url 테이블
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_PHISHING_URL + "("
                + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT"
                + ", " + COL_URL + " TEXT UNIQUE"
                + ");");
        /*
        CREATE TABLE IF NOT EXISTS phishing_url (
            id INTEGER PRIMARY kEY AUTOINCREMENT
            , url TEXT UNIQUE
        );
        */
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ACCOUNT);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LINKED_ACCOUNT);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_WARNING);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PHISHING_URL);
        onCreate(db);
    }

    //외래키 기능 활성화, ON DELETE CASCADE 동작
    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }
}
