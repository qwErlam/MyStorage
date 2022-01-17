package com.example.mystorage;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;

import java.util.Objects;

public class AddActivity extends AppCompatActivity {

    Menu menuAddActivity;
    MenuItem done, clear;
    TextInputEditText resource, login, password, description;
    Boolean isResourceChanged, isLoginChanged, isPasswordChanged, isDescriptionChanged;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        resource = (TextInputEditText)findViewById(R.id.resource);
        login = (TextInputEditText)findViewById(R.id.login);
        password = (TextInputEditText)findViewById(R.id.password);
        description = (TextInputEditText)findViewById(R.id.description);

        isResourceChanged = isLoginChanged = isPasswordChanged = isDescriptionChanged = false;

        resource.addTextChangedListener(new GenericTextWatcher(resource));
        login.addTextChangedListener(new GenericTextWatcher(login));
        password.addTextChangedListener(new GenericTextWatcher(password));
        description.addTextChangedListener(new GenericTextWatcher(description));

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_add_menu, menu);
        menuAddActivity = menu;
        done = menuAddActivity.findItem(R.id.doneMenuAdd);
        clear = menuAddActivity.findItem(R.id.clearMenuAdd);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.doneMenuAdd:
                if(resource.getEditableText().toString().equals("")||password.getEditableText().toString().equals(""))
                {
                    Toast.makeText(this, "Resource name and Password must have value!", Toast.LENGTH_SHORT).show();
                    return true;
                }
                else{
                    Intent intent = new Intent();
                    String str_resource = resource.getEditableText().toString();
                    String str_login = login.getEditableText().toString();
                    String str_password = password.getEditableText().toString();
                    String str_description = description.getEditableText().toString();
                    ResourceData resourceData = new ResourceData(str_resource, str_login, str_password, str_description);
                    intent.putExtra(ResourceData.class.getSimpleName(), resourceData);
                    setResult(RESULT_OK, intent);
                    finish();
                }
                return true;
            case R.id.clearMenuAdd:
                new AlertDialog.Builder(this)
                        .setTitle("Clear input fields")
                        .setMessage("Do you really want to clear data from all input fields?")
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int whichButton) {
                                resource.setText("");
                                login.setText("");
                                password.setText("");
                                description.setText("");
                                done.setEnabled(false);
                                done.setIcon(R.drawable.ic_done_disable);
                                clear.setEnabled(false);
                                clear.setVisible(false);
                            }})
                        .setNegativeButton(android.R.string.no, null).show();
                return true;
            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
    }

    //прослушивание наличия изменений
    private class GenericTextWatcher implements TextWatcher {
        private View view;
        private GenericTextWatcher(View view) {
            this.view = view;
        }

        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

        public void afterTextChanged(Editable editable) {
            String text = editable.toString();
            String str_empty = "";
            switch(view.getId()){
                case R.id.resource:
                    isResourceChanged = !str_empty.equals(text);
                    break;
                case R.id.login:
                    isLoginChanged = !str_empty.equals(text);
                    break;
                case R.id.password:
                    isPasswordChanged = !str_empty.equals(text);
                    break;
                case R.id.description:
                    isDescriptionChanged = !str_empty.equals(text);
                    break;
            };
            if (isResourceChanged || isLoginChanged || isPasswordChanged || isDescriptionChanged)
            {
                done.setEnabled(true);
                done.setIcon(R.drawable.ic_done);
                clear.setEnabled(true);
                clear.setVisible(true);
            }
            else
            {
                done.setEnabled(false);
                done.setIcon(R.drawable.ic_done_disable);
                clear.setEnabled(false);
                clear.setVisible(false);
            }
        }
    }

}