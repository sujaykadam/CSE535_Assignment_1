package com.sujay.assignment_1;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

public class AssignmentDbHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "Kadam.db";

    private static final String SQL_CREATE_ENTRIES=
            "CREATE TABLE tblPat ("
                    + " recID integer PRIMARY KEY autoincrement, "
                    + " timestamp TEXT, "
                    + " heartRate NUMERIC, "
                    + " respiratoryRate NUMERIC, "
                    + " symptomsNausea NUMERIC, "
                    + " symptomsHeadache NUMERIC, "
                    + " symptomsDiarrhea NUMERIC, "
                    + " symptomsSoarThroat NUMERIC, "
                    + " symptomsFever NUMERIC, "
                    + " symptomsMuscleAche NUMERIC, "
                    + " symptomsLOS NUMERIC, "
                    + " symptomsCough NUMERIC, "
                    + " symptomsSOB NUMERIC, "
                    + " FeelingFT NUMERIC ); ";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS tblPat";

    public AssignmentDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    public void onCreate(SQLiteDatabase db) {
        try {
            db.execSQL(SQL_CREATE_ENTRIES);
        }catch (SQLiteException e) {
        }
    }
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}