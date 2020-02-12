package com.clocktower.lullaby.view.fragments.login;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.clocktower.lullaby.R;
import com.clocktower.lullaby.model.utilities.Constants;
import com.clocktower.lullaby.view.activities.Splash;

import de.hdodenhof.circleimageview.CircleImageView;

public class Profile_creation_frag extends Fragment implements View.OnClickListener {

    private static final String NAME = "Name";
    private Splash activity;
    private ImageButton addPicture;
    private CircleImageView profilePic;
    private Button mContinue;
    private TextView username;
    private String getName;
    

    public static Profile_creation_frag getInstance(String name){
        Profile_creation_frag fragment = new Profile_creation_frag();
        Bundle extra =  new Bundle();
        extra.putString(NAME, name );
        fragment.setArguments(extra);
        return  fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_alarm_scheduler, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getName = getArguments().getString(NAME);
        initialiseWidgets(view);
    }



    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        activity = (Splash)context;
    }

    private void initialiseWidgets(View view){
        addPicture = view.findViewById(R.id.buttonAddImage);
        username = view.findViewById(R.id.text_user_name_crtn);
        username.setText(getName);
        profilePic= view.findViewById(R.id.imageViewID);
        mContinue = view.findViewById(R.id.buttonContinueHome);
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.buttonAddImage:
                break;
            case R.id.buttonContinueHome:
                activity.startHomeActivity();
                break;
        }
    }
}
