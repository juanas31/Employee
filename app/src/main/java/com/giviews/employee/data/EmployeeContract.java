package com.giviews.employee.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by asus on 22/10/2017.
 */

public final class EmployeeContract {

    private EmployeeContract() {}

    public static final String CONTENT_ATHORITY = "com.giviews.employee";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_ATHORITY);

    public static final String PATH_EMPLOYEES = "employees-path";

    public static final class EmployeeEntry implements BaseColumns {

        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_EMPLOYEES);

        public static final String CONTENT_LIST_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_ATHORITY + "/" + PATH_EMPLOYEES;

        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_ATHORITY + "/" + PATH_EMPLOYEES;

        public final static String TABLE_NAME = "employees";

        public final static String _ID = BaseColumns._ID;

        public final static String COLUMN_FIRSTNAME = "firstname";
        public final static String COLUMN_LASTNAME = "lastname";
        public final static String COLUMN_TITLE = "title";
        public final static String COLUMN_DEPARTMENT = "department";
        public final static String COLUMN_CITY = "city";
        public final static String COLUMN_PHONE = "phone";
        public final static String COLUMN_IMAGE = "image";
        public final static String COLUMN_EMAIL = "email";

        public final static String COLUMN_EMPLOYEE_GENDER = "gender";

        public static final int GENDER_UNKNOWN = 0;
        public static final int GENDER_MALE = 1;
        public static final int GENDER_FEMALE = 2;

        public static boolean isValidGender(int gender) {
            if (gender == GENDER_UNKNOWN || gender == GENDER_MALE || gender == GENDER_FEMALE) {
                return true;
            }
            return false;
        }
    }
}
