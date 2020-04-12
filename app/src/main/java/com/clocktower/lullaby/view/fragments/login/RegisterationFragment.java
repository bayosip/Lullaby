package com.clocktower.lullaby.view.fragments.login;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.ContentLoadingProgressBar;
import androidx.fragment.app.Fragment;

import com.clocktower.lullaby.R;
import com.clocktower.lullaby.model.utilities.GeneralUtil;
import com.clocktower.lullaby.view.activities.Splash;

public class RegisterationFragment extends Fragment {

    private Splash activity;

    public static RegisterationFragment getInstance() {

        RegisterationFragment fragment = new RegisterationFragment();
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
        initialiseWidgets(view);
    }


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        activity = (Splash) context;
    }

    private void initialiseWidgets(View view) {

        final EditText usernameEditText = view.findViewById(R.id.username);
        final EditText passwordEditText = view.findViewById(R.id.password);
        final EditText confirmPwd = view.findViewById(R.id.passwordConfirm);
        final Button loginButton = view.findViewById(R.id.login);

        final ContentLoadingProgressBar loadingProgressBar = view.findViewById(R.id.loading);
        loadingProgressBar.hide();

        loginButton.setOnClickListener(view1 -> {
            loadingProgressBar.show();
            String pwd = passwordEditText.getText().toString();
            String cPwd = confirmPwd.getText().toString();
            String email = usernameEditText.getText().toString();
            if (pwd.equals(cPwd) && email.contains("@")){
                activity.registerUserWith(email, pwd);
                passwordEditText.setText("");
                confirmPwd.setText("");
                usernameEditText.setText("");
            }else {
                GeneralUtil.message("Registration Error, check email or password");
            }
        });
    }

}
