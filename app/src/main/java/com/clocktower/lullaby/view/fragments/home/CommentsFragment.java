package com.clocktower.lullaby.view.fragments.home;

import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.clocktower.lullaby.R;
import com.clocktower.lullaby.model.Comments;
import com.clocktower.lullaby.model.utilities.GeneralUtil;
import com.clocktower.lullaby.view.list.comment_list.CommentListAdapter;
import com.skyhope.showmoretextview.ShowMoreTextView;

import java.util.LinkedList;
import java.util.List;

public class CommentsFragment extends BaseFragment{

    private static final String TAG = CommentsFragment.class.getSimpleName();
    private static final String USERNAME = "username";
    private static final String ID = "id";
    private static final String TITLE = "title";
    private RecyclerView allComments;
    private CommentListAdapter adapter;
    private EditText commentText;
    private ShowMoreTextView postTitle;
    private ImageButton sendComment;
    private static List<Comments> comments;
    private String PID, title;

    public static CommentsFragment newInstance(String postID, String title){
        CommentsFragment frag = new CommentsFragment();
        Bundle bundle = new Bundle();
        bundle.putString(ID, postID);
        bundle.putString(TITLE, title);
        frag.setArguments(bundle);
        return frag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.comment_section, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initialisePrequistes();
        PID = getArguments().getString(ID);
        title = getArguments().getString(TITLE);
        initialiseWidget(view);
        listener.retrieveAllComments(PID);
    }

    private void initialisePrequistes(){
        comments = new LinkedList<>();
        adapter = new CommentListAdapter(comments, getActivity());
    }

    private void initialiseWidget(View v) {
        postTitle = v.findViewById(R.id.textPostTitle);
        postTitle.setText(title);
        postTitle.setShowingLine(2);
        postTitle.addShowMoreText("More");
        postTitle.addShowLessText("Less");
        postTitle.setShowMoreColor(Color.MAGENTA); // or other color
        postTitle.setShowLessTextColor(Color.BLACK); // or other color
        allComments = v.findViewById(R.id.recyclerComments);
        LinearLayoutManager layoutManager
                = new LinearLayoutManager(listener.getViewContext(), RecyclerView.VERTICAL,
                false);
        allComments.setLayoutManager(layoutManager);
        allComments.setAdapter(adapter);


        commentText =v.findViewById(R.id.editTextComment);
        sendComment = v.findViewById(R.id.btnSendComment);
        sendComment.setOnClickListener(view -> {
            String msg = commentText.getText().toString();
            if(!TextUtils.isEmpty(msg)){
                listener.postACommentOnPostWithId(PID, msg);
                commentText.setText("");
            }else {
                GeneralUtil.message("Enter Comment Please...");
            }
        });
    }

    public void updateAdapter(final Comments comment){
        GeneralUtil.getHandler().post(() -> {
            comments.add(comment);
            adapter.notifyItemChanged(0);
            scrollToBottom();
        });
    }

    private void scrollToBottom() {
        allComments.scrollToPosition(adapter.getItemCount() - 1);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        listener.restoreViewsAfterLeavingCommentSection();
    }
}
