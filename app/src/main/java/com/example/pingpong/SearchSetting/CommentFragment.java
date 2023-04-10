package com.example.pingpong.SearchSetting;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pingpong.R;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CommentFragment extends Fragment {
    private final String userUID, userName;
    RecyclerView recyclerView;
    MyCommentAdapter adapter;
    List<String> myCommentList;
    TextView nocomment;

    public CommentFragment(String userUID, String userName) {
        this.userUID = userUID;
        this.userName = userName;
        this.myCommentList = new ArrayList();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_comment, container, false);
        adapter = new MyCommentAdapter(myCommentList, userUID, userName);

        nocomment = view.findViewById(R.id.nocomment);
        recyclerView = view.findViewById(R.id.recy_mycomment);
        recyclerView.setAdapter(adapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);

        return view;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateComment() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        myCommentList = new ArrayList();
        db.collection("Users").document(userUID).get().addOnCompleteListener(task -> {
            DocumentSnapshot document = task.getResult();
            myCommentList = (List<String>) document.getData().get("commentList");
            if (myCommentList != null) {
                Collections.reverse(myCommentList);
                adapter.setList(myCommentList);
                adapter.notifyDataSetChanged();
                if (myCommentList.size() == 0)
                    nocomment.setVisibility(View.VISIBLE);
            } else nocomment.setVisibility(View.VISIBLE);
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        updateComment();
    }
}
