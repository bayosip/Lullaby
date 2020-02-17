package com.clocktower.lullaby.view.fragments.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.clocktower.lullaby.R;

public class ChatFragment extends BaseFragment {

    private static final String NAME = "Name";
    private EditText chatBox;
    private ImageButton sendChat;
    private String getName;

    public static ChatFragment getInstance(String name){
        ChatFragment fragment = new ChatFragment();
        Bundle extra =  new Bundle();
        extra.putString(NAME, name );
        fragment.setArguments(extra);
        return  fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getName = getArguments().getString(NAME);
        initialiseWidgets(view);
    }

    private void initialiseWidgets(View view){
        chatBox = view.findViewById(R.id.editTextChat);
        sendChat = view.findViewById(R.id.btnSendChat);
    }
}
