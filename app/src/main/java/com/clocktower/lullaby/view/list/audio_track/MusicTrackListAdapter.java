package com.clocktower.lullaby.view.list.audio_track;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.clocktower.lullaby.interfaces.AudioItemClickListener;
import com.clocktower.lullaby.R;
import com.clocktower.lullaby.model.SongInfo;

import java.io.File;
import java.util.List;

public class MusicTrackListAdapter extends RecyclerView.Adapter<MusicTrackVH> implements AudioItemClickListener.RefreshItem {

    private static final String TAG = "MusicTrackListAdapter";

    private List<File> musictracks;
    private List<SongInfo> tracks;
    private AudioItemClickListener listener;
    private int selectedPos = RecyclerView.NO_POSITION;


    public MusicTrackListAdapter(List<SongInfo> tracks, AudioItemClickListener listener) {
        this.tracks = tracks;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MusicTrackVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(listener.getViewContext()).inflate(R.layout.music_list_item,
                parent, false);
        MusicTrackVH holder = new MusicTrackVH(view, this);
        holder.setItemClickListener(listener);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull MusicTrackVH holder, int position) {
        holder.setData(tracks);
        Log.w(TAG, "onBindViewHolder: pos - " + position + " vs selectedPos - "+ selectedPos  );
        holder.changeItemBackground(position == selectedPos);
    }

    @Override
    public int getItemCount() {
        return tracks.size();
    }

    @Override
    public void setSelectedPosition(int position) {
        selectedPos = position;
    }
}


