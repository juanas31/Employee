package com.giviews.employee.data;

import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by asus on 22/10/2017.
 */

public final class EmployeeDbHelper extends SQLiteOpenHelper{

    public static final String LOG_TAG = EmployeeDbHelper.class.getSimpleName();

    public  static final String DATABASE_NAME = "employee.db";

    private static final int DATABASE_VERSION = 1;

    public EmployeeDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //Create String to contains the SQL table
        String SQL_CREATE_EMPLOYEES_TABLE = "CREATE TABLE " + EmployeeContract.EmployeeEntry.TABLE_NAME + "("
                + EmployeeContract.EmployeeEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + EmployeeContract.EmployeeEntry.COLUMN_FIRSTNAME + " TEXT NOT NULL, "
                + EmployeeContract.EmployeeEntry.COLUMN_LASTNAME + " TEXT NOT NULL, "
                + EmployeeContract.EmployeeEntry.COLUMN_TITLE + " TEXT NOT NULL, "
                + EmployeeContract.EmployeeEntry.COLUMN_DEPARTMENT + " TEXT NOT NULL, "
                + EmployeeContract.EmployeeEntry.COLUMN_CITY + " TEXT NOT NULL, "
                + EmployeeContract.EmployeeEntry.COLUMN_PHONE + " TEXT NOT NULL, "
                + EmployeeContract.EmployeeEntry.COLUMN_IMAGE + " BLOB NOT NULL, "
                + EmployeeContract.EmployeeEntry.COLUMN_EMAIL + " TEXT NOT NULL, "
                + EmployeeContract.EmployeeEntry.COLUMN_EMPLOYEE_GENDER + " INTEGER NOT NULL" + " );";

        //EXECUTE THE SQL STATEMENT
        db.execSQL(SQL_CREATE_EMPLOYEES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        //on upgrade
    }
}
