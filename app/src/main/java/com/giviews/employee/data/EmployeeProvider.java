package com.giviews.employee.data;

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

import java.util.HashMap;

/**
 * Created by asus on 24/10/2017.
 */

public class EmployeeProvider extends ContentProvider {

    public static final String LOG_TAG = EmployeeProvider.class.getSimpleName();

    private static final int EMPLOYEES = 100;

    private static final int EMPLOYEE_ID = 101;

    private static final int SEARCH_SUGGEST = 102;

    private static final HashMap<String, String> SEARCH_SUGGEST_PROJECTION_MAP;
    static {
        SEARCH_SUGGEST_PROJECTION_MAP = new HashMap<String, String>();
        SEARCH_SUGGEST_PROJECTION_MAP.put(EmployeeContract.EmployeeEntry._ID, EmployeeContract.EmployeeEntry._ID);
        SEARCH_SUGGEST_PROJECTION_MAP.put(SearchManager.SUGGEST_COLUMN_TEXT_1, EmployeeContract.EmployeeEntry.COLUMN_FIRSTNAME + " AS " + SearchManager.SUGGEST_COLUMN_TEXT_1);
        SEARCH_SUGGEST_PROJECTION_MAP.put(SearchManager.SUGGEST_COLUMN_TEXT_2, EmployeeContract.EmployeeEntry.COLUMN_LASTNAME + " AS " + SearchManager.SUGGEST_COLUMN_TEXT_2);
        SEARCH_SUGGEST_PROJECTION_MAP.put(SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID, EmployeeContract.EmployeeEntry._ID + " AS " + SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID);
    }

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    public static final Uri SEARCH_SUGGEST_URI = Uri.parse("content://" + EmployeeContract.CONTENT_ATHORITY + "/" + EmployeeContract.PATH_EMPLOYEES + "/" + SearchManager.SUGGEST_URI_PATH_QUERY);

    static {
        sUriMatcher.addURI(EmployeeContract.CONTENT_ATHORITY, EmployeeContract.PATH_EMPLOYEES, EMPLOYEES);

        sUriMatcher.addURI(EmployeeContract.CONTENT_ATHORITY, EmployeeContract.PATH_EMPLOYEES + "/#", EMPLOYEE_ID);
    }

    public EmployeeProvider(){
        sUriMatcher.addURI(EmployeeContract.CONTENT_ATHORITY, SearchManager.SUGGEST_URI_PATH_QUERY, SEARCH_SUGGEST);
        sUriMatcher.addURI(EmployeeContract.CONTENT_ATHORITY, SearchManager.SUGGEST_URI_PATH_QUERY + "/*", SEARCH_SUGGEST);
    }

    private EmployeeDbHelper mDbHelper;

    @Override
    public boolean onCreate() {
        mDbHelper = new EmployeeDbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {

        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        Cursor cursor;

        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        queryBuilder.setTables(EmployeeContract.EmployeeEntry.TABLE_NAME);

        int match = sUriMatcher.match(uri);
        switch (match) {
            case SEARCH_SUGGEST:
                selectionArgs = new String[] { "%" + selectionArgs[0] + "%", "%" + selectionArgs[0] + "%"};
                queryBuilder.setProjectionMap(SEARCH_SUGGEST_PROJECTION_MAP);

                cursor = queryBuilder.query(database, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case EMPLOYEES:
                cursor = database.query(EmployeeContract.EmployeeEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case EMPLOYEE_ID:
                selection = EmployeeContract.EmployeeEntry._ID + "=?";
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};

                cursor = database.query(EmployeeContract.EmployeeEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown uri" + uri);
        }
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case EMPLOYEES:
                return insertEmployee(uri, contentValues);
            default:
                throw new IllegalArgumentException("Inserted is not supported for " + uri);
        }
    }

    private Uri insertEmployee(Uri uri, ContentValues values) {
        String firstname = values.getAsString(EmployeeContract.EmployeeEntry.COLUMN_FIRSTNAME);
        if (firstname == null) {
            throw new IllegalArgumentException("Employee require a firstname");
        }

        String lastname = values.getAsString(EmployeeContract.EmployeeEntry.COLUMN_LASTNAME);
        if (lastname == null) {
            throw new IllegalArgumentException("Employee requires a lastname");
        }

        String department = values.getAsString(EmployeeContract.EmployeeEntry.COLUMN_DEPARTMENT);
        if (department == null) {
            throw new IllegalArgumentException("Employee requires a lastname");
        }

        String title = values.getAsString(EmployeeContract.EmployeeEntry.COLUMN_TITLE);
        if (title == null) {
            throw new IllegalArgumentException("Employee requires a title");
        }

        String city = values.getAsString(EmployeeContract.EmployeeEntry.COLUMN_CITY);
        if (city == null) {
            throw  new IllegalArgumentException("Employee requires a city");
        }

        String phone = values.getAsString(EmployeeContract.EmployeeEntry.COLUMN_PHONE);
        if (phone == null) {
            throw new IllegalArgumentException("Employee requires a phone number");
        }

        String email = values.getAsString(EmployeeContract.EmployeeEntry.COLUMN_EMAIL);
        if (email == null) {
            throw new IllegalArgumentException("Employee requires an email");
        }

        //check gender is valid
        Integer gender = values.getAsInteger(EmployeeContract.EmployeeEntry.COLUMN_EMPLOYEE_GENDER);
        if (gender == null || !EmployeeContract.EmployeeEntry.isValidGender(gender)) {
            throw new IllegalArgumentException("Employee requires valid gender");
        }

        //get writable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        //Insert new Employee with given values
        Long id = database.insert(EmployeeContract.EmployeeEntry.TABLE_NAME, null, values);
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for" + uri);
            return null;
        }

        getContext().getContentResolver().notifyChange(uri, null);

        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case EMPLOYEES:
                return updateEmployee(uri, contentValues, selection, selectionArgs);
            case EMPLOYEE_ID:
                selection = EmployeeContract.EmployeeEntry._ID + "=?";
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};
                return updateEmployee(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    private int updateEmployee(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        if (values.containsKey(EmployeeContract.EmployeeEntry.COLUMN_FIRSTNAME)){
            String firstname = values.getAsString(EmployeeContract.EmployeeEntry.COLUMN_FIRSTNAME);
            if (firstname == null) {
                throw new IllegalArgumentException("Employee requires a firstname");
            }
        }

        if (values.containsKey(EmployeeContract.EmployeeEntry.COLUMN_LASTNAME)){
            String lastname = values.getAsString(EmployeeContract.EmployeeEntry.COLUMN_LASTNAME);
            if (lastname == null) {
                throw new IllegalArgumentException("Employess requires a lastname");
            }
        }

        if (values.containsKey(EmployeeContract.EmployeeEntry.COLUMN_TITLE)){
            String title = values.getAsString(EmployeeContract.EmployeeEntry.COLUMN_TITLE);
            if (title == null) {
                throw new IllegalArgumentException("Employee requires a title");
            }
        }

        if (values.containsKey(EmployeeContract.EmployeeEntry.COLUMN_DEPARTMENT)) {
            String department = values.getAsString(EmployeeContract.EmployeeEntry.COLUMN_DEPARTMENT);
            if (department == null) {
                throw new IllegalArgumentException("Employee requires a department");
            }
        }

        if (values.containsKey(EmployeeContract.EmployeeEntry.COLUMN_CITY)) {
            String city = values.getAsString(EmployeeContract.EmployeeEntry.COLUMN_CITY);
            if (city == null) {
                throw new IllegalArgumentException("Employee requires a city");
            }
        }

        if (values.containsKey(EmployeeContract.EmployeeEntry.COLUMN_PHONE)) {
            String phone = values.getAsString(EmployeeContract.EmployeeEntry.COLUMN_PHONE);
            if (phone == null) {
                throw new IllegalArgumentException("Employee requires a phone number");
            }
        }

        if (values.containsKey(EmployeeContract.EmployeeEntry.COLUMN_EMAIL)) {
            String email = values.getAsString(EmployeeContract.EmployeeEntry.COLUMN_EMAIL);
            if (email == null) {
                throw new IllegalArgumentException("Employee require an email");
            }
        }

        if (values.containsKey(EmployeeContract.EmployeeEntry.COLUMN_EMPLOYEE_GENDER)) {
            Integer gender = values.getAsInteger(EmployeeContract.EmployeeEntry.COLUMN_EMPLOYEE_GENDER);
            if (gender == null || !EmployeeContract.EmployeeEntry.isValidGender(gender)) {
                throw new IllegalArgumentException("Employee require a valid gender");
            }
        }

        if (values.size() == 0) {
            return 0;
        }

        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        int rowsUpdated = database.update(EmployeeContract.EmployeeEntry.TABLE_NAME, values, selection, selectionArgs);

        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsUpdated;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        int rowsDeleted;

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case EMPLOYEES:
                //delete all rows
                rowsDeleted = database.delete(EmployeeContract.EmployeeEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case EMPLOYEE_ID:
                selection = EmployeeContract.EmployeeEntry._ID + "=?";
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = database.delete(EmployeeContract.EmployeeEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not support for " + uri);
        }

        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsDeleted;
    }

    @Override
    public String getType(Uri uri) {
        final  int match = sUriMatcher.match(uri);
        switch (match) {
            case EMPLOYEES:
                return EmployeeContract.EmployeeEntry.CONTENT_LIST_TYPE;
            case EMPLOYEE_ID:
                return EmployeeContract.EmployeeEntry.CONTENT_ITEM_TYPE;
            case SEARCH_SUGGEST:
                return null;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri + " with match " + match);
        }
    }
}
