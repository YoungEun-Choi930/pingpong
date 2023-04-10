package com.example.pingpong.SearchSetting;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.example.pingpong.Group.GroupInfoActivity;
import com.example.pingpong.LoginActivity;
import com.example.pingpong.R;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class MyApplicationActivity extends AppCompatActivity {
    RecyclerView recy;
    MainSearchAdapter adapter;
    TextView none;

    @SuppressLint("NotifyDataSetChanged")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_application);

        Toolbar myToolbar = findViewById(R.id.myapplication_toolbar);
        myToolbar.setTitle("가입신청중인 탁구장");
        setSupportActionBar(myToolbar);//툴바달기
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);


        List<HashMap<String, Object>> waitingList = new ArrayList();
        recy = findViewById(R.id.myapplication_recy);
        RequestManager requestManager = Glide.with(this);
        adapter = new MainSearchAdapter(waitingList, null, requestManager);
        recy.setAdapter(adapter);
        none = findViewById(R.id.myapplication_none);

        String userUID = LoginActivity.userUID;
        List<String> waitinglist = MainSearchFragment.userWaitingList;
        if(waitinglist.size() == 0) none.setVisibility(View.VISIBLE);

        // 그룹 정보 들고오기
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        for (String groupID: waitinglist) {
            db.collection("Groups").document(groupID).get()
                    .addOnCompleteListener(task -> {
                        DocumentSnapshot document = task.getResult();
                        assert document != null;
                        String groupName = document.getString("name");
                        String comment = document.getString("comment");
                        long member = document.getLong("member");
                        String managerUID = document.getString("manager");
                        long jointype = document.getLong("type");

                        HashMap<String, Object> map = new HashMap<>();
                        map.put("groupID", groupID);
                        map.put("groupName", groupName);
                        map.put("comment", comment);
                        map.put("member", member);
                        map.put("manager", managerUID);
                        map.put("type", jointype);

                        FirebaseStorage storage = FirebaseStorage.getInstance();
                        StorageReference reference = storage.getReference("group_img/"+groupID+"/main.jpg");
                        reference.getDownloadUrl().addOnSuccessListener(uri -> {
                            map.put("Uri", uri);
                            waitingList.add(map);
                            adapter.notifyDataSetChanged();
                        });
                    });

        }


        // 그룹 클릭하면 그룹인포로
        adapter.setOnItemClickListener((v, pos) -> {
            HashMap<String, Object> groupInfo = (HashMap<String, Object>) adapter.filterList.get(pos);
            String groupID = (String) groupInfo.get("groupID");

            Intent intent = new Intent(getApplicationContext(), GroupInfoActivity.class);
            intent.putExtra("groupID", groupID);
            intent.putExtra("userUID", userUID);
            intent.putExtra("groupInfo", groupInfo);
            intent.putExtra("state", true);

            startActivity(intent);

        });


    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == android.R.id.home) {//뒤로가기 버튼
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}