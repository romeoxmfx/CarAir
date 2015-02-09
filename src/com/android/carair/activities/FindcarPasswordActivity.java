
package com.android.carair.activities;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.android.carair.R;
import com.android.carair.utils.Util;
import com.android.carair.views.PasswordInputView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class FindcarPasswordActivity extends SherlockFragmentActivity {
    TextView tvPassword;
    PasswordInputView passwordInput;
    public static final int PASSWORD_STATE_SET = 0;
    public static final int PASSWORD_STATE_INPUT = 1;
    public static final String PASSWROD_KEY = "pasword_state";
    private int currentState = PASSWORD_STATE_SET;
    Button btCanclePassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.carair_findcar_password_activity);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setBackgroundDrawable(
                getResources().getDrawable(R.drawable.actionbar_background));
        getSupportActionBar().setTitle("寻车密码");
        tvPassword = (TextView) findViewById(R.id.tvPassword);
        btCanclePassword = (Button) findViewById(R.id.btCanclePassword);
        btCanclePassword.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Util.setFindCarSafe(FindcarPasswordActivity.this, false);
                Util.setFindCarPassword(FindcarPasswordActivity.this, "");
                finish();
            }
        });
        passwordInput = (PasswordInputView) findViewById(R.id.passInput);
        Bundle bundle = getIntent().getExtras();
        if (bundle != null && bundle.containsKey(PASSWROD_KEY)) {
            this.currentState = bundle.getInt(PASSWROD_KEY);
        }
        if (currentState == PASSWORD_STATE_SET) {
            tvPassword.setText("请输入新密码");
        } else {
            tvPassword.setText("请输入密码");
            btCanclePassword.setVisibility(View.GONE);
        }
        passwordInput.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String text = passwordInput.getText().toString();
                if (!TextUtils.isEmpty(text) && text.length() == 4) {
                    // passwordInput.setText("");
                    if (currentState == PASSWORD_STATE_SET) {
                        Util.setFindCarSafe(FindcarPasswordActivity.this, true);
                        Util.setFindCarPassword(FindcarPasswordActivity.this, text);
                        Toast.makeText(FindcarPasswordActivity.this, "密码设定成功", 1).show();
                        finish();
                    } else {
                        if (text.equals(Util.getFindCarPassword(FindcarPasswordActivity.this))) {
                            startActivity(new Intent(FindcarPasswordActivity.this,
                                    MapActivity.class));
                            finish();
                        } else {
                            passwordInput.setText("");
                            Toast.makeText(FindcarPasswordActivity.this, "密码输入错误，请重新输入", 1).show();
                        }
                    }
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        passwordInput.setFocusable(true);
        passwordInput.setFocusableInTouchMode(true);
        passwordInput.requestFocus();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                this.finish();
                break;

            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                InputMethodManager inputManager = (InputMethodManager) passwordInput.getContext()
                        .getSystemService(
                                Context.INPUT_METHOD_SERVICE);
                inputManager.showSoftInput(passwordInput, 0);
            }
        }, 700);
    }
}
