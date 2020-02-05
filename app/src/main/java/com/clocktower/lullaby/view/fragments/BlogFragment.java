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

import java.util.Arrays;
import java.util.List;

public class BlogFragment extends BaseFragment {

    private RecyclerView blog;
    private BlogListAdapter adapter;
    private List<Post> posts;
    private static Post postsarr[];


    static{
        postsarr= new Post[5];
        Post post = new Post("Magic Mugs",
                "https://firebasestorage.googleapis.com/v0/b/lullaby-7a5b3.appspot.com/o/" +
                        "magic%20mugs.mp4?alt=media&token=6ac36223-805d-42fb-8441-036698110a0e");
        Arrays.fill(postsarr, post);
    }

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
        posts = Arrays.asList(postsarr);
        blog = view.findViewById(R.id.post_list);
        LinearLayoutManager layoutManager = new LinearLayoutManager(listener.getListenerContext(),
                RecyclerView.VERTICAL, false);

        adapter = new BlogListAdapter(posts, listener.getListenerContext(), listener.getVideoMediaController());
        //musicList.addItemDecoration(itemDecoration);
        blog.setLayoutManager(layoutManager);
        blog.setAdapter(adapter);
    }
}
