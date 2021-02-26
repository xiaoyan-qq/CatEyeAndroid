package com.vtm.library.spatialite;

import android.content.Context;

import org.spatialite.database.SQLiteDatabase;
import org.spatialite.database.SQLiteOpenHelper;

public class SpatialiteDbHelper extends SQLiteOpenHelper {
    private static SpatialiteDbHelper instance;

    public SpatialiteDbHelper init(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        if (instance == null) {
            instance = new SpatialiteDbHelper(context, name, factory, version);
        }
        return instance;
    }

    public static SpatialiteDbHelper getInstance() {
        return instance;
    }

    public SpatialiteDbHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
