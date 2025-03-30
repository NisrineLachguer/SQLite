package com.example.sqlite.Service;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.sqlite.classes.Etudiant;
import com.example.sqlite.util.MySQLiteHelper;

import java.util.ArrayList;
import java.util.List;

public class EtudiantService {
    private static final String TABLE_NAME = "etudiant";
    private static final String KEY_ID = "id";
    private static final String KEY_NOM = "nom";
    private static final String KEY_PRENOM = "prenom";
    private static final String KEY_DATE_NAISSANCE = "date_naissance";
    private static final String KEY_PHOTO = "photo";

    private MySQLiteHelper helper;

    public EtudiantService(Context context) {

        try {
            this.helper = new MySQLiteHelper(context);
            Log.d("EtudiantService", "Database helper initialized successfully");
        } catch (Exception e) {
            Log.e("EtudiantService", "Error initializing database helper", e);
            throw e;
        }    }

    public void create(Etudiant e) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_NOM, e.getNom());
        values.put(KEY_PRENOM, e.getPrenom());
        values.put(KEY_DATE_NAISSANCE, e.getDateNaissance());
        values.put(KEY_PHOTO, e.getPhoto());

        db.insert(TABLE_NAME, null, values);
        db.close();
    }

    public void update(Etudiant e) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_NOM, e.getNom());
        values.put(KEY_PRENOM, e.getPrenom());
        values.put(KEY_DATE_NAISSANCE, e.getDateNaissance());
        values.put(KEY_PHOTO, e.getPhoto());

        db.update(TABLE_NAME, values, "id = ?", new String[]{String.valueOf(e.getId())});
        db.close();
    }

    public Etudiant findById(int id) {
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor c = db.query(TABLE_NAME, null, "id = ?", new String[]{String.valueOf(id)}, null, null, null);

        if (c.moveToFirst()) {
            Etudiant e = new Etudiant();
            e.setId(c.getInt(0));
            e.setNom(c.getString(1));
            e.setPrenom(c.getString(2));
            e.setDateNaissance(c.getString(3));
            e.setPhoto(c.getBlob(4));
            db.close();
            return e;
        }
        db.close();
        return null;
    }

    public List<Etudiant> findAll() {
        List<Etudiant> etudiants = new ArrayList<>();
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM " + TABLE_NAME, null);

        if (c.moveToFirst()) {
            do {
                Etudiant e = new Etudiant();
                e.setId(c.getInt(0));
                e.setNom(c.getString(1));
                e.setPrenom(c.getString(2));
                e.setDateNaissance(c.getString(3));
                e.setPhoto(c.getBlob(4));
                etudiants.add(e);
            } while (c.moveToNext());
        }
        db.close();
        return etudiants;
    }

    public void delete(Etudiant e) {
        SQLiteDatabase db = helper.getWritableDatabase();
        db.delete(TABLE_NAME, "id = ?", new String[]{String.valueOf(e.getId())});
        db.close();
    }
}