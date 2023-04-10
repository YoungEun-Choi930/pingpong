package com.example.pingpong.SearchSetting;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;

import com.example.pingpong.LoginActivity;
import com.example.pingpong.R;
import com.google.android.material.tabs.TabLayout;

public class MyWritingActivity extends AppCompatActivity {
    FragmentTransaction transaction;
    Fragment fragmentP,fragmentC;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_writing);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.mywriting_toolbar);
        myToolbar.setTitle("내가 쓴 글");
        setSupportActionBar(myToolbar);//툴바달기
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        String userUID = LoginActivity.userUID;
        String userName = intent.getStringExtra("userName");
        Uri userImage = intent.getParcelableExtra("userImage");


        fragmentP = new PostFragment(userUID, userName, userImage);
        fragmentC = new CommentFragment(userUID, userName);

        transaction = getSupportFragmentManager().beginTransaction();

        transaction.add(R.id.frame, fragmentP);
        transaction.add(R.id.frame, fragmentC);

        transaction.hide(fragmentC);
        transaction.show(fragmentP);
        transaction.commit();


        TabLayout tabs = findViewById(R.id.tab_layout);
        tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                transaction = getSupportFragmentManager().beginTransaction();

                if(position == 0){
                    transaction.hide(fragmentC);
                    transaction.show(fragmentP);
                }
                else if (position == 1){
                    transaction.hide(fragmentP);
                    transaction.show(fragmentC);
                }
                transaction.commit();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }
            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {//toolbar의 back키 눌렀을 때 동작
            // 액티비티 이동
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}