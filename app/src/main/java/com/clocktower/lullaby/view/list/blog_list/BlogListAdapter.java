package com.clocktower.lullaby.view.list.blog_list;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.clocktower.lullaby.R;
import com.clocktower.lullaby.model.Post;

import java.util.List;

public class BlogListAdapter extends RecyclerView.Adapter<BlogVH> {


    List<Post> blogPosts;
    Context context;

    public BlogListAdapter(List<Post> blogPosts, Context context) {
        this.blogPosts = blogPosts;
        this.context = context;
    }

    @NonNull
    @Override
    public BlogVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.video_blog_item,parent, false);
        BlogVH holder =  new BlogVH(view);
        //holder.setListener(itemListener);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull BlogVH holder, int position) {
        holder.setBlogItems(blogPosts);
    }

    @Override
    public int getItemCount() {
        return blogPosts.size();
    }
}
