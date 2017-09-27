package com.example.fadi.testingrx.ui.onboarding;

import android.app.Activity;
import android.content.Intent;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.fadi.testingrx.NormalModeActivity;
import com.example.fadi.testingrx.R;
import com.example.fadi.testingrx.db.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.jakewharton.rxbinding2.view.RxView;
import com.tapadoo.alerter.Alerter;

import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent;
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEventListener;
import net.yslibrary.android.keyboardvisibilityevent.Unregistrar;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

        initView();

        unregistrar = KeyboardVisibilityEvent.registerEventListener((Activity) this, (KeyboardVisibilityEventListener) (new KeyboardVisibilityEventListener() {
            public final void onVisibilityChanged(boolean isOpen) {
                Login.this.onKeyBoardVisibilityChanger(isOpen);
            }
        }));

        btnLoginEmail.setOnClickListener((View v) -> {
            showLoginForm();
        });

        btnForgetPassword.setOnClickListener((View v) -> {
            //resetPassword();
        });

        btnValidateEmail.setOnClickListener((View v) -> {
            //loginValidate();
        });

        btnNewAccount.setOnClickListener((View v) -> {
            //createNewAccount();
        });
    }

    void initView() {
        //layouts
        layoutContainerConstraintSet = (ConstraintLayout) findViewById(R.id.layout_container);
        layoutLoginConstraintSet = (ConstraintLayout) findViewById(R.id.layout_login);
        layoutLoginFormConstraintSet = (ConstraintLayout) findViewById(R.id.layout_login_form);
        layoutLoginChoices = (ConstraintLayout) findViewById(R.id.layout_login_choices);


        //ConstraintSet
        containerConstraintSet = new ConstraintSet(); // create a Constraint Set
        loginConstraintSet = new ConstraintSet(); // create a Constraint Set
        loginFormConstraintSet = new ConstraintSet(); // create a Constraint Set

        containerConstraintSet.clone(layoutContainerConstraintSet);
        containerConstraintSet.applyTo(layoutContainerConstraintSet);

        loginConstraintSet.clone(layoutLoginConstraintSet);
        loginConstraintSet.applyTo(layoutLoginConstraintSet);

        loginFormConstraintSet.clone(layoutLoginFormConstraintSet);
        loginFormConstraintSet.applyTo(layoutLoginFormConstraintSet);

        //other views
        btnLoginEmail = (Button) findViewById(R.id.btn_login_email);
        btnLoginFacebook = (Button) findViewById(R.id.btn_login_facebook);
        btnLoginGoogle = (Button) findViewById(R.id.btn_login_google);
        btnLoginTwitter = (Button) findViewById(R.id.btn_login_twitter);
        btnForgetPassword = (Button) findViewById(R.id.btn_login_email_forgot_password);
        btnValidateEmail = (Button) findViewById(R.id.btn_login_email_validate);
        btnNewAccount = (Button) findViewById(R.id.btn_login_email_new_account);
        fieldEmail = (TextInputEditText) findViewById(R.id.et_email);
        fieldPassword = (TextInputEditText) findViewById(R.id.et_password);

        loginProgressBar = (ProgressBar) findViewById(R.id.progress_bar_login);

        RxView.clicks(findViewById(R.id.buttonSkip))
                .subscribe(v->{
                    Intent intent = new Intent (this, ProfileActivity.class);
                    startActivity(intent);
                    finish();
                });
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregistrar.unregister();
    }

    @Override
    public void onBackPressed() {
        if (layoutLoginFormConstraintSet.getVisibility() == View.VISIBLE) {
            hideLoginForm();
        } else {
            super.onBackPressed();
        }
    }

    private void showLoginForm() {
        TransitionManager.beginDelayedTransition(layoutLoginConstraintSet);
        loginConstraintSet.setVisibility(R.id.layout_login_choices, ConstraintSet.GONE);
        loginConstraintSet.setVisibility(R.id.layout_login_form, ConstraintSet.VISIBLE);
        loginConstraintSet.applyTo(layoutLoginConstraintSet);
    }

    private void hideLoginForm() {
        TransitionManager.beginDelayedTransition(layoutLoginConstraintSet);
        loginConstraintSet.setVisibility(R.id.layout_login_choices, ConstraintSet.VISIBLE);
        loginConstraintSet.setVisibility(R.id.layout_login_form, ConstraintSet.GONE);
        loginConstraintSet.applyTo(layoutLoginConstraintSet);
    }

    private void onKeyBoardVisibilityChanger(Boolean isOpen) {
        TransitionManager.beginDelayedTransition(layoutContainerConstraintSet);
        containerConstraintSet.setVerticalBias(R.id.layout_container, isOpen ? 0.0F : 0.5F);
        containerConstraintSet.applyTo(layoutContainerConstraintSet);
    }

    private void resetPassword() {
        // TODO
        Toast.makeText(this, "Not implemented", Toast.LENGTH_SHORT).show();
    }

    private void loginValidate() {

        String userEmail = fieldEmail.getText().toString();
        String userPassword = fieldPassword.getText().toString();

        if (!TextUtils.isEmpty(userEmail) && !TextUtils.isEmpty(userPassword)) {

            btnValidateEmail.setEnabled(false);
            btnNewAccount.setEnabled(false);

            TransitionManager.beginDelayedTransition(layoutLoginFormConstraintSet);
            loginFormConstraintSet.setVisibility(R.id.til_username, ConstraintSet.INVISIBLE);
            loginFormConstraintSet.setVisibility(R.id.til_password, ConstraintSet.INVISIBLE);
            loginFormConstraintSet.setVisibility(R.id.btn_login_email_forgot_password, ConstraintSet.INVISIBLE);
            loginFormConstraintSet.setVisibility(R.id.progress_bar_login, ConstraintSet.VISIBLE);
            loginFormConstraintSet.applyTo(layoutLoginConstraintSet);


            Log.e("Identification", "Mail: " + userEmail + " Pass: " + userPassword);
            FirebaseAuth.getInstance().signInWithEmailAndPassword(userEmail, userPassword)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            Log.e("Sign In Account", "signInWithEmail:onComplete:" + task.isSuccessful());

                            // If sign in fails, display a message to the user. If sign in succeeds
                            // the auth state listener will be notified and logic to handle the
                            // signed in user can be handled in the listener.

                            if (!task.isSuccessful()) {
                                Alerter.create(Login.this)
                                        .setTitle("Login")
                                        .setText(task.getException().toString())
                                        .setBackgroundColor(R.color.colorAccent)
                                        .setIcon(R.drawable.ic_error_white_48dp)
                                        .show();
                                btnValidateEmail.setEnabled(true);
                                btnNewAccount.setEnabled(true);

                                TransitionManager.beginDelayedTransition(layoutLoginFormConstraintSet);
                                loginFormConstraintSet.setVisibility(R.id.til_username, ConstraintSet.VISIBLE);
                                loginFormConstraintSet.setVisibility(R.id.btn_login_email_forgot_password, ConstraintSet.VISIBLE);
                                loginFormConstraintSet.setVisibility(R.id.progress_bar_login, ConstraintSet.INVISIBLE);
                                loginFormConstraintSet.applyTo(layoutLoginFormConstraintSet);

                                Log.e("Sign In Account", "signInWithEmail:failed", task.getException());
                                //Toast.makeText(getApplicationContext(), "Authentification Fail. Please, try again.", Toast.LENGTH_SHORT).show();

                            } else {
                                Alerter.create(Login.this)
                                        .setTitle("User logged in")
                                        .setText(task.getResult().getUser().getDisplayName())
                                        .setBackgroundColor(R.color.colorAccent)
                                        .showIcon(false)
                                        .show();

                                createUser(task.getResult().getUser().getUid());

                                Log.e("Sign In Account", "Success ! waiting for user data ...");
                            }
                        }
                    });

            FirebaseAuth.getInstance().signInWithEmailAndPassword(userEmail, userPassword)
                    .addOnFailureListener(this, new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        }
    }

    private void createNewAccount() {
        String userEmail = fieldEmail.getText().toString();
        String userPassword = fieldPassword.getText().toString();

        if (!TextUtils.isEmpty(userEmail) && isEmailValid(userEmail) && !TextUtils.isEmpty(userPassword)) {

            btnValidateEmail.setEnabled(false);
            btnNewAccount.setEnabled(false);

            FirebaseAuth.getInstance().createUserWithEmailAndPassword(userEmail, userPassword)   //TODO: OKAY
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {

                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            Log.e("Create Account", "createUserWithEmail:onComplete:" + task.isSuccessful());

                            // If sign in fails, display a message to the user. If sign in succeeds
                            // the auth state listener will be notified and logic to handle the
                            // signed in user can be handled in the listener.
                            if (!task.isSuccessful()) {
                                //Toast.makeText(getApplicationContext(), "Authentification Fail. Please, try again.", Toast.LENGTH_SHORT).show();
                                Alerter.create(Login.this)
                                        .setTitle(R.string.login_create_new_account)
                                        .setText(task.getException().toString())
                                        .setBackgroundColor(R.color.colorAccent)
                                        .setIcon(R.drawable.ic_error_white_48dp)
                                        .show();

                                btnValidateEmail.setEnabled(true);
                                btnNewAccount.setEnabled(true);
                            } else {
                                Alerter.create(Login.this)
                                        .setTitle(R.string.login_new_account_created)
                                        .setText(task.getResult().getUser().getEmail())
                                        .setBackgroundColor(R.color.colorAccent)
                                        .showIcon(false)
                                        .show();

                                createUser(task.getResult().getUser().getUid());
                            }

                            // ...
                        }
                    });

        }
    }


    private void createUser(String fireBaseUid) {
        User user = new User(fireBaseUid, null, null, null, null, null);
        // TODO insert to Db
//        dataManager.insertDbUser(user)
//                .subscribeOn(Schedulers.io())
//                .subscribe {
//            finish();
//        }
    }


    private static final String EMAIL_PATTERN = "^[a-zA-Z0-9#_~!$&'()*+,;=:.\"(),:;<>@\\[\\]\\\\]+@[a-zA-Z0-9-]+(\\.[a-zA-Z0-9-]+)*$";
    private Pattern pattern = Pattern.compile(EMAIL_PATTERN);
    private Matcher matcher;

    // method for verification (email)
    public boolean isEmailValid(String email) {
        matcher = pattern.matcher(email);
        return matcher.matches();
    }
}
