package com.cateye.vtm.db;

import android.content.Context;

import com.vtm.library.spatialite.SpatialiteDbHelper;

import org.spatialite.database.SQLiteDatabase;

public class SpatialiteDBHelper extends SpatialiteDbHelper {
    public SpatialiteDBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        super.onCreate(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        super.onUpgrade(db, oldVersion, newVersion);
    }
}
