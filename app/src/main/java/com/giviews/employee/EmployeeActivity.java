package com.giviews.employee;

import android.annotation.TargetApi;
import android.app.LoaderManager;
import android.app.SearchManager;
import android.content.ContentUris;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;


import com.giviews.employee.data.EmployeeContract;
import com.giviews.employee.data.EmployeeDbHelper;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class EmployeeActivity extends AppCompatActivity implements SearchView.OnQueryTextListener,
        LoaderManager.LoaderCallbacks<Cursor>{
//    EmployeeDbHelper employeeDbHelper;

    private static final int EMPLOYEE_LOADER = 0;

    EmployeeCursorAdapter mCursorAdapter;

    @BindView(R.id.fab) FloatingActionButton button;

    public EmployeeActivity(){

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employee);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_setting);
        setSupportActionBar(toolbar);
        toolbar.setBackgroundColor(Constant.color);

        //Setup FAB
        ButterKnife.bind(this);
        findViewById(R.id.fab).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), EmployeeEditor.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                intent.putExtra(EmployeeEditor.EXTRA_RECT, createRect(button));
                startActivity(intent);
            }
        });

        ListView employeeListView = (ListView) findViewById(R.id.list);

        View emptyView = findViewById(R.id.empty_view);
        employeeListView.setEmptyView(emptyView);

        mCursorAdapter = new EmployeeCursorAdapter(this, null);
        employeeListView.setAdapter(mCursorAdapter);

        //TODO
        employeeListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Intent intent = new Intent(getApplicationContext(), EmployeeDetailsActivity.class);
                Uri currentEmployeeUri = ContentUris.withAppendedId(EmployeeContract.EmployeeEntry.CONTENT_URI, id);
                intent.setData(currentEmployeeUri);
                intent.putExtra(EmployeeEditor.EXTRA_RECT, createRect(button));
                startActivity(intent);
            }
        });

        employeeListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {

                Intent intent = new Intent(EmployeeActivity.this, EmployeeEditor.class);
                Uri currentEmployeeUri = ContentUris.withAppendedId(EmployeeContract.EmployeeEntry.CONTENT_URI, id);
                intent.setData(currentEmployeeUri);
                intent.putExtra(EmployeeEditor.EXTRA_RECT, createRect(button));
                startActivity(intent);
                return true;
            }
        });

        getLoaderManager().initLoader(EMPLOYEE_LOADER, null, this);

//        employeeDbHelper = new EmployeeDbHelper(this);
    }

    private Rect createRect(View view) {
        Rect rect = new Rect();
        view.getDrawingRect(rect);
        ((ViewGroup) view.getParent()).offsetDescendantRectToMyCoords(view, rect);
        return rect;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_catalog, menu);

        MenuItem search = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(search);
        searchView.setOnQueryTextListener(this);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case R.id.action_search:
                onSearchRequested();
                return true;
            case R.id.setting:
                startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
                return true;
            default:
                return false;
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
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
        };

        return new CursorLoader(this,
                EmployeeContract.EmployeeEntry.CONTENT_URI,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        mCursorAdapter.swapCursor(cursor);
       }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCursorAdapter.swapCursor(null);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        newText = newText.toLowerCase();
        ArrayList<Cursor> newList = new ArrayList<>();
        return true;
    }
}
