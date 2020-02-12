package com.clocktower.lullaby.view.list.audio_track;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.clocktower.lullaby.R;
import com.clocktower.lullaby.interfaces.ListItemClickListener;
import com.clocktower.lullaby.model.SongInfo;

import java.io.File;
import java.util.List;

public class MusicTrackVH extends RecyclerView.ViewHolder implements View.OnClickListener {

    View layout;
    TextView songName, artiste;
    ListItemClickListener itemClickListener;

    public MusicTrackVH(@NonNull View itemView) {
        super(itemView);
        initialiseWidgets(itemView);
    }

    public void setItemClickListener(ListItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public void setData (List<SongInfo> songs){
        songName.setText(songs.get(getAdapterPosition()).getSongName());
        artiste.setText(songs.get(getAdapterPosition()).getArtiste());
    }

    private void initialiseWidgets(View v){
        layout = v.findViewById(R.id.layoutItem);
        songName = v.findViewById(R.id.textSongName);
        artiste = v.findViewById(R.id.textArtist);
        layout.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        itemClickListener.onMusicTrackClick(getAdapterPosition());
    }
}