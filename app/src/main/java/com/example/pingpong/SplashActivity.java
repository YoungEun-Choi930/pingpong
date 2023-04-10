package com.example.pingpong;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.pingpong.Main.MainActivity;
import com.example.pingpong.Main.MainGroupFragment;
import com.example.pingpong.SearchSetting.MainProfileChangeActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class SplashActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // [START initialize_auth]
        // Initialize Firebase Auth
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        // [END initialize_auth]

        //로그인이 되어있으면 홈화면으로 넘어가기
        if (mAuth.getCurrentUser() != null) {
            // setting
            FirebaseUser user = mAuth.getCurrentUser();
            LoginActivity.userUID = user.getUid();

            //DB에 사용자로 등록되어있는지 확인
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("Users").document(user.getUid()).get().addOnCompleteListener(task -> {
                if ((task.getResult()).exists()) {

                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(intent);
                    finish();

                } else {  //정보 없음 -> 정보입력화면으로
                    Intent intent = new Intent(getApplication(), LoginActivity.class);
                    startActivity(intent);
                    finish();
                }

            });


        } else {
            Intent intent = new Intent(getApplication(), LoginActivity.class);
            startActivity(intent);
            finish();
        }
    }
}
