package com.example.pingpong.Group;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.example.pingpong.R;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class GroupMemberFragment extends Fragment {
    RecyclerView member, waitingMember;
    public static GroupGameParticiAdapter memberAdapter, waitingMemberAdapter;
    LinearLayout waitlayout;

    private final String groupID, managerUID, userUID;
    public static List<String> list;
    public static List<String> waitingList;
    RequestManager requestManager;
    public GroupMemberFragment(String group, String manager, String user) {
        this.groupID = group;
        this.managerUID = manager;
        this.userUID = user;

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Groups").document(groupID).get()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();

                        assert document != null;
                        list = (List) document.getData().get("people");
                        waitingList = (List) document.getData().get("waiting");
                        if(waitingList == null) waitingList = new ArrayList();
                    }
                });
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestManager = Glide.with(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_group_member, container, false);

        waitlayout = view.findViewById(R.id.group_member_wait);
        if(userUID.equals(managerUID)) {
            waitlayout.setVisibility(View.VISIBLE);
        }

        member = view.findViewById(R.id.group_member_recy);
        memberAdapter = new GroupGameParticiAdapter(list, false, groupID, "", true, requestManager);
        memberAdapter.setManager(managerUID);
        member.setAdapter(memberAdapter);

        waitingMember = view.findViewById(R.id.group_member_waiting_recy);
        waitingMemberAdapter = new GroupGameParticiAdapter(waitingList, false, groupID, "", true, requestManager);
        waitingMemberAdapter.setAccept();
        waitingMember.setAdapter(waitingMemberAdapter);

        return view;
    }

    @SuppressLint("NotifyDataSetChanged")
    public static void notiallrecy(String userUID) {
        list.add(userUID);
        memberAdapter.notifyDataSetChanged();

        waitingList.remove(userUID);
        waitingMemberAdapter.notifyDataSetChanged();
    }
}