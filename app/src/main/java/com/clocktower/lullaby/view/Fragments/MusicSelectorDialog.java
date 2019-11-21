package com.clocktower.lullaby.view.Fragments;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.clocktower.lullaby.R;
import com.clocktower.lullaby.view.activities.Alarm;
import com.clocktower.lullaby.view.list.MusicTrackListAdapter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MusicSelectorDialog extends DialogFragment {

    private RecyclerView musicList;
    private Button select;
    private Alarm activity;
    private MusicTrackListAdapter adapter;
    private List<File> audioFiles;
    private boolean isShowing = false;


    public static MusicSelectorDialog getInstance(){
        MusicSelectorDialog dialog = new MusicSelectorDialog();
        return  dialog;
    }
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        activity =  (Alarm) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.track_selector_dialog, container, true);
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        setCancelable(true);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initialiseWidgets(view);
    }

    private void initialiseWidgets(View view){
        musicList = view.findViewById(R.id.listTracks);
        select = view.findViewById(R.id.buttonSelect);

        LinearLayoutManager layoutManager = new LinearLayoutManager(activity,
                RecyclerView.VERTICAL, false);
        DividerItemDecoration itemDecoration = new DividerItemDecoration(musicList.getContext(),
                layoutManager.getOrientation());
        musicList.addItemDecoration(itemDecoration);
        musicList.setAdapter(adapter);

    }

    public void show(FragmentManager manager, List<File> myAudioFiles){

        try {
            if(!isStateSaved()){
                isShowing = true;
                audioFiles = new ArrayList<>();
                audioFiles.addAll(myAudioFiles);
                adapter = new MusicTrackListAdapter(audioFiles, activity);
                show(manager, "Track Selector");
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }



    public void updateListOfAudioFiles(final List<File> files){

    }
}
