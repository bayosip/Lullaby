package com.clocktower.lullaby.view.list.audio_track;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.clocktower.lullaby.interfaces.AudioItemClickListener;
import com.clocktower.lullaby.interfaces.ListItemClickListener;
import com.clocktower.lullaby.R;
import com.clocktower.lullaby.model.SongInfo;

import java.io.File;
import java.util.List;

public class MusicTrackListAdapter extends RecyclerView.Adapter<MusicTrackVH> {

    private List<File> musictracks;
    private List<SongInfo> tracks;
    private AudioItemClickListener listener;

    /*public MusicTrackListAdapter(List<File> musictracks, ListItemClickListener listener) {
        this.musictracks = musictracks;
        this.listener = listener;
    }*/

    public MusicTrackListAdapter(List<SongInfo> tracks, AudioItemClickListener listener) {
        this.tracks = tracks;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MusicTrackVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(listener.getViewContext()).inflate(R.layout.music_list_item,
                parent, false);
        MusicTrackVH holder = new MusicTrackVH(view);
        holder.setItemClickListener(listener);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull MusicTrackVH holder, int position) {
        holder.setData(tracks);
    }

    @Override
    public int getItemCount() {
        return tracks.size();
    }
}
