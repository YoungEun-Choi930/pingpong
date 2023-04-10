package com.example.pingpong.SearchSetting;

import android.annotation.SuppressLint;
import android.net.Uri;
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

public class PostFragment extends Fragment {
    private final String userUID, userName;
    private final Uri userImage;
    RecyclerView recyclerView;
    MyPostAdapter adapter;
    List<String> myPostList;
    TextView nopost;

    public PostFragment(String userUID, String userName, Uri userImage) {
        this.userUID = userUID;
        this.myPostList = new ArrayList();
        this.userName = userName;
        this.userImage = userImage;
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_post, container, false);

        nopost = view.findViewById(R.id.nopost);
        adapter = new MyPostAdapter(myPostList, userUID, userName, userImage);

        recyclerView = view.findViewById(R.id.recy_mypost);
        recyclerView.setAdapter(adapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);

        return view;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updatePost() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        myPostList = new ArrayList();

        db.collection("Users").document(userUID).get().addOnCompleteListener(task -> {
            DocumentSnapshot document = task.getResult();
            assert document != null;
            myPostList = (List<String>) document.getData().get("postList");
            if (myPostList != null) {
                Collections.reverse(myPostList);
                adapter.setList(myPostList);
                adapter.notifyDataSetChanged();
                if (myPostList.size() == 0)
                    nopost.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        updatePost();
    }
}
