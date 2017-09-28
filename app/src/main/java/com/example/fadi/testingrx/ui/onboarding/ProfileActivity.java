package com.example.fadi.testingrx.ui.onboarding;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputEditText;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.example.fadi.testingrx.MainActivity;
import com.example.fadi.testingrx.R;
import com.example.fadi.testingrx.db.model.UserGender;
import com.example.fadi.testingrx.ui.base.fBaseActivity;
import com.tapadoo.alerter.Alerter;

import net.yslibrary.android.keyboardvisibilityevent.util.UIUtil;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;
import io.reactivex.disposables.CompositeDisposable;

/**
 * Created by fadi on 27/09/2017.
 */

public class ProfileActivity extends fBaseActivity {
    Toolbar toolbar;
    CircleImageView profileImage;
    FloatingActionButton fabEditProfileImage, fabEditProfile;
    TextView userName, userHeight, userWeight, userGender, userBirthdate;
    TextInputEditText txtEditUserName, txtEditUserHeight, txtEditUserWight, txtEditUserBirthDate;
    RadioGroup rdGender;
    RadioButton rbFemale, rbMale;
    Boolean enterEditMode = false;
    String EXTRA_EDIT_MODE = "edit_mode";
    String TAG = "ProfileActivity";
    ConstraintLayout layoutContentConstraintSet, layoutUsernameConstraintSet;
    ConstraintSet contentConstraintSet, usernameConstraintSet;
    View.OnFocusChangeListener onFocusChangeListener;
    private CompositeDisposable compositeDisposable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        initView();
        compositeDisposable = new CompositeDisposable();
        if (getIntent().getExtras() != null) {
            enterEditMode = getIntent().getBooleanExtra(EXTRA_EDIT_MODE, false);
        }
        setSupportActionBar(toolbar);
        toolbar.setOnClickListener((View v) -> {
            finish();
        });
        try {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        } catch (Exception execption) {
            Log.e(TAG, "NULL POINTER MENU");
        }
        fabEditProfile.setOnClickListener((View v) -> {
            editProfile();
        });
        fabEditProfileImage.setOnClickListener((View v) -> {
            //TODO
        });
        fabEditProfileImage.hide(); // temporarly
        onFocusChangeListener = new View.OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                EditText editText = (EditText) findViewById(v.getId());
                if (hasFocus) {
                    editText.setSelection(editText.length());
                }
            }
        };

        txtEditUserName.setOnFocusChangeListener(onFocusChangeListener);
        txtEditUserHeight.setOnFocusChangeListener(onFocusChangeListener);
        txtEditUserWight.setOnFocusChangeListener(onFocusChangeListener);
        txtEditUserBirthDate.setOnClickListener((View v) -> {
            editBirthDate();
        });
    }


    void initView() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);

        profileImage = (CircleImageView) findViewById(R.id.iv_profile_picture);
        // adding temporary event when the profile image is clicked, to go skip to the actual screen
        profileImage.setOnClickListener(view -> {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        });

        fabEditProfileImage = (FloatingActionButton) findViewById(R.id.fab_edit_picture);
        fabEditProfile = (FloatingActionButton) findViewById(R.id.fab_edit_profile);
        rdGender = (RadioGroup) findViewById(R.id.rg_gender);
        rbFemale = (RadioButton) findViewById(R.id.rb_female);
        rbMale = (RadioButton) findViewById(R.id.rb_male);
        userName = (TextView) findViewById(R.id.tv_username);
        userHeight = (TextView) findViewById(R.id.tv_height_value);
        userWeight = (TextView) findViewById(R.id.tv_weight_value);
        userGender = (TextView) findViewById(R.id.tv_gender_value);
        userBirthdate = (TextView) findViewById(R.id.tv_birth_date_value);
        txtEditUserName = (TextInputEditText) findViewById(R.id.et_username);
        txtEditUserHeight = (TextInputEditText) findViewById(R.id.et_height);
        txtEditUserWight = (TextInputEditText) findViewById(R.id.et_weight);
        txtEditUserBirthDate = (TextInputEditText) findViewById(R.id.et_birth_date);
        layoutContentConstraintSet = (ConstraintLayout) findViewById(R.id.layout_content);
        layoutUsernameConstraintSet = (ConstraintLayout) findViewById(R.id.layout_username);


        //ConstraintSet
        contentConstraintSet = new ConstraintSet(); // create a Constraint Set
        usernameConstraintSet = new ConstraintSet(); // create a Constraint Set

        contentConstraintSet.clone(layoutContentConstraintSet);
        contentConstraintSet.applyTo(layoutContentConstraintSet);

        usernameConstraintSet.clone(layoutUsernameConstraintSet);
        usernameConstraintSet.applyTo(layoutUsernameConstraintSet);



    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_activity_profile, menu);
        if (enterEditMode) {
            menu.findItem(R.id.item_save_profile).setVisible(true);
            enterEditMode = false;
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.item_save_profile) {
            saveProfile();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();

        //TODO Some Db Treatment

        if (enterEditMode) {
            editProfile();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        compositeDisposable.clear();
    }

    private void editProfile() {
        this.setTitle((CharSequence) this.getString((R.string.global_edit_profile)));
        Menu menu = ((Toolbar) findViewById(R.id.toolbar)).getMenu();
        if (menu != null) {
            MenuItem menuItem = menu.findItem(R.id.item_save_profile);
            if (menuItem != null) {
                menuItem.setVisible(true);
            }
        }
        fabEditProfile.hide();
        fabEditProfileImage.show();
        //TODO MODIFY THIS TO BE ADAPT TO CHANGES
        toggleEditModeUsername(true);
        toggleEditModeUserInfo(true);
    }

    private void stopEditProfile() {
        UIUtil.hideKeyboard(this);

        this.setTitle((CharSequence) this.getString((R.string.global_my_profile)));
        Menu menu = ((Toolbar) findViewById(R.id.toolbar)).getMenu();
        if (menu != null) {
            MenuItem menuItem = menu.findItem(R.id.item_save_profile);
            if (menuItem != null) {
                menuItem.setVisible(false);
            }
        }
        fabEditProfile.show();
        fabEditProfileImage.hide();
        toggleEditModeUsername(false);
        toggleEditModeUserInfo(false);
    }

    private void toggleEditModeUserInfo(Boolean isEditMode) {
        usernameConstraintSet.setVisibility(R.id.tv_username, isEditMode ? View.GONE : View.VISIBLE);
        usernameConstraintSet.setVisibility(R.id.til_username, isEditMode ? View.VISIBLE : View.GONE);
        usernameConstraintSet.applyTo((ConstraintLayout) findViewById(R.id.layout_username));
    }

    private void toggleEditModeUsername(Boolean isEditMode) {
        contentConstraintSet.setVisibility(R.id.layout_user_info, isEditMode ? View.GONE : View.VISIBLE);
        contentConstraintSet.setVisibility(R.id.layout_user_info_edit, isEditMode ? View.VISIBLE : View.GONE);
        contentConstraintSet.applyTo((ConstraintLayout) findViewById(R.id.layout_content));
    }

    private void editBirthDate() {
        final Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        DatePickerDialog datePickerDialog = new DatePickerDialog((Context) this, (DatePickerDialog.OnDateSetListener) (new DatePickerDialog.OnDateSetListener() {
            public final void onDateSet(DatePicker datePicker, int year, int month, int dayOfMonth) {
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, month);
                calendar.set(Calendar.DATE, dayOfMonth);
                ((TextInputEditText) ProfileActivity.this.findViewById(R.id.et_birth_date)).setText((CharSequence) SimpleDateFormat.getDateInstance().format(calendar.getTime()));
            }
        }), calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DATE));
        datePickerDialog.show();
    }

    private Boolean checkAllField() {
        return !TextUtils.isEmpty(txtEditUserName.getText())
                && rdGender.getCheckedRadioButtonId() != -1
                && !TextUtils.isEmpty(txtEditUserHeight.getText())
                && !TextUtils.isEmpty(txtEditUserWight.getText())
                && !TextUtils.isEmpty(txtEditUserBirthDate.getText());
    }

    private void saveToDb() {
        String name = txtEditUserName.getText().toString();
        UserGender userGender;
        switch (rdGender.getCheckedRadioButtonId()) {
            case (R.id.rb_female):
                userGender = UserGender.FEMALE;
                break;
            case (R.id.rb_male):
                userGender = UserGender.MALE;
                break;
            default:
                userGender = null;
                break;
        }
        int height = Integer.valueOf(txtEditUserHeight.getText().toString());
        float weight = Float.valueOf(txtEditUserWight.getText().toString());
        String date = txtEditUserBirthDate.getText().toString();
        //TODO DB SAVE
//        dataManager.getDbUser() ?.let {
//            val user = it.blockingFirst()
//            user.name = name
//            user.gender = gender
//            user.height = height
//            user.weight = weight
//            user.birthDate = SimpleDateFormat.getDateInstance().parse(date)
//            dataManager.updateDbUser(user)
//                    .subscribeOn(Schedulers.io())
//                    .subscribe()
//        }
    }
    private void saveProfile() {
        if (checkAllField()) {
            saveToDb();
            stopEditProfile();
        } else {
            Alerter.create(this)
                    .setTitle(R.string.profile_please_fill_in_all_fields)
                    .setBackgroundColor(R.color.colorAccent)
                    .showIcon(false)
                    .show();
        }
    }

}

