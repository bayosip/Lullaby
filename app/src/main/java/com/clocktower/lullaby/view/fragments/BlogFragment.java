package com.clocktower.lullaby.view.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.clocktower.lullaby.R;
import com.clocktower.lullaby.model.Post;
import com.clocktower.lullaby.view.list.blog_list.BlogListAdapter;

import java.util.List;

public class BlogFragment extends BaseFragment {

    private RecyclerView blog;
    private BlogListAdapter adapter;
    private List<Post> posts;

    public static BlogFragment getInstance(){
        BlogFragment fragment = new BlogFragment();
        return  fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_blog, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initialiseWidgets(view);
    }

    private void initialiseWidgets(View view){

        blog = view.findViewById(R.id.post_list);
        LinearLayoutManager layoutManager = new LinearLayoutManager(listener.getListenerContext(),
                RecyclerView.VERTICAL, false);

        adapter = new BlogListAdapter(posts, listener.getListenerContext());
        //musicList.addItemDecoration(itemDecoration);
        blog.setLayoutManager(layoutManager);
        blog.setAdapter(adapter);
    }
}
