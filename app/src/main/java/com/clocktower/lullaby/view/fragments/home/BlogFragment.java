package com.clocktower.lullaby.view.fragments.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.clocktower.lullaby.R;
import com.clocktower.lullaby.model.CozaBlog;
import com.clocktower.lullaby.model.Post;
import com.clocktower.lullaby.view.list.blog_list.BlogListAdapter;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class BlogFragment extends BaseFragment {

    private static final String NAME = "Name";
    private RecyclerView blog;
    private BlogListAdapter adapter;
    private List<CozaBlog> posts;
    private String getName;
    private Boolean reachedBottom = false;
    private List<String> previousPostIds = new LinkedList<>();


    public static BlogFragment getInstance(String name){
        BlogFragment fragment = new BlogFragment();
        Bundle extra =  new Bundle();
        extra.putString(NAME, name );
        fragment.setArguments(extra);
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
        getName = getArguments().getString(NAME);
        posts = new LinkedList<>();
        initialiseWidgets(view);
        listener.startLoadingPostsFromFirebase();
    }

    private void initialiseWidgets(View view) {
        //posts = Arrays.asList(postsarr);
        blog = view.findViewById(R.id.post_list);
        blog.setItemViewCacheSize(3);
        LinearLayoutManager layoutManager = new LinearLayoutManager(listener.getViewContext(),
                RecyclerView.VERTICAL, false);

        adapter = new BlogListAdapter(posts, listener.getViewContext(), listener.getVideoMediaController());
        //musicList.addItemDecoration(itemDecoration);
        blog.setLayoutManager(layoutManager);
        blog.setAdapter(adapter);
        blog.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                reachedBottom = !recyclerView.canScrollVertically(1);
                if (reachedBottom) {
                    listener.loadMorePost();
                }
            }
        });
    }

    public void updateAdapter(final Post post)
    {if (!previousPostIds.contains(post.postId)) {
        CozaBlog blog = new CozaBlog(post);
        if(reachedBottom) {
            posts.add(blog);
            previousPostIds.add(post.postId);
            //Collections.sort(posts, CozaBlog.blogPostComparator);
        } else {
            posts.add(0, blog);
        }
        adapter.notifyDataSetChanged();
    }
    }

    public void clearList() {
        posts.clear();
    }

    public void updateCommentCount(String postId, int count){
        int i = 0;
        for(Iterator<CozaBlog>it = posts.iterator(); it.hasNext();){
            CozaBlog cb = it.next();
            if(cb.getPost().postId.equals(postId)){
                posts.get(i).setCommentCount(count);
            }
            i++;
        }

        if(adapter!=null)adapter.notifyItemChanged(i);
    }

    public void updateLikeCount(String postId, int count) {
        int i = 0;
        for(Iterator<CozaBlog>it = posts.iterator(); it.hasNext();){
            CozaBlog cb = it.next();
            if(cb.getPost().postId.equals(postId)){
                posts.get(i).setLikeCount(count);
            }
            i++;
        }

        if(adapter!=null)adapter.notifyItemChanged(i);
    }

    public void updateLikeBtnImg(String id, boolean exists) {
        int i = 0;
        for(Iterator<CozaBlog>it = posts.iterator(); it.hasNext();){
            CozaBlog cb = it.next();
            if(cb.getPost().postId.equals(id)){
                posts.get(i).setLiked(exists);
            }
            i++;
        }
        if(adapter!=null)adapter.notifyItemChanged(i);
    }
}
