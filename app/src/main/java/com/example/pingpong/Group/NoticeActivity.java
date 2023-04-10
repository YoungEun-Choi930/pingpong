package com.example.pingpong.Group;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.example.pingpong.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class NoticeActivity extends AppCompatActivity {
    RecyclerView recy_notice;
    NoticeAdapter adapter;
    List<HashMap<String, Object>> noticeList;
    private String groupID, manager,userUID;
    RequestManager requestManager;
    @SuppressLint("NotifyDataSetChanged")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notice);


        requestManager = Glide.with(this);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(myToolbar);//툴바달기
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        this.noticeList = new ArrayList();
        Intent intent = getIntent();
        noticeList = (ArrayList) intent.getSerializableExtra("noticeList");
        groupID = intent.getStringExtra("groupID");
        manager = intent.getStringExtra("manager");
        userUID = intent.getStringExtra("userUID");
        adapter = new NoticeAdapter(noticeList,groupID,manager,userUID, requestManager);
        recy_notice = findViewById(R.id.recy_notice);
        recy_notice.setAdapter(adapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL,false);
        recy_notice.setLayoutManager(layoutManager);

        adapter.notifyDataSetChanged();


    }


    @SuppressLint("NotifyDataSetChanged")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        adapter.notifyDataSetChanged();
        return super.onOptionsItemSelected(item);
    }


    }