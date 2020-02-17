package com.clocktower.lullaby.view.list.blog_list;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.MediaController;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.clocktower.lullaby.R;
import com.clocktower.lullaby.interfaces.FragmentListener;
import com.clocktower.lullaby.model.CozaBlog;
import com.clocktower.lullaby.model.Post;

import java.util.List;

public class BlogListAdapter extends RecyclerView.Adapter<BlogVH> {


    List<CozaBlog> blogPosts;
    Context context;
    MediaController mediaController;
    FragmentListener listener;

    public BlogListAdapter(List<CozaBlog> blogPosts, FragmentListener listener, MediaController mediaController) {
        this.blogPosts = blogPosts;
        this.context = listener.getListenerContext();
        this.mediaController = mediaController;
        this.listener = listener;
    }

    @NonNull
    @Override
    public BlogVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.media_blog_item,parent, false);
        BlogVH holder =  new BlogVH(view);
        holder.setListener(listener);
        //holder.setListener(itemListener);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull BlogVH holder, int position) {
        holder.setBlogItems(blogPosts);
        holder.setMediaController(mediaController);
    }

    @Override
    public int getItemCount() {
        return blogPosts.size();
    }
}
