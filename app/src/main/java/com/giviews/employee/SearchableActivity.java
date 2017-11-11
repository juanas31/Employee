package com.giviews.employee;

import android.app.ListActivity;
import android.app.SearchManager;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import com.giviews.employee.data.EmployeeContract;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by asus on 29/10/2017.
 */

public class SearchableActivity extends ListActivity implements SearchView.OnQueryTextListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        checkIntent(intent);
    }

    @Override
    protected void onNewIntent(Intent newIntent) {
        setIntent(newIntent);

        checkIntent(newIntent);
    }

    private void checkIntent(Intent intent) {
        String query = "";
        String intentAction = intent.getAction();
        if (Intent.ACTION_SEARCH.equals(intentAction)) {
            query = intent.getStringExtra(SearchManager.QUERY);
        } else if (Intent.ACTION_VIEW.equals(intentAction)) {
            Uri details = intent.getData();
            Intent detailsIntent = new Intent(Intent.ACTION_VIEW, details);
            startActivity(detailsIntent);
        }
        fillList(query);
    }

    private void fillList(String query) {
        String wildcardQuery = "%" + query + "%";

        Cursor cursor = getContentResolver().query(
                EmployeeContract.EmployeeEntry.CONTENT_URI,
                null,
                EmployeeContract.EmployeeEntry.COLUMN_FIRSTNAME + " LIKE ? OR " + EmployeeContract.EmployeeEntry.COLUMN_LASTNAME + " LIKE ? OR ",
                new String[]{ wildcardQuery, wildcardQuery },
                null);

        if (cursor.getCount() == 0) {
            Toast.makeText(this, " NO SEARCH RESULT ", Toast.LENGTH_SHORT).show();
            int timeout = 3000;

            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    finish();
                    Intent intent = new Intent(SearchableActivity.this, EmployeeActivity.class);
                    startActivity(intent);
                }
            }, timeout);

        }

        ListAdapter adapter = new SimpleCursorAdapter(
                this,
                android.R.layout.simple_list_item_2,
                cursor,
                new String[]{ EmployeeContract.EmployeeEntry.COLUMN_FIRSTNAME, EmployeeContract.EmployeeEntry.COLUMN_LASTNAME },
                new int[]{android.R.id.text1, android.R.id.text2},
                0);
        setListAdapter(adapter);
    }

    @Override
    protected void onListItemClick(ListView l, View view, int position, long id) {
        Intent intent = new Intent(SearchableActivity.this, EmployeeActivity.class);

        Uri details = Uri.withAppendedPath(EmployeeContract.EmployeeEntry.CONTENT_URI, "" + id);
        intent.setData(details);
        startActivity(intent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {

            Intent parentActivityIntent = new Intent(this, EmployeeActivity.class);
            parentActivityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(parentActivityIntent);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return false;
    }
}
