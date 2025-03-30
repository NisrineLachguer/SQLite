package com.example.sqlite.util;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class MySQLiteHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 2;
    private static final String DATABASE_NAME = "ecole";

    private static final String CREATE_TABLE_ETUDIANT = "create table etudiant(" +
            "id INTEGER primary key autoincrement," +
            "nom TEXT," +
            "prenom TEXT," +
            "date_naissance TEXT," +
            "photo BLOB)";

    public MySQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            db.execSQL(CREATE_TABLE_ETUDIANT);
            Log.d("MySQLiteHelper", "Table etudiant created successfully");
        } catch (Exception e) {
            Log.e("MySQLiteHelper", "Error creating table", e);
            throw e;
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP table if exists etudiant");
        this.onCreate(db);
    }
}