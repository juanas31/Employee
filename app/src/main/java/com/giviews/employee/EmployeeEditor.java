package com.giviews.employee;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.giviews.employee.data.EmployeeContract;
import com.hendraanggrian.bundler.BindExtra;
import com.hendraanggrian.bundler.Bundler;
import com.hendraanggrian.kota.content.Themes;
import com.hendraanggrian.reveallayout.Radius;
import com.hendraanggrian.reveallayout.RevealableLayout;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

@RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
public class EmployeeEditor extends AppCompatActivity implements View.OnClickListener, LoaderManager.LoaderCallbacks<Cursor>{

    private static final int EXISTING_EMPLOYEE_LOADER = 0;

    private Uri mCurrentEmployeeUri;

    private EditText mFirstNameEditText;
    private EditText mLastNameEditText;
    private EditText mTitleEditText;
    private EditText mDepartment;
    private EditText mCity;
    private EditText mPhone;
    private EditText mEmail;

    private Spinner mGenderSpinner;
    private int mGender = EmployeeContract.EmployeeEntry.GENDER_UNKNOWN;

    private ImageView profileImageView;
    private Button pickImage;

    private static final int SELECT_PHOTO = 1;
    private static final int CAPTURE_PHOTO = 2;

    //TODO
    public static final int MEDIA_TYPE_IMAGE = 1;

    private static  final String IMAGE_DIRECTORY_NAME = "KAMERA";

    private Uri fileUri;

    private ProgressDialog progressBar;
    private int progressBarStatus = 0;
    private Handler progressBarHandler = new Handler();
    private boolean hasImageChanges = false;
    Bitmap thumbnail;

    private boolean mEmployeeHasChanged = false;

    View rootLayout;

    public static final  String EXTRA_RECT = "com.giviews.employee";
    @BindExtra(EXTRA_RECT)
    Rect rect;

    @BindView(R.id.revealLayout)
    RevealableLayout revealLayout;

    @BindView(R.id.root_layout)
    ViewGroup layout;

    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mEmployeeHasChanged = true;
            return false;
        }
    };

    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_setting);
        setSupportActionBar(toolbar);
        toolbar.setBackgroundColor(Constant.color);

        Bundler.bindExtras(this);
        ButterKnife.bind(this);

        //TODO
        Intent intent = getIntent();
        mCurrentEmployeeUri = intent.getData();

        if (mCurrentEmployeeUri == null) {
            setTitle("Add an Employee");
            invalidateOptionsMenu();
        }else {
            setTitle("Edit Employee");

            getLoaderManager().initLoader(EXISTING_EMPLOYEE_LOADER, null, this);
        }

        mFirstNameEditText = (EditText) findViewById(R.id.firstName);
        mLastNameEditText = (EditText) findViewById(R.id.lastName);
        mTitleEditText = (EditText) findViewById(R.id.title);
        mDepartment = (EditText) findViewById(R.id.department);
        mCity = (EditText) findViewById(R.id.city);
        mPhone = (EditText) findViewById(R.id.phone);
        mEmail = (EditText) findViewById(R.id.email);
        mGenderSpinner = (Spinner) findViewById(R.id.spinner_gender);

        mFirstNameEditText.setOnTouchListener(mTouchListener);
        mLastNameEditText.setOnTouchListener(mTouchListener);
        mTitleEditText.setOnTouchListener(mTouchListener);
        mDepartment.setOnTouchListener(mTouchListener);
        mCity.setOnTouchListener(mTouchListener);
        mPhone.setOnTouchListener(mTouchListener);
        mEmail.setOnTouchListener(mTouchListener);
        mGenderSpinner.setOnTouchListener(mTouchListener);

        setupSpinner();

        rootLayout = findViewById(R.id.root_layout);

        layout.post(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
            @Override
            public void run() {
                Animator animator = revealLayout.reveal(layout, rect.centerX(), rect.centerY(), Radius.GONE_ACTIVITY);
                animator.setDuration(1000);
                animator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        if (Build.VERSION.SDK_INT >= 21) {
                            getWindow().setStatusBarColor(Themes.getColor(getTheme(), R.attr.colorAccent, true));
                        }
                    }
                });
                animator.start();
            }
        });

        //TODO
        profileImageView = (ImageView) findViewById(R.id.profileImageView);
        pickImage = (Button) findViewById(R.id.pickImage);

        pickImage.setOnClickListener(this);

        if (ContextCompat.checkSelfPermission(EmployeeEditor.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            profileImageView.setEnabled(false);
            ActivityCompat.requestPermissions(EmployeeEditor.this, new String[]{ Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
        }else {
            profileImageView.setEnabled(true);
        }
    }

    private void setupSpinner() {

        ArrayAdapter genderSpinnerAdapter = ArrayAdapter.createFromResource(this,R.array.array_gender_options, android.R.layout.simple_spinner_item);

        genderSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        mGenderSpinner.setAdapter(genderSpinnerAdapter);

        mGenderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)){
                    if (selection.equals(getString(R.string.gender_male))){
                        mGender = EmployeeContract.EmployeeEntry.GENDER_MALE;
                    }else if (selection.equals(getString(R.string.gender_female))) {
                        mGender = EmployeeContract.EmployeeEntry.GENDER_FEMALE;
                    }else {
                        mGender = EmployeeContract.EmployeeEntry.GENDER_UNKNOWN;
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                mGender = EmployeeContract.EmployeeEntry.GENDER_UNKNOWN;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_editor, menu);

        if (mCurrentEmployeeUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                saveEmployee();
                finish();
                return true;
            case R.id.action_delete:
                showDeleteConfirmationDialog();
                return true;
            case android.R.id.home:
                if (!mEmployeeHasChanged) {
                    NavUtils.navigateUpFromSameTask(EmployeeEditor.this);
                    return true;
                }

                DialogInterface.OnClickListener discardButtonClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        NavUtils.navigateUpFromSameTask(EmployeeEditor.this);
                    }
                };

                showUnSavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void saveEmployee() {
        String firstnameString = mFirstNameEditText.getText().toString().trim();
        String lastnameString = mLastNameEditText.getText().toString().trim();
        String titleString = mTitleEditText.getText().toString().trim();
        String departmentString = mDepartment.getText().toString().trim();
        String cityString = mCity.getText().toString().trim();
        String phoneString = mPhone.getText().toString().trim();
        String emailString = mEmail.getText().toString().trim();

        profileImageView.setDrawingCacheEnabled(true);
        profileImageView.buildDrawingCache();
        Bitmap bitmap = profileImageView.getDrawingCache();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        if (mCurrentEmployeeUri == null &&
                TextUtils.isEmpty(firstnameString) && TextUtils.isEmpty(lastnameString) &&
                TextUtils.isEmpty(titleString) && TextUtils.isEmpty(departmentString) &&
                TextUtils.isEmpty(cityString) && TextUtils.isEmpty(phoneString) &&
                TextUtils.isEmpty(emailString) && mGender == EmployeeContract.EmployeeEntry.GENDER_UNKNOWN) {
            return;
        }

        ContentValues values = new ContentValues();
        values.put(EmployeeContract.EmployeeEntry.COLUMN_FIRSTNAME, firstnameString);
        values.put(EmployeeContract.EmployeeEntry.COLUMN_LASTNAME, lastnameString);
        values.put(EmployeeContract.EmployeeEntry.COLUMN_TITLE, titleString);
        values.put(EmployeeContract.EmployeeEntry.COLUMN_DEPARTMENT, departmentString);
        values.put(EmployeeContract.EmployeeEntry.COLUMN_CITY, cityString);
        values.put(EmployeeContract.EmployeeEntry.COLUMN_PHONE, phoneString);
        values.put(EmployeeContract.EmployeeEntry.COLUMN_IMAGE, data);
        values.put(EmployeeContract.EmployeeEntry.COLUMN_EMAIL, emailString);

        values.put(EmployeeContract.EmployeeEntry.COLUMN_EMPLOYEE_GENDER, mGender);

        if (mCurrentEmployeeUri == null) {
            Uri newUri = getContentResolver().insert(EmployeeContract.EmployeeEntry.CONTENT_URI, values);

            if (newUri == null) {
                Toast.makeText(this, "Error with saving Employee", Toast.LENGTH_SHORT).show();
            }else {
                Toast.makeText(this, "Employee saved", Toast.LENGTH_SHORT).show();
            }
        }else {
            int rowAffected = getContentResolver().update(mCurrentEmployeeUri, values, null, null);

            if (rowAffected == 0) {
                Toast.makeText(getApplicationContext(), "Error with updating employee", Toast.LENGTH_SHORT).show();
            }else {
                Toast.makeText(getApplicationContext(), "Employee updated", Toast.LENGTH_SHORT).show();
            }
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
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
            int imageColumnIndex = cursor.getColumnIndex(EmployeeContract.EmployeeEntry.COLUMN_IMAGE);
            int emailColumnIndex = cursor.getColumnIndex(EmployeeContract.EmployeeEntry.COLUMN_EMAIL);
            int genderColumnIndex = cursor.getColumnIndex(EmployeeContract.EmployeeEntry.COLUMN_EMPLOYEE_GENDER);

            String firstName = cursor.getString(firstnameColumnIndex);
            String lastName = cursor.getString(lastnameColumnIndex);
            String title = cursor.getString(titleColumnIndex);
            String department = cursor.getString(departmentColumnIndex);
            String city = cursor.getString(cityColumnIndex);
            String phone = cursor.getString(phoneColumnIndex);
            byte[] image = cursor.getBlob(imageColumnIndex);
            String email = cursor.getString(emailColumnIndex);
            int gender = cursor.getInt(genderColumnIndex);

            mFirstNameEditText.setText(firstName);
            mLastNameEditText.setText(lastName);
            mTitleEditText.setText(title);
            mDepartment.setText(department);
            mCity.setText(city);
            mPhone.setText(phone);
            Bitmap bmp = BitmapFactory.decodeByteArray(image, 0, image.length);
            profileImageView.setImageBitmap(Bitmap.createScaledBitmap(bmp, 200, 200, false));
            mEmail.setText(email);

            switch (gender) {
                case EmployeeContract.EmployeeEntry.GENDER_MALE:
                    mGenderSpinner.setSelection(1);
                    break;
                case EmployeeContract.EmployeeEntry.GENDER_FEMALE:
                    mGenderSpinner.setSelection(2);
                    break;
                default:
                    mGenderSpinner.setSelection(0);
                    break;
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

        mFirstNameEditText.setText("");
        mLastNameEditText.setText("");
        mTitleEditText.setText("");
        mDepartment.setText("");
        mCity.setText("");
        mPhone.setText("");
        mEmail.setText("");
        mGenderSpinner.setSelection(0);

    }

    private void showUnSavedChangesDialog(DialogInterface.OnClickListener discardButtonClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Discard your changes and quit editing?");
        builder.setPositiveButton("Discard", discardButtonClickListener);
        builder.setNegativeButton("Keep Editing", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Delete this Employee?");
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                deleteEmployee();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null){
                    dialog.dismiss();
                }
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void deleteEmployee() {
        if (mCurrentEmployeeUri != null){
            int rowsDeleted = getContentResolver().delete(mCurrentEmployeeUri,null, null);
            if (rowsDeleted == 0) {
                Toast.makeText(this, "Error with deleteing employee", Toast.LENGTH_SHORT).show();
            }else {
                Toast.makeText(this, "Employee deleted", Toast.LENGTH_SHORT).show();
            }
        }

        finish();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.pickImage:
                new MaterialDialog.Builder(this)
                        .title("set your image")
                        .items(R.array.upload_images)
                        .itemsIds(R.array.itemIds)
                        .itemsCallback(new MaterialDialog.ListCallback() {
                            @Override
                            public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                                switch (which) {
                                    case 0:
                                        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                                        photoPickerIntent.setType("image/*");
                                        startActivityForResult(photoPickerIntent, SELECT_PHOTO);
                                        break;
                                    case 1:
                                        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                                        fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);

                                        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);

                                        startActivityForResult(intent, CAPTURE_PHOTO);
                                        break;
                                    case 2:
                                        profileImageView.setImageResource(R.drawable.ic_account_circle_black);
                                        break;
                                }
                            }
                        })
                        .show();
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 0){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                profileImageView.setEnabled(true);
            }
        }
    }

    public void setProgressBar(){
        progressBar = new ProgressDialog(this);
        progressBar.setCancelable(true);
        progressBar.setMessage("Please wait....");
        progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressBar.setProgress(0);
        progressBar.setMax(100);
        progressBar.show();
        progressBarStatus = 0;
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (progressBarStatus < 100) {
                    progressBarStatus += 30;

                    try {
                        Thread.sleep(1000);
                    }catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    progressBarHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            progressBar.setProgress(progressBarStatus);
                        }
                    });
                }
                if (progressBarStatus >= 100){
                    try {
                        Thread.sleep(2000);
                    }catch (InterruptedException e){
                        e.printStackTrace();
                    }
                    progressBar.dismiss();
                }
            }
        }).start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SELECT_PHOTO) {
            if (resultCode == RESULT_OK) {
                try {
                    final Uri imageUri = data.getData();
                    final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                    final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                    setProgressBar();
                    profileImageView.setImageBitmap(selectedImage);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

            }else if (requestCode == CAPTURE_PHOTO){
                if (resultCode == RESULT_OK) {
                    onCaptureImageResult();
                }
            }
        }
    }

    private void onCaptureImageResult() {
        try {
//        thumbnail = (Bitmap) data.getExtras().get("data");
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 8;
            thumbnail = BitmapFactory.decodeFile(fileUri.getPath(), options);
            setProgressBar();
//            profileImageView.setMaxWidth(200);
            profileImageView.setImageBitmap(thumbnail);
        }catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    public  Uri getOutputMediaFileUri(int type) {
        return Uri.fromFile(getOutputMediaFile(type));
    }

    private File getOutputMediaFile(int type) {
        File mediaStorageDir = new File(
                Environment
                        .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                IMAGE_DIRECTORY_NAME);

        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d(IMAGE_DIRECTORY_NAME, "Ooops! Failed create"
                        + IMAGE_DIRECTORY_NAME + " directory");
                return null;
            }
        }

        String timeStamp;
        timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmsss",
                Locale.getDefault()).format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator
                    + "IMG_" + timeStamp + ".jpg");
        }else {
            return null;
        }
        return mediaFile;
    }
}
