package com.example.mystorage;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.documentfile.provider.DocumentFile;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "myLogs";
    SwipeMenuListView listView;

    final int REQUEST_CODE_ADD = 1, REQUEST_CODE_EDIT = 2, REQUEST_CODE_AUTH = 3, REQUEST_CODE_CHANGE_PIN = 4, REQUEST_CODE_EXPORT = 5, REQUEST_CODE_IMPORT = 6;
    boolean isFirstAuth = false;
    AppDatabase database;
    ResourceDataDao resourceDataDao;
    MyCipher myCipher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        myCipher = new MyCipher();
        myCipher.context = this;

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            verifyStoragePermissions(this);
        }

        String databaseName = "myTestDB";
        database = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, databaseName).allowMainThreadQueries().build();
        resourceDataDao = database.getResourceDataDao();

        //InitDebug();

        isFirstAuth = true;
        Intent intentAuthActivity = new Intent(this, LockScreenActivity.class);
        intentAuthActivity.putExtra("mode", REQUEST_CODE_AUTH);
        startActivityForResult(intentAuthActivity, REQUEST_CODE_AUTH);
    }

    @Override
    protected void onResume()
    {
        CreateSwipeMenuListView();
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.mainMenuAdd:
                Intent intentAddActivity = new Intent(this, AddActivity.class);
                startActivityForResult(intentAddActivity, REQUEST_CODE_ADD);
                return true;
            case R.id.mainMenuChangePin:
                Intent intentChangePinActivity = new Intent(this, LockScreenActivity.class);
                intentChangePinActivity.putExtra("mode", REQUEST_CODE_CHANGE_PIN);
                startActivityForResult(intentChangePinActivity, REQUEST_CODE_CHANGE_PIN);
                return true;
            case R.id.mainMenuExport:
                Intent intentExport = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                startActivityForResult(intentExport, REQUEST_CODE_EXPORT);
                return true;
            case R.id.mainMenuImport:
                Intent importIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                importIntent.addCategory(Intent.CATEGORY_OPENABLE);
                importIntent.setType("*/*");
                importIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivityForResult(importIntent, REQUEST_CODE_IMPORT);
                return true;
            case R.id.mainMenuClear:
                resourceDataDao.deleteAll();
                CreateSwipeMenuListView();
                return true;
            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // запишем в лог значения requestCode и resultCode
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("myLogs", "requestCode = " + requestCode + ", resultCode = " + resultCode);

        // если пришло ОК
        if (resultCode == RESULT_OK) {
            Bundle arguments = data.getExtras();
            ResourceData resourceData;
            switch (requestCode) {
                case REQUEST_CODE_ADD://обработка добавления новой записи
                    resourceData = (ResourceData)arguments.getSerializable(ResourceData.class.getSimpleName());
                    resourceDataDao.insert(myCipher.EncryptResourceData(resourceData));
                    Log.d("myLogs", "request add success");
                    break;
                case REQUEST_CODE_EDIT://обработка добавления новой записи
                    resourceData = (ResourceData)arguments.getSerializable(ResourceData.class.getSimpleName());
                    ResourceData resourceDataEncrypt = myCipher.EncryptResourceData(resourceData);
                    resourceDataEncrypt.setId(resourceData.getId());
                    resourceDataDao.update(resourceDataEncrypt);
                    Log.d("myLogs", "request edit success");
                    break;
                case REQUEST_CODE_AUTH:
                    if (MyCipher.getCode(this).equals(""))
                    {
                        MyCipher.saveToPref(this, arguments.getString("new_pin"));
                    }
                    isFirstAuth = false;
                    Log.d("myLogs", "request auth success");
                    break;
                case REQUEST_CODE_CHANGE_PIN:
                    ReEncyptAllDB(arguments.getString("new_pin"));
                    CreateSwipeMenuListView();
                    Toast.makeText(this, "PIN CHANGED", Toast.LENGTH_SHORT).show();
                    Log.d("myLogs", "request change success");
                    break;
                case REQUEST_CODE_EXPORT:
                    ExportDatabase(data.getData());
                    new AlertDialog.Builder(this)
                            .setTitle("Database Export")
                            .setMessage("Сurrent DB state has been successfully exported!")
                            .setPositiveButton("OK",null)
                            .show();
                    Log.d("myLogs", "export success");
                    break;
                case REQUEST_CODE_IMPORT:
                    final Uri uri = data.getData();
                    new AlertDialog.Builder(this)
                            .setTitle("Database Import")
                            .setMessage("Do you really want to import DB from file? Current DB state will be lost.")
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                                public void onClick(DialogInterface dialog, int whichButton) {
                                    ImportDatabase(uri);
                                    Log.d("myLogs", "import success");
                                    CreateSwipeMenuListView();
                                }})
                            .setNegativeButton(android.R.string.no, null).show();
                    break;
            }
            // если вернулось не ОК
        } else {
            if(isFirstAuth)
            {
                finish();
            }
            Log.d("myLogs", "wrong result");
        }
    };

    private void CreateSwipeMenuListView(){
        Log.d("myLogs", "curent code = " + MyCipher.getCode(this));
        List<ResourceDataMinimal> resourceDataMinimalList = ReadResourcesDataMinimalFromDB();
        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, resourceDataMinimalList);//*/
        listView = (SwipeMenuListView) findViewById(R.id.mainListView);
        listView.setAdapter(adapter);
        SwipeMenuCreator creator = new SwipeMenuCreator() {

            @Override
            public void create(SwipeMenu menu) {
                // create "delete" item
                SwipeMenuItem deleteItem = new SwipeMenuItem(
                        getApplicationContext());
                // set item background
                deleteItem.setBackground(new ColorDrawable(Color.rgb(0xF9,
                        0x3F, 0x25)));
                // set item width
                deleteItem.setWidth(340);
                // set a icon
                deleteItem.setIcon(R.drawable.ic_delete);
                // add to menu
                menu.addMenuItem(deleteItem);
            }
        };
        listView.setMenuCreator(creator);
        listView.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
                switch (index) {
                    case 0:
                        final ResourceDataMinimal resourceDataMinimal = new ResourceDataMinimal(ReadResourcesDataMinimalFromDB().get(position));
                        Log.d(TAG, "onMenuItemClick: clicked item to DELETE with id = " + resourceDataMinimal.id);
                        new AlertDialog.Builder(MainActivity.this)
                                .setTitle("Delete note")
                                .setMessage("Do you really want to delete this note for " + resourceDataMinimal.resource + "?")
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        resourceDataDao.deleteById(resourceDataMinimal.id);
                                        CreateSwipeMenuListView();
                                    }})
                                .setNegativeButton(android.R.string.no, null).show();
                        break;
                }
                // false : close the menu; true : not close the menu
                return false;
            }
        });
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "onItemClick: clicked item on position " + position);
                List<ResourceDataMinimal> resourceDataMinimalList1 = ReadResourcesDataMinimalFromDB();
                ResourceData resourceData = myCipher.DecryptResourceData(resourceDataDao.getResourceDataById(ReadResourcesDataMinimalFromDB().get(position).id));
                Intent itemExplorerActivityIntent = new Intent(view.getContext(), EditActivity.class);
                itemExplorerActivityIntent.putExtra(ResourceData.class.getSimpleName(), resourceData);
                startActivityForResult(itemExplorerActivityIntent, REQUEST_CODE_EDIT);//*/
            }
        });
    }

    void InitDebug() {
        database.clearAllTables();
        MyCipher.saveToPref(this, "1566");
        resourceDataDao.deleteAll();
        resourceDataDao.insert(myCipher.EncryptResourceData(new ResourceData("res0", "login0", "pswd0", "description0")));
        resourceDataDao.insert(myCipher.EncryptResourceData(new ResourceData("res1", "login1", "pswd1", "description1")));
        resourceDataDao.insert(myCipher.EncryptResourceData(new ResourceData("res2", "login2", "pswd2", "description2")));
        resourceDataDao.insert(myCipher.EncryptResourceData(new ResourceData("res3", "login3", "pswd3", "description3")));
        resourceDataDao.insert(myCipher.EncryptResourceData(new ResourceData("res4", "login4", "pswd4", "description4")));//*/
    }

    private List<ResourceDataMinimal> ReadResourcesDataMinimalFromDB(){
        /*ArrayAdapter<ResourceData> adapter = new ResourceDataAdapter(this);//*/
        //ArrayList<ResourceData> resourceDataArrayList = new ArrayList<>();
        //resourceDataArrayList = testResourceDataArrayList;
        List<ResourceDataMinimal> resourceDataMinimalList = resourceDataDao.getAllResourceDataMinimal();
        for(int i=0; i<resourceDataMinimalList.size(); i++) {
            try {
                resourceDataMinimalList.set(i, new ResourceDataMinimal(resourceDataMinimalList.get(i).id, myCipher.DecryptString(resourceDataMinimalList.get(i).resource)));
            } catch (Exception e) {
                e.printStackTrace();
                Log.d("myLogs", "DecryptResourceDataMinimal in list  CATCH");
            };
        };//*/
        return resourceDataMinimalList;
    };

    private void ReEncyptAllDB(String new_pin){
        try {
            List<ResourceData> resourceDataEncryptionList = resourceDataDao.getAllResourceData();
            for(int i=0; i<resourceDataEncryptionList.size(); i++) {
                resourceDataEncryptionList.set(i, myCipher.DecryptResourceData(resourceDataEncryptionList.get(i)));
            };
            MyCipher.saveToPref(this, new_pin);
            for(int i=0; i<resourceDataEncryptionList.size(); i++) {
                ResourceData resourceDataReEncrypt = new ResourceData(myCipher.EncryptResourceData(resourceDataEncryptionList.get(i)));
                resourceDataReEncrypt.setId(resourceDataEncryptionList.get(i).getId());
                resourceDataEncryptionList.set(i, resourceDataReEncrypt);
            };
            resourceDataDao.updateMany(resourceDataEncryptionList);
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("myLogs", "ReEncyptAllDB  CATCH");
        };
    }

    private void verifyStoragePermissions(Activity activity) {

        final int REQUEST_EXTERNAL_STORAGE = 1;
        String[] PERMISSIONS_STORAGE = {

                //Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };

        int permission = ActivityCompat.checkSelfPermission(
                activity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if(permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    private void ExportDatabase(Uri treeUri){
        DocumentFile pickedDir = DocumentFile.fromTreeUri(this, treeUri);
        SimpleDateFormat SDFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        Date date = Calendar.getInstance().getTime();
        String currentTime = SDFormat.format(date);
        Log.d(TAG, "export time = " + currentTime);
        DocumentFile newFile = pickedDir.createFile("text/csv", "DB_" + currentTime);
        try {
            File tempFile;
            tempFile = File.createTempFile("tempfile", ".csv", this.getCacheDir());
            CSVWriter csvWrite = new CSVWriter(new FileWriter(tempFile));
            List<ResourceData> resourceDataList = resourceDataDao.getAllResourceData();
            for(int i=0; i<resourceDataList.size(); i++){
                String[] mySecondStringArray ={
                        resourceDataList.get(i).getResource(),
                        resourceDataList.get(i).getLogin(),
                        resourceDataList.get(i).getPassword(),
                        resourceDataList.get(i).getDescription()
                };
                csvWrite.writeNext(mySecondStringArray);
            }
            csvWrite.close();
            int buffersize = 8 * 1024;
            byte[] buffer = new byte[buffersize];
            int bytes_read = buffersize;
            OutputStream savedb = getContentResolver().openOutputStream(newFile.getUri());
            InputStream indb = new FileInputStream(tempFile);
            while ((bytes_read = indb.read(buffer,0,buffersize)) > 0) {
                savedb.write(buffer,0,bytes_read);
            }
            savedb.flush();
            indb.close();
            savedb.close();
            Toast.makeText(this, "Succesfully", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "export GOOD");
        } catch (IOException e) {
            Log.d(TAG, "export CATCH");
        }
    }

    private void ImportDatabase(Uri treeUri){
        Log.d(TAG, "import");
        try {
            int buffersize = 8 * 1024;
            byte[] buffer = new byte[buffersize];
            int bytes_read = buffersize;
            File tempFile;
            tempFile = File.createTempFile("tempfile", ".csv", this.getCacheDir());
            InputStream indb = getContentResolver().openInputStream(treeUri);
            OutputStream savedb = new FileOutputStream(tempFile);
            while ((bytes_read = indb.read(buffer,0,buffersize)) > 0) {
                savedb.write(buffer,0,bytes_read);
            }
            savedb.flush();
            indb.close();
            savedb.close();

            resourceDataDao.deleteAll();
            CSVReader csvReader = new CSVReader(new FileReader(tempFile));
            String nextLine[];
            while ((nextLine = csvReader.readNext()) != null){
                resourceDataDao.insert(new ResourceData(
                        nextLine[0],
                        nextLine[1],
                        nextLine[2],
                        nextLine[3]
                ));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}