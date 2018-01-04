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
import android.widget.ImageButton;

import com.example.fadi.testingrx.AiRealTimeActivity;
import com.example.fadi.testingrx.R;

public class AddAIPostureActivity extends AppCompatActivity {

    Button buttonOK;
    Button buttonCancel;
    EditText editTextPostureName;

    ImageButton imageButtonPosture1;
    ImageButton imageButtonPosture2;
    ImageButton imageButtonPosture3;
    ImageButton imageButtonPosture4;
    ImageButton imageButtonPosture5;
    ImageButton imageButtonPosture6;

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

        imageButtonPosture1 = findViewById(R.id.imageButton1);
        imageButtonPosture1.setImageResource(R.drawable.ic_posture_1);
        imageButtonPosture1.setOnClickListener(a->{
            Intent intentResult = new Intent();
            String postureName=editTextPostureName.getText().toString();
            intentResult.putExtra("posture_name",postureName);
            intentResult.putExtra("posture_icon",1);
            setResult(1,intentResult);//1 is for success
            finish();
        });

        imageButtonPosture2 = findViewById(R.id.imageButton2);
        imageButtonPosture2.setImageResource(R.drawable.ic_posture_2);
        imageButtonPosture2.setOnClickListener(a->{
            Intent intentResult = new Intent();
            String postureName=editTextPostureName.getText().toString();
            intentResult.putExtra("posture_name",postureName);
            intentResult.putExtra("posture_icon",2);
            setResult(1,intentResult);//1 is for success
            finish();
        });

        imageButtonPosture3 = findViewById(R.id.imageButton3);
        imageButtonPosture3.setImageResource(R.drawable.ic_posture_3);
        imageButtonPosture3.setOnClickListener(a->{
            Intent intentResult = new Intent();
            String postureName=editTextPostureName.getText().toString();
            intentResult.putExtra("posture_name",postureName);
            intentResult.putExtra("posture_icon",3);
            setResult(1,intentResult);//1 is for success
            finish();
        });

        imageButtonPosture4 = findViewById(R.id.imageButton4);
        imageButtonPosture4.setImageResource(R.drawable.ic_posture_4);
        imageButtonPosture4.setOnClickListener(a->{
            Intent intentResult = new Intent();
            String postureName=editTextPostureName.getText().toString();
            intentResult.putExtra("posture_name",postureName);
            intentResult.putExtra("posture_icon",4);
            setResult(1,intentResult);//1 is for success
            finish();
        });

        imageButtonPosture5 = findViewById(R.id.imageButton5);
        imageButtonPosture5.setImageResource(R.drawable.ic_posture_5);
        imageButtonPosture5.setOnClickListener(a->{
            Intent intentResult = new Intent();

            String postureName=editTextPostureName.getText().toString();
            intentResult.putExtra("posture_name",postureName);
            intentResult.putExtra("posture_icon",5);
            setResult(1,intentResult);//1 is for success
            finish();
        });

        imageButtonPosture6 = findViewById(R.id.imageButton6);
        imageButtonPosture6.setImageResource(R.drawable.ic_posture_6);
        imageButtonPosture6.setOnClickListener(a->{
            Intent intentResult = new Intent();
            String postureName=editTextPostureName.getText().toString();
            intentResult.putExtra("posture_name",postureName);
            intentResult.putExtra("posture_icon",6);
            setResult(1,intentResult);//1 is for success
            finish();
        });
    }

}
