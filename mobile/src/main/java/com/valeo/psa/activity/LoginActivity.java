package com.valeo.psa.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import com.valeo.bleranging.persistence.SdkPreferencesHelper;
import com.valeo.psa.R;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by l-avaratha on 02/09/2016.
 */
public class LoginActivity extends AppCompatActivity {
    private static final String EMAIL_PATTERN = "^[a-zA-Z0-9#_~!$&'()*+,;=:.\"<>@\\[\\]\\\\]+@[a-zA-Z0-9-]+(\\.[a-zA-Z0-9-]+)*$";
    private Pattern pattern = Pattern.compile(EMAIL_PATTERN);
    private Matcher matcher;
    private Button sign_in;
    private TextInputLayout usernameWrapper;
    private TextInputLayout passwordWrapper;
    private TextInputEditText usernameEditText;
    private TextInputEditText passwordEditText;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.psa_activity_login);
        usernameEditText = (TextInputEditText) findViewById(R.id.mail_tiet);
        passwordEditText = (TextInputEditText) findViewById(R.id.password_tiet);
        usernameWrapper = (TextInputLayout) findViewById(R.id.mail_til);
        passwordWrapper = (TextInputLayout) findViewById(R.id.password_til);
        sign_in = (Button) findViewById(R.id.sign_in);
        sign_in.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = usernameEditText.getText().toString();
                String password = passwordEditText.getText().toString();
                if (!validateEmail(username)) {
                    usernameWrapper.setError(getString(R.string.not_valid_mail));
                } else if (!validatePassword(password)) {
                    passwordWrapper.setError(getString(R.string.not_valid_password));
                } else {
                    usernameWrapper.setError(null);
                    passwordWrapper.setError(null);
                    doLogin();
                }
            }
        });
    }

    public void doLogin() {
        LayoutInflater factory = LayoutInflater.from(this);
        final View dialogView = factory.inflate(R.layout.psa_sign_in_alert_dialog, null);
        final AlertDialog alertDialog = new AlertDialog.Builder(this, R.style.MyDialog).create();
        alertDialog.setView(dialogView);
        alertDialog.setCancelable(false);
        Button yesButton = (Button) dialogView.findViewById(R.id.sign_in_message_alert_yes);
        Button noButton = (Button) dialogView.findViewById(R.id.sign_in_message_alert_no);
        yesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO save mail and password in Preferences
                SdkPreferencesHelper.getInstance().setUserMail(usernameEditText.getText().toString());
                SdkPreferencesHelper.getInstance().setPassword(passwordEditText.getText().toString());
                finish();
                alertDialog.dismiss();
            }
        });
        noButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                alertDialog.dismiss();
            }
        });
        alertDialog.show();
    }

    public boolean validateEmail(String email) {
        matcher = pattern.matcher(email);
        return matcher.matches();
    }

    public boolean validatePassword(String password) {
        return password.length() > 5;
    }
}
