package com.giviews.employee;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.giviews.employee.data.EmployeeContract;

/**
 * Created by asus on 28/10/2017.
 */

public class EmployeeDetailsActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{
    private Uri mCurrentEmployeeUri;

    private TextView mFirstname;
    private TextView mLastname;
    private TextView mDepartment;
    private TextView mTitle;
    private TextView mCity;
    private TextView mPhone;
    private TextView mEmail;
    private TextView mGender;
    private ImageView profileImageView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employee_details);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_setting);
        setSupportActionBar(toolbar);
        toolbar.setBackgroundColor(Constant.color);

        Intent intent = getIntent();
        mCurrentEmployeeUri = intent.getData();

        if (mCurrentEmployeeUri == null) {
            Toast.makeText(this, "No Employee Data ", Toast.LENGTH_SHORT).show();
        }else {
            getLoaderManager().initLoader(0, null, this);
        }

        mFirstname = (TextView) findViewById(R.id.employee_firstname);
        mLastname = (TextView) findViewById(R.id.employee_lastname);
        mTitle = (TextView) findViewById(R.id.employee_title);
        mDepartment = (TextView) findViewById(R.id.employee_department);
        mCity = (TextView) findViewById(R.id.employee_city);
        mPhone = (TextView) findViewById(R.id.employee_phone);
        mEmail = (TextView) findViewById(R.id.employee_email);
        mGender = (TextView) findViewById(R.id.employee_gender);
        profileImageView = (ImageView) findViewById(R.id.profileImageView);
    }

    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {
                EmployeeContract.EmployeeEntry._ID,
                EmployeeContract.EmployeeEntry.COLUMN_FIRSTNAME,
                EmployeeContract.EmployeeEntry.COLUMN_LASTNAME,
                EmployeeContract.EmployeeEntry.COLUMN_TITLE,
                EmployeeContract.EmployeeEntry.COLUMN_DEPARTMENT,
                EmployeeContract.EmployeeEntry.COLUMN_CITY,
                EmployeeContract.EmployeeEntry.COLUMN_PHONE,
                EmployeeContract.EmployeeEntry.COLUMN_IMAGE,
                EmployeeContract.EmployeeEntry.COLUMN_EMAIL,
                EmployeeContract.EmployeeEntry.COLUMN_EMPLOYEE_GENDER,
        };

        return new CursorLoader(this,
                mCurrentEmployeeUri,
                projection,
                null,
                null,
                null);
    }
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        if (cursor.moveToFirst()) {
            int firstnameColumnIndex = cursor.getColumnIndex(EmployeeContract.EmployeeEntry.COLUMN_FIRSTNAME);
            int lastnameColumnIndex = cursor.getColumnIndex(EmployeeContract.EmployeeEntry.COLUMN_LASTNAME);
            int titleColumnIndex = cursor.getColumnIndex(EmployeeContract.EmployeeEntry.COLUMN_TITLE);
            int departmentColumnIndex = cursor.getColumnIndex(EmployeeContract.EmployeeEntry.COLUMN_DEPARTMENT);
            int cityColumnIndex = cursor.getColumnIndex(EmployeeContract.EmployeeEntry.COLUMN_CITY);
            int phoneColumnIndex = cursor.getColumnIndex(EmployeeContract.EmployeeEntry.COLUMN_PHONE);
            int imageCoumnIndex = cursor.getColumnIndex(EmployeeContract.EmployeeEntry.COLUMN_IMAGE);
            int emailColumnIndex = cursor.getColumnIndex(EmployeeContract.EmployeeEntry.COLUMN_EMAIL);
            int genderColumnIndex = cursor.getColumnIndex(EmployeeContract.EmployeeEntry.COLUMN_EMPLOYEE_GENDER);

            String firstName = cursor.getString(firstnameColumnIndex);
            String lastName = cursor.getString(lastnameColumnIndex);
            String title = cursor.getString(titleColumnIndex);
            String department = cursor.getString(departmentColumnIndex);
            String city = cursor.getString(cityColumnIndex);
            String phone = cursor.getString(phoneColumnIndex);
            byte[] image = cursor.getBlob(imageCoumnIndex);
            String email = cursor.getString(emailColumnIndex);
            int gender = cursor.getInt(genderColumnIndex);

            setTitle(firstName);
            mFirstname.setText(firstName);
            mLastname.setText(lastName);
            mTitle.setText(title);
            mDepartment.setText(department);
            mCity.setText(city);
            mPhone.setText(phone);
            Bitmap bmp = BitmapFactory.decodeByteArray(image, 0, image.length);
            profileImageView.setImageBitmap(Bitmap.createScaledBitmap(bmp, 200,
                    200, false));
            mEmail.setText(email);

            switch (gender) {
                case EmployeeContract.EmployeeEntry.GENDER_MALE:
                    mGender.setText("Male");
                    break;
                case EmployeeContract.EmployeeEntry.GENDER_FEMALE:
                    mGender.setText("Female");
                    break;
                default:
                    mGender.setText("Unknown");
                    break;
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
