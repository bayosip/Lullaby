package com.clocktower.lullaby.view.fragments.home;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.clocktower.lullaby.R;
import com.clocktower.lullaby.interfaces.AudioItemClickListener;
import com.clocktower.lullaby.interfaces.FragmentListener;
import com.clocktower.lullaby.interfaces.ListItemClickListener;
import com.clocktower.lullaby.model.SongInfo;
import com.clocktower.lullaby.model.utilities.GeneralUtil;
import com.clocktower.lullaby.view.activities.Home;
import com.clocktower.lullaby.view.list.audio_track.MusicTrackListAdapter;

import java.util.ArrayList;
import java.util.List;

public class MusicSelectorDialog extends DialogFragment implements AudioItemClickListener {

    private RecyclerView musicList;
    private Button select;
    private FragmentListener listener;
    private MusicTrackListAdapter adapter;
    private static List<SongInfo> audioFiles;
    private SongInfo audio;
    private ConstraintLayout layout;

    static {
        audioFiles = new ArrayList<>();
    }

    public static void setAudioFiles(List<SongInfo> audioFiles) {
        if (audioFiles!= null && !audioFiles.isEmpty()) {
            Log.w("dialog Songs", audioFiles.get(0).getTrackName());
            MusicSelectorDialog.audioFiles.clear();
            MusicSelectorDialog.audioFiles.addAll(audioFiles);
        }
    }

    public static MusicSelectorDialog getInstance(){
        MusicSelectorDialog dialog = new MusicSelectorDialog();
        return  dialog;
    }
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        listener =  (FragmentListener) context;
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
        layout = view.findViewById(R.id.dialog_layout);

        LinearLayoutManager layoutManager = new LinearLayoutManager(listener.getViewContext(),
                RecyclerView.VERTICAL, false);
        DividerItemDecoration itemDecoration = new DividerItemDecoration(musicList.getContext(),
                layoutManager.getOrientation());
        adapter = new MusicTrackListAdapter(audioFiles, this);
        //musicList.addItemDecoration(itemDecoration);
        musicList.setLayoutManager(layoutManager);
        musicList.setAdapter(adapter);

        select.setOnClickListener(view1 -> {
            if (audio!=null)
                listener.playSelectedAudio(audio);
            else GeneralUtil.message("Select An Audio File");
        });
    }

    public void show(FragmentManager manager){
        try {
            if(!isStateSaved()){
                show(manager, "Track Selector");
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
       Window window = getDialog().getWindow();
        if(window == null) return;
        window.setLayout(WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT);
    }

    @Override
    public Home getViewContext() {
        return listener.getViewContext();
    }

    @Override
    public void onMusicTrackClick(int position) {

        audio =audioFiles.get(position);
        adapter.notifyDataSetChanged();
    }
}
