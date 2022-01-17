package com.example.mystorage;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class LockScreenActivity extends AppCompatActivity implements View.OnClickListener {

    final int REQUEST_CODE_AUTH = 3, REQUEST_CODE_CHANGE_PIN_OLD = 4, REQUEST_CODE_CREATE_PIN = 5, REQUEST_CODE_AGAIN = 6, REQUEST_CODE_CHANGE_PIN_NEW = 7;
    private int mode;
    TextView button_0, button_1, button_2, button_3, button_4, button_5, button_6, button_7, button_8, button_9, button_next, title;
    ImageView imageButtonDelete;
    String resultPin = "", tempResultPin = "";
    int checkBoxPosition = 0;
    CheckBox checkBox_1, checkBox_2, checkBox_3, checkBox_4;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lock_screen);

        button_0 = (TextView)findViewById(R.id.button_0);
        button_0.setOnClickListener(this);
        button_1 = (TextView)findViewById(R.id.button_1);
        button_1.setOnClickListener(this);
        button_2 = (TextView)findViewById(R.id.button_2);
        button_2.setOnClickListener(this);
        button_3 = (TextView)findViewById(R.id.button_3);
        button_3.setOnClickListener(this);
        button_4 = (TextView)findViewById(R.id.button_4);
        button_4.setOnClickListener(this);
        button_5 = (TextView)findViewById(R.id.button_5);
        button_5.setOnClickListener(this);
        button_6 = (TextView)findViewById(R.id.button_6);
        button_6.setOnClickListener(this);
        button_7 = (TextView)findViewById(R.id.button_7);
        button_7.setOnClickListener(this);
        button_8 = (TextView)findViewById(R.id.button_8);
        button_8.setOnClickListener(this);
        button_9 = (TextView)findViewById(R.id.button_9);
        button_9.setOnClickListener(this);
        button_next = (TextView)findViewById(R.id.button_next);
        button_next.setOnClickListener(this);
        imageButtonDelete = (ImageView)findViewById(R.id.button_delete);
        imageButtonDelete.setOnClickListener(this);

        checkBox_1 = (CheckBox)findViewById(R.id.checkbox_1);
        checkBox_2 = (CheckBox)findViewById(R.id.checkbox_2);
        checkBox_3 = (CheckBox)findViewById(R.id.checkbox_3);
        checkBox_4 = (CheckBox)findViewById(R.id.checkbox_4);

        title = (TextView)findViewById(R.id.title_text_view);

        Intent intent = getIntent();
        mode = intent.getIntExtra("mode", 0);

        if (mode == REQUEST_CODE_AUTH){
            if(MyCipher.isPinExist(this))
            {
                title.setText(R.string.mode_auth);
            }
            else
            {
                title.setText(R.string.mode_create);
                mode = REQUEST_CODE_CREATE_PIN;
            }
        }
        else if (mode == REQUEST_CODE_CHANGE_PIN_OLD){
            title.setText(R.string.mode_change_old);
        }
    }

    @Override
    protected void onResume()
    {
        super.onResume();

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.button_0:
                setValueAndChecked("0");
                break;
            case R.id.button_1:
                setValueAndChecked("1");
                break;
            case R.id.button_2:
                setValueAndChecked("2");
                break;
            case R.id.button_3:
                setValueAndChecked("3");
                break;
            case R.id.button_4:
                setValueAndChecked("4");
                break;
            case R.id.button_5:
                setValueAndChecked("5");
                break;
            case R.id.button_6:
                setValueAndChecked("6");
                break;
            case R.id.button_7:
                setValueAndChecked("7");
                break;
            case R.id.button_8:
                setValueAndChecked("8");
                break;
            case R.id.button_9:
                setValueAndChecked("9");
                break;
            case R.id.button_next:
                ChangeTitleAndStage();
                break;
            /*case R.id.button_right:
                getValueAndChecked();
                break;//*/
            case R.id.button_delete:
                getValueAndChecked();
                break;
        };
    }

    private void setValueAndChecked(String value){

        resultPin +=value;
        if(checkBoxPosition == 0) {
            checkBox_1.setChecked(true);
        } else if(checkBoxPosition == 1) {
            checkBox_2.setChecked(true);
        } else if(checkBoxPosition == 2) {
            checkBox_3.setChecked(true);
        } else if(checkBoxPosition == 3) {
            checkBox_4.setChecked(true);
        };
        checkBoxPosition += 1;
    };

    private void getValueAndChecked(){
        resultPin =resultPin.substring(0, resultPin.length() - 1);
        if(checkBoxPosition == 4) {
            checkBox_4.setChecked(false);
        } else if(checkBoxPosition == 3) {
            checkBox_3.setChecked(false);
        } else if(checkBoxPosition == 2) {
            checkBox_2.setChecked(false);
        } else if(checkBoxPosition == 1) {
            checkBox_1.setChecked(false);
        };
        checkBoxPosition--;
    };

    private void setTitle(String newTitle){
        title.setText(""+newTitle);
    };

    private void ChangeTitleAndStage(){
        if (mode == REQUEST_CODE_CREATE_PIN || mode == REQUEST_CODE_CHANGE_PIN_NEW)
        {
            tempResultPin = resultPin;
            resultPin = "";
            title.setText(R.string.mode_again);
            checkBox_1.setChecked(false);
            checkBox_2.setChecked(false);
            checkBox_3.setChecked(false);
            checkBox_4.setChecked(false);
            checkBoxPosition = 0;
            mode = REQUEST_CODE_AGAIN;
        }
        else if (mode == REQUEST_CODE_AUTH)
        {
            if (resultPin.equals(MyCipher.getCode(this)))
            {
                Intent intent = new Intent();
                setResult(RESULT_OK, intent);
                finish();
            }
            else
            {
                finish();
            }
        }
        else if(mode == REQUEST_CODE_CHANGE_PIN_OLD)
        {
            if (resultPin.equals(MyCipher.getCode(this)))
            {
                resultPin = "";
                title.setText(R.string.mode_change_new);
                checkBox_1.setChecked(false);
                checkBox_2.setChecked(false);
                checkBox_3.setChecked(false);
                checkBox_4.setChecked(false);
                checkBoxPosition = 0;
                mode = REQUEST_CODE_CHANGE_PIN_NEW;
            }
            else
            {
                Toast.makeText(this, "Incorrect PIN!", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
        else
        {
            //change or create finish
            if (resultPin.equals(tempResultPin))
            {

                //MyCipher.saveToPref(this, resultPin);
                Intent intent = new Intent();
                intent.putExtra("new_pin", resultPin);
                setResult(RESULT_OK, intent);
                finish();
            }
            else
            {
                finish();
            }
        }

    }
}