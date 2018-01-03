package com.example.fadi.testingrx.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.fadi.testingrx.AiRealTimeActivity;
import com.example.fadi.testingrx.R;

public class AddAIPostureActivity extends AppCompatActivity {

    Button buttonOK;
    Button buttonCancel;
    EditText editTextPostureName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_aiposture);

        initUI();
    }

    private void initUI(){
        buttonOK = findViewById(R.id.buttonOK);
        buttonCancel = findViewById(R.id.buttonCancel);
        editTextPostureName = findViewById(R.id.editTextPostureName);

        buttonOK.setOnClickListener(a->{
            Intent intentResult = new Intent();
            String postureName=editTextPostureName.getText().toString();
            intentResult.putExtra("posture_name",postureName);
            setResult(1,intentResult);//1 is for success
            finish();
        });

        buttonCancel.setOnClickListener(a->{
            Intent intentResult = new Intent();
            setResult(0,intentResult);//0 is for cancel
            finish();
        });
    }

}
