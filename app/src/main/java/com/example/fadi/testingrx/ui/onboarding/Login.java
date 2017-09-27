package com.example.fadi.testingrx.ui.onboarding;

import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ProgressBar;

import com.example.fadi.testingrx.R;
import com.google.firebase.auth.FirebaseAuth;

import net.yslibrary.android.keyboardvisibilityevent.Unregistrar;

public class Login extends AppCompatActivity {

    public static final String TAG = "LoginActivity";
    ConstraintLayout layoutLoginChoices, layoutContainerConstraintSet, layoutLoginConstraintSet, layoutLoginFormConstraintSet;
    Button btnLoginEmail, btnLoginFacebook, btnLoginGoogle, btnLoginTwitter, btnForgetPassword, btnValidateEmail, btnNewAccount;
    TextInputEditText fieldEmail, fieldPassword;
    ProgressBar loginProgressBar;
    FirebaseAuth mAuth;
    ConstraintSet containerConstraintSet, loginConstraintSet, loginFormConstraintSet;
    // public DataManager dataManager;
    private Unregistrar unregistrar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
    }
}
