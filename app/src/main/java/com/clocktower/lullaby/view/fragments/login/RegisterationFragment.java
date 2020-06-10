package com.clocktower.lullaby.view.fragments.login;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.ContentLoadingProgressBar;
import androidx.fragment.app.Fragment;

import com.clocktower.lullaby.R;
import com.clocktower.lullaby.model.utilities.Constants;
import com.clocktower.lullaby.model.utilities.GeneralUtil;
import com.clocktower.lullaby.view.activities.Splash;

import java.util.StringTokenizer;

public class RegisterationFragment extends Fragment {

    private static final String TYPE = "type";
    private Splash activity;
    private String getType;
    EditText usernameEditText, passwordEditText, confirmPwd;
    ContentLoadingProgressBar loadingProgressBar;
    Button loginButton;
    TextView title;

    public static RegisterationFragment getInstance(String type) {

        RegisterationFragment fragment = new RegisterationFragment();
        Bundle extra =  new Bundle();
        extra.putString(TYPE, type);
        fragment.setArguments(extra);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_registration, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getType = getArguments().getString(TYPE, null);
        initialiseWidgets(view);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        activity = (Splash) context;
    }

    private void initialiseWidgets(View view) {
        title = view.findViewById(R.id.textTitleReg);
        usernameEditText = view.findViewById(R.id.username);
        passwordEditText = view.findViewById(R.id.password);
        confirmPwd = view.findViewById(R.id.passwordConfirm);
        loginButton = view.findViewById(R.id.login);

        loadingProgressBar = view.findViewById(R.id.loading);
        loadingProgressBar.hide();

        if(getType.equals(Constants.SIGN_IN)){
            title.setText(R.string.enter_login_details);
            confirmPwd.setVisibility(View.GONE);
            loginButton.setText(Constants.SIGN_IN);
        }

        loginButton.setOnClickListener(view1 -> {
            loginButton.setEnabled(false);
                registerOrSignInUser();

        });
    }

    private void registerOrSignInUser(){
        loadingProgressBar.show();
        String pwd = passwordEditText.getText().toString();
        String cPwd = confirmPwd.getText().toString();
        String email = usernameEditText.getText().toString();
        if (email.contains("@")){
            if(getType.equals(Constants.REGISTRATION)) {
               if(pwd.equals(cPwd))
                   activity.registerUserWith(email, pwd);
               else {
                   GeneralUtil.message("Registration Error, check email or password");
                   loginButton.setEnabled(true);
               }
            }
            else
                activity.signInUserWith(email, pwd);

            passwordEditText.setEnabled(false);
            confirmPwd.setEnabled(false);
            usernameEditText.setEnabled(false);
        }else {
            GeneralUtil.message("Error, check email or password");
            loginButton.setEnabled(true);
        }
    }

    public void enableRetry(){
        loadingProgressBar.hide();
        passwordEditText.setEnabled(true);
        confirmPwd.setEnabled(true);
        usernameEditText.setEnabled(true);
        loginButton.setEnabled(true);
    }

}
