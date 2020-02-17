package com.clocktower.lullaby.view.list.comment_list;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.clocktower.lullaby.R;
import com.clocktower.lullaby.model.Comments;

import java.util.List;

public class CommentListAdapter extends RecyclerView.Adapter<CommentListViewHolder> {


    private static final String TAG = "Comments";
    private CommentListViewHolder holder;
    private List<Comments> comments;
    private LayoutInflater inflater;

    public CommentListAdapter(List<Comments> comments, Activity context) {
        this.comments = comments;
        this.inflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public CommentListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.comment_line, parent, false);
        holder = new CommentListViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull CommentListViewHolder holder, int position) {
        holder.setComments(comments);
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }
}
