package com.example.mystorage;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;

public class EditActivity extends AppCompatActivity {

    Menu menuEditActivity;
    MenuItem done, clear;
    TextInputEditText resource, login, password, description;
    Boolean isResourceChanged, isLoginChanged, isPasswordChanged, isDescriptionChanged;
    String str_resource, str_login, str_password, str_description;

    ResourceData resourceDataOld;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        resource = (TextInputEditText)findViewById(R.id.resource);
        login = (TextInputEditText)findViewById(R.id.login);
        password = (TextInputEditText)findViewById(R.id.password);
        description = (TextInputEditText)findViewById(R.id.description);

        //запрос полученных данных для отображения
        Intent requestIntent = getIntent();
        Bundle arguments = requestIntent.getExtras();
        resourceDataOld = (ResourceData)arguments.getSerializable(ResourceData.class.getSimpleName());

        str_resource = resourceDataOld.getResource();
        str_login = resourceDataOld.getLogin();
        str_password = resourceDataOld.getPassword();
        str_description = resourceDataOld.getDescription();
        isResourceChanged = isLoginChanged = isPasswordChanged = isDescriptionChanged = false;

        resource.setText(str_resource);
        login.setText(str_login);
        password.setText(str_password);
        description.setText(str_description);

        resource.addTextChangedListener(new GenericTextWatcher(resource));
        login.addTextChangedListener(new GenericTextWatcher(login));
        password.addTextChangedListener(new GenericTextWatcher(password));
        description.addTextChangedListener(new GenericTextWatcher(description));

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_add_menu, menu);
        menuEditActivity = menu;
        done = menuEditActivity.findItem(R.id.doneMenuAdd);
        clear = menuEditActivity.findItem(R.id.clearMenuAdd);
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
                    /*String new_str_resource = resource.getEditableText().toString();
                    String new_str_login = login.getEditableText().toString();
                    String new_str_password = password.getEditableText().toString();
                    String new_str_description = description.getEditableText().toString();
                    ResourceData resourceData = new ResourceData(new_str_resource, new_str_login, new_str_password, new_str_description);//*/
                    resourceDataOld.setResource(resource.getEditableText().toString());
                    resourceDataOld.setLogin(login.getEditableText().toString());
                    resourceDataOld.setPassword(password.getEditableText().toString());
                    resourceDataOld.setDescription(description.getEditableText().toString());
                    Log.d("myLogs", "editable note with id = " + resourceDataOld.getId());
                    intent.putExtra(ResourceData.class.getSimpleName(), resourceDataOld);
                    setResult(RESULT_OK, intent);
                    finish();
                }
                return true;
            case R.id.clearMenuAdd:
                new AlertDialog.Builder(this)
                        .setTitle("Remove Changes")
                        .setMessage("Do you really want to remove all changes?")
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int whichButton) {
                                resource.setText(str_resource);
                                login.setText(str_login);
                                password.setText(str_password);
                                description.setText(str_description);
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
            switch(view.getId()){
                case R.id.resource:
                    isResourceChanged = !str_resource.equals(text);
                    break;
                case R.id.login:
                    isLoginChanged = !str_login.equals(text);
                    break;
                case R.id.password:
                    isPasswordChanged = !str_password.equals(text);
                    break;
                case R.id.description:
                    isDescriptionChanged = !str_description.equals(text);
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