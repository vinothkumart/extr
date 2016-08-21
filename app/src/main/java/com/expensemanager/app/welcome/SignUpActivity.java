package com.expensemanager.app.welcome;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.expensemanager.app.R;
import com.expensemanager.app.main.MainActivity;
import com.expensemanager.app.service.SyncUser;

import org.json.JSONObject;

import bolts.Continuation;
import bolts.Task;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SignUpActivity extends AppCompatActivity {
    private static final String TAG = SignUpActivity.class.getSimpleName();

    private String email;
    private String password;
    private String confirmPassword;
    private String fullname;

    @BindView(R.id.sign_up_activity_sign_up_button_id) Button signUpButton;
    @BindView(R.id.sign_up_activity_email_edit_text_id) EditText emailEditText;
    @BindView(R.id.sign_up_activity_password_edit_text_id) EditText passwordEditText;
    @BindView(R.id.sign_up_activity_confirm_password_edit_text_id) EditText confirmPasswordEditText;
    @BindView(R.id.sign_up_activity_name_edit_text_id) EditText nameEditText;
    @BindView(R.id.sign_up_activity_error_text_view_id) TextView errorMessageTextView;
    @BindView(R.id.sign_up_activity_mismatch_image_view_id) ImageView mismatchImageView;
    @BindView(R.id.sign_up_activity_clear_email_image_view_id) ImageView clearEmailImageView;
    @BindView(R.id.sign_up_activity_clear_password_image_view_id) ImageView clearPasswordImageView;
    @BindView(R.id.sign_up_activity_clear_name_image_view_id) ImageView clearNameImageView;
    @BindView(R.id.sign_up_activity_error_relative_layout_id) RelativeLayout errorMessageRelativeLayout;
    @BindView(R.id.sign_up_activity_step_one_relative_layout_id) RelativeLayout stepOneRelativeLayout;
    @BindView(R.id.sign_up_activity_step_two_relative_layout_id) RelativeLayout stepTwoRelativeLayout;
    @BindView(R.id.sign_up_activity_title_text_view_id) TextView titleTextView;

    public static void newInstance(Context context) {
        Intent intent = new Intent(context, SignUpActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_up_activity);
        ButterKnife.bind(this);

        signUpButton.setEnabled(false);
        signUpButton.setOnClickListener(this::signUp);

        emailEditText.addTextChangedListener(emailTextWatcher);
        passwordEditText.addTextChangedListener(passwordTextWatcher);
        confirmPasswordEditText.addTextChangedListener(confirmPasswordTextWatcher);
        nameEditText.addTextChangedListener(nameTextWatcher);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.sign_up_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_login_activity_id:
                LoginActivity.newInstance(this);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @OnClick({R.id.sign_up_activity_clear_email_image_view_id, R.id.sign_up_activity_clear_password_image_view_id,
        R.id.sign_up_activity_clear_name_image_view_id})
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.sign_up_activity_clear_email_image_view_id:
                emailEditText.setText("");
                break;
            case R.id.sign_up_activity_clear_password_image_view_id:
                passwordEditText.setText("");
                break;
            case R.id.sign_up_activity_clear_name_image_view_id:
                nameEditText.setText("");
                break;
        }
    }

    public void signUp(View v) {
        //todo: add progress bar

        if (stepOneRelativeLayout.getVisibility() == View.VISIBLE) {
            setStepTwo();
        } else {
            SyncUser.signUp(email, password).continueWith(onSignUpSuccess, Task.UI_THREAD_EXECUTOR);
        }
    }

    private void setStepTwo() {
        titleTextView.setText(R.string.sign_up_title_step_two);
        stepOneRelativeLayout.setVisibility(View.GONE);
        stepTwoRelativeLayout.setVisibility(View.VISIBLE);
        signUpButton.setText(R.string.sign_up);
    }

    private void setStepOne() {
        titleTextView.setText(R.string.sign_up_title_step_one);
        stepTwoRelativeLayout.setVisibility(View.GONE);
        stepOneRelativeLayout.setVisibility(View.VISIBLE);
        signUpButton.setText(R.string.next);
    }

    private Continuation<JSONObject, Void> onSignUpSuccess = new Continuation<JSONObject, Void>() {
        @Override
        public Void then(Task<JSONObject> task) throws Exception {
            if (task.isFaulted()) {
                Log.e(TAG, "Error in sign up.", task.getError());
                // todo: dismiss progress bar
                // display info message
            }

            if (task.getResult().has("error")) {
                errorMessageTextView.setText(task.getResult().getString("error"));
                errorMessageTextView.setVisibility(View.VISIBLE);
            } else {
                SyncUser.login(email, password).onSuccess(onLoginSuccess, Task.UI_THREAD_EXECUTOR);
            }
            return null;
        }
    };

    private Continuation<JSONObject, Void> onLoginSuccess = new Continuation<JSONObject, Void>() {
        @Override
        public Void then(Task<JSONObject> task) throws Exception {
            // todo: dismiss progress bar
            if (task.isFaulted()) {
                Log.e(TAG, "Error in login. ", task.getError());
                // display info message
            }

            if (task.getResult().has("error")) {
                errorMessageTextView.setText(task.getResult().getString("error"));
                errorMessageTextView.setVisibility(View.VISIBLE);
                errorMessageRelativeLayout.setVisibility(View.VISIBLE);
            } else {
                MainActivity.newInstance(SignUpActivity.this);
            }

            return null;
        }
    };

    private TextWatcher emailTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            setButton();
            clearEmailImageView.setVisibility(email != null && email.length() != 0 ? View.VISIBLE : View.GONE);
        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    };

    private TextWatcher passwordTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            setButton();
            clearPasswordImageView.setVisibility(password != null && password.length() != 0 ? View.VISIBLE : View.GONE);
            setMismatchSign();
        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    };

    private TextWatcher confirmPasswordTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            setButton();
            setMismatchSign();
        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    };

    private void setMismatchSign() {
        mismatchImageView.setVisibility(confirmPassword != null && confirmPassword.length() > 0 ? View.VISIBLE : View.GONE);
        mismatchImageView.setImageResource(password.equals(confirmPassword) ? R.drawable.ic_check : R.drawable.ic_alert_circle_outline);
    }

    private TextWatcher nameTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            setButton();
            clearNameImageView.setVisibility(fullname != null && fullname.length() != 0 ? View.VISIBLE : View.GONE);
        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    };

    private void setButton() {
        signUpButton.setEnabled(isValidUserInfo());
        if (signUpButton.isEnabled()) {
            int color = ContextCompat.getColor(this, R.color.white);
            signUpButton.setTextColor(color);
        } else {
            int color = ContextCompat.getColor(this, R.color.blue);
            signUpButton.setTextColor(color);
        }
    }

    private boolean isValidUserInfo() {
        getSignUpInfo();

        // Validate SignUp Step 1
        if (stepOneRelativeLayout.getVisibility() == View.VISIBLE) {
            if (email == null || email.length() < 7) {
                return false;
            }
        } else {
        // Validate SignUp Step 2
            if (password == null || password.length() < 6) {
                return false;
            }

            if (!password.equals(confirmPassword)) {
                return false;
            }

            if (fullname == null || fullname.length() < 5) {
                return false;
            }
        }

        return true;
    }

    private void getSignUpInfo() {
        email = emailEditText.getText().toString();
        password = passwordEditText.getText().toString();
        confirmPassword = confirmPasswordEditText.getText().toString();
        fullname = nameEditText.getText().toString();
    }

    @Override
    public void onBackPressed() {
        if (stepTwoRelativeLayout.getVisibility() == View.VISIBLE) {
            setStepOne();
        } else {
            finish();
        }
    }
}