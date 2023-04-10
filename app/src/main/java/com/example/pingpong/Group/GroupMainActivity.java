package com.example.pingpong.Group;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.pingpong.LoginActivity;
import com.example.pingpong.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.HashMap;
import java.util.Objects;

public class GroupMainActivity extends AppCompatActivity {
    private FragmentManager fragmentManager;
    private GroupHomeFragment fragmentGroupHome;
    private GroupGameFragment fragmentGroupGame;
    private GroupMemberFragment fragmentGroupMember;
    private GroupSettingFragment fragmentGroupSetting;
    private FragmentTransaction transaction;

    public static String groupName;

    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_main);

        Intent intent = getIntent();
        String groupID = intent.getStringExtra("groupID");
        String userUID = LoginActivity.userUID;
        HashMap<String, Object> info = (HashMap<String, Object>) intent.getSerializableExtra("groupInfo");
        groupName = (String) info.get("groupName");
        String manager = (String) info.get("managerUID");

        Toolbar myToolbar = (Toolbar) findViewById(R.id.group_toolbar);
        myToolbar.setTitle(groupName);
        setSupportActionBar(myToolbar);//툴바달기
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);


        BottomNavigationView bottomNavigationView = findViewById(R.id.groupNavi);
        bottomNavigationView.setOnItemSelectedListener(menuItem -> {
            switch (menuItem.getItemId()) {
                case R.id.action_groupHome:
                    setFrag(0);
                    break;
                case R.id.action_groupGame:
                    setFrag(1);
                    break;
                case R.id.action_groupMember:
                    setFrag(2);
                    break;
                case R.id.action_groupSetting:
                    setFrag(3);
                    break;
            }
            return true;
        });

        fragmentManager = getSupportFragmentManager();

        fragmentGroupHome = new GroupHomeFragment(groupID,userUID, manager);
        fragmentGroupGame = new GroupGameFragment(groupID, manager, userUID);
        fragmentGroupMember = new GroupMemberFragment(groupID, manager, userUID);
        fragmentGroupSetting = new GroupSettingFragment(groupID, userUID);

        transaction = fragmentManager.beginTransaction();
        setFrag(0);//첫 프래그먼트 화면을 그룹 프래그먼트로
    }


    private void setFrag(int n) {
        fragmentManager = getSupportFragmentManager();
        transaction = fragmentManager.beginTransaction();
        switch (n) {
            case 0:
                transaction.replace(R.id.frameLayout, fragmentGroupHome);
                transaction.commit();
                break;
            case 1:
                transaction.replace(R.id.frameLayout, fragmentGroupGame);
                transaction.commit();
                break;
            case 2:
                transaction.replace(R.id.frameLayout, fragmentGroupMember);
                transaction.commit();
                break;
            case 3:
                transaction.replace(R.id.frameLayout, fragmentGroupSetting);
                transaction.commit();
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == android.R.id.home) {//뒤로가기 버튼
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void setActionBarTitle(String title) {
        Toolbar myToolbar = findViewById(R.id.group_toolbar);
        myToolbar.setTitle(title);
        setSupportActionBar(myToolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
    }




}