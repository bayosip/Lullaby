package com.clocktower.lullaby.view.list.audio_track;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.clocktower.lullaby.R;
import com.clocktower.lullaby.interfaces.AudioItemClickListener;
import com.clocktower.lullaby.interfaces.ListItemClickListener;
import com.clocktower.lullaby.model.SongInfo;

import java.util.List;

public class MusicTrackVH extends RecyclerView.ViewHolder implements View.OnClickListener {

    View layout;
    TextView songName, artiste;
    AudioItemClickListener itemClickListener;
    AudioItemClickListener.RefreshItem refreshItem;

    public MusicTrackVH(@NonNull View itemView, AudioItemClickListener.RefreshItem refreshItem) {
        super(itemView);
        this.refreshItem = refreshItem;
        initialiseWidgets(itemView);
    }

    public void setItemClickListener(AudioItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public void setData (List<SongInfo> songs){
        songName.setText(songs.get(getAdapterPosition()).getTrackName());
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
        refreshItem.setSelectedPosition( getAdapterPosition());
        itemClickListener.onMusicTrackClick(getAdapterPosition());

    }

    public void changeItemBackground(boolean shouldChange){
        if (shouldChange)
            layout.setBackgroundResource(R.color.colorSelect);
        else {
            layout.setBackgroundResource(R.color.app_background_white);
        }
    }
}
