package com.giviews.employee;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.giviews.employee.data.EmployeeContract;

/**
 * Created by asus on 27/10/2017.
 */

class EmployeeCursorAdapter extends CursorAdapter{

    public EmployeeCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView firstnameTextView = (TextView) view.findViewById(R.id.firstname);
        TextView lastnameTextView = (TextView) view.findViewById(R.id.lastname);
        TextView titleTextView = (TextView) view.findViewById(R.id.titleview);

        int firstnameColumnIndex = cursor.getColumnIndex(EmployeeContract.EmployeeEntry.COLUMN_FIRSTNAME);
        int lastnameColumnIndex = cursor.getColumnIndex(EmployeeContract.EmployeeEntry.COLUMN_LASTNAME);
        int titleColumnIndex = cursor.getColumnIndex(EmployeeContract.EmployeeEntry.COLUMN_TITLE);

        String employeeFirstName = cursor.getString(firstnameColumnIndex);
        String employeeLastName = cursor.getString(lastnameColumnIndex);
        String employeeTitle = cursor.getString(titleColumnIndex);

        if (TextUtils.isEmpty(employeeFirstName)) {
            employeeFirstName = "Unknown";
        }

        if (TextUtils.isEmpty(employeeLastName)) {
            employeeLastName = "Unknown";
        }

        if (TextUtils.isEmpty(employeeTitle)) {
            employeeTitle = "Unknown";
        }

        firstnameTextView.setText(employeeFirstName);
        lastnameTextView.setText(employeeLastName);
        titleTextView.setText(employeeTitle);
    }
}
