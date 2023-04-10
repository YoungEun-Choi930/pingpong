package com.example.pingpong.Game;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.pingpong.R;
import com.ventura.bracketslib.BracketsView;
import com.ventura.bracketslib.model.ColomnData;

import java.util.List;
import java.util.Objects;

public class GameTreeActivity extends AppCompatActivity {
    List<ColomnData> matchList;
    BracketsView bracketsView;
    View makeTournament;
    TextView nonetxt, nextmatch;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_tree);

        Toolbar myToolbar = findViewById(R.id.game_tree_toolbar);
        myToolbar.setTitle("대진표");
        setSupportActionBar(myToolbar);//툴바달기
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        bracketsView = findViewById(R.id.game_tree);
        makeTournament = findViewById(R.id.make_tournament);
        nonetxt = findViewById(R.id.game_tree_none_txt);
        nextmatch = findViewById(R.id.game_next_match);

        Intent intent = getIntent();
        matchList = (List<ColomnData>) intent.getSerializableExtra("list");
        bracketsView.setBracketsData(matchList);
        if(matchList.size() == 0) {
            bracketsView.setVisibility(View.GONE);
            nonetxt.setVisibility(View.VISIBLE);
        }

        String str = intent.getStringExtra("nextmatch");
        if(str.equals("")) nextmatch.setVisibility(View.GONE);
        else nextmatch.setText(str);



    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home: //toolbar의 back키 눌렀을 때 동작
                // 액티비티 이동
                finish();
                break;

        }
        return super.onOptionsItemSelected(item);
    }
}