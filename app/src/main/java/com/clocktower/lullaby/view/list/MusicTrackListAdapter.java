package com.clocktower.lullaby.view.list;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.clocktower.lullaby.interfaces.ListItemClickListener;
import com.clocktower.lullaby.R;

import java.io.File;
import java.util.List;

public class MusicTrackListAdapter extends RecyclerView.Adapter<MusicTrackVH> {

    private List<File> musictracks;
    private ListItemClickListener listener;

    public MusicTrackListAdapter(List<File> musictracks, ListItemClickListener listener) {
        this.musictracks = musictracks;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MusicTrackVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(listener.getListenerContext()).inflate(R.layout.music_list_item, parent);
        MusicTrackVH holder = new MusicTrackVH(view);
        holder.setItemClickListener(listener);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull MusicTrackVH holder, int position) {
        holder.setData(musictracks);
    }

    @Override
    public int getItemCount() {
        return musictracks.size();
    }
}
