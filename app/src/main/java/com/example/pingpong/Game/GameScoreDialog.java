package com.example.pingpong.Game;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.example.pingpong.R;

import java.util.HashMap;
import java.util.Random;

public class GameScoreDialog extends Dialog implements View.OnClickListener {
    private HashMap<String, Object> info;
    private int curSet, maxSet;
    private EditText score1, score2;
    private TextView setText;
    String groupID, gameID;

    private GameScoreDialogListener listener;

    public GameScoreDialog(Context context, String groupID, String gameID, HashMap<String, Object> info) {
        super(context);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_game_score);
        setCanceledOnTouchOutside(false);   //주변터치방지

        this.groupID =groupID;
        this.gameID = gameID;
        this.info = info;
    }



    interface GameScoreDialogListener {
        void onPosiviceClicked(HashMap<String, Object> info);
    }

    public void setDiaglogListener(GameScoreDialogListener listener) {
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        RadioGroup setNum = findViewById(R.id.game_set_radioGroup);

        TextView name1 = findViewById(R.id.game_score_name1);
        TextView name2 = findViewById(R.id.game_score_name2);
        setText = findViewById(R.id.game_set_text);
        score1 = findViewById(R.id.game_score_enter1);
        score2 = findViewById(R.id.game_score_enter2);

        Button setMBtn = findViewById(R.id.game_set_mbtn);
        setMBtn.setOnClickListener(this);
        Button setPBtn = findViewById(R.id.game_set_pbtn);
        setPBtn.setOnClickListener(this);
        Button del = findViewById(R.id.game_score_del_btn);
        del.setOnClickListener(this);
        Button rand = findViewById(R.id.game_score_random_btn);
        rand.setOnClickListener(this);
        Button back = findViewById(R.id.game_score_back_btn);
        back.setOnClickListener(this);
        Button save = findViewById(R.id.game_score_save_btn);
        save.setOnClickListener(this);

        //이름 설정
        name1.setText((String) info.get("name1"));
        name2.setText((String) info.get("name2"));

        //처음 화면 들어왔을 때 만약 4, 5세트에 정보가 있다면 maxset을 5로 해준다.
        if(info.get("score7") != null | info.get("score8") != null | info.get("score9") != null | info.get("score10") != null)
            maxSet = 5;
        else maxSet = 3;

        //처음 화면 들어오면 무조건 1세트를 띄운다.
        curSet = 1;
        showCurScore(1);

        //세트 3, 5중에 고르면 maxSet을 업데이트, 5->3으로가면 7,8,9,10의 정보 없앰.
        setNum.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(checkedId == R.id.game_set_radio3) {
                    maxSet = 3;
                    info.remove("score7");
                    info.remove("score8");
                    info.remove("score9");
                    info.remove("score10");
                }else { //5
                    maxSet = 5;
                }
            }
        });
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.game_set_mbtn:        //세트 마이너스
                saveCureScore();
                showCurScore(curSet-1);
                break;
            case R.id.game_set_pbtn:       //세트 플러스
                saveCureScore();
                showCurScore(curSet+1);
                break;
            case R.id.game_score_del_btn:   //삭제
                info.put("score1", null);
                info.put("score2", null);
                info.put("score3", null);
                info.put("score4", null);
                info.put("score5", null);
                info.put("score6", null);
                info.put("score7", null);
                info.put("score8", null);
                info.put("score9", null);
                info.put("score10", null);
                info.put("resultscore1", null);
                info.put("resultscore2", null);

                listener.onPosiviceClicked(info);
                dismiss();
                break;
            case R.id.game_score_random_btn:    //랜덤
                random();
                saveResultScore();
                listener.onPosiviceClicked(info);
                dismiss();
                break;
            case R.id.game_score_back_btn:  //뒤로가기
                cancel();
                break;
            case R.id.game_score_save_btn:  //저장
                saveCureScore();
                saveResultScore();
                listener.onPosiviceClicked(info);
                dismiss();
                break;
        }
    }


    public void saveCureScore(){
        if(!score1.getText().toString().equals("")) info.put("score"+(curSet*2-1), Long.parseLong(score1.getText().toString()));
        if(!score2.getText().toString().equals("")) info.put("score"+(curSet*2), Long.parseLong(score2.getText().toString()));
    }

    @SuppressLint("SetTextI18n")
    private void showCurScore(int set) {    // set: 보여줘야하는 세트의 숫자
        if(set == 0 | set > maxSet) return;

        System.out.println(set+"세트");
        setText.setText(set+"세트");
        if(info.get("score"+(set*2-1)) != null) score1.setText(Long.toString((long)info.get("score"+(set*2-1))));
        else score1.setText("");
        if(info.get("score"+(set*2)) != null) score2.setText(Long.toString((long)info.get("score"+(set*2))));
        else score2.setText("");
        curSet = set;
    }

    public void saveResultScore() {
        long result1 = 0;
        long result2 = 0;
        if(info.get("score1") != null & info.get("score2") != null) {
            long s1 = (long) info.get("score1");
            long s2 = (long) info.get("score2");
            if(s1 > s2) result1 = result1+1;
            else if(s1 < s2) result2 = result2+1;
        }
        if(info.get("score3") != null & info.get("score4") != null) {
            long s1 = (long) info.get("score3");
            long s2 = (long) info.get("score4");
            if(s1 > s2) result1 = result1+1;
            else if(s1 < s2) result2 = result2+1;
        }
        if(info.get("score5") != null & info.get("score6") != null) {
            long s1 = (long) info.get("score5");
            long s2 = (long) info.get("score6");
            if(s1 > s2) result1 = result1+1;
            else if(s1 < s2) result2 = result2+1;
        }
        if(info.get("score7") != null & info.get("score8") != null) {
            long s1 = (long) info.get("score7");
            long s2 = (long) info.get("score8");
            if(s1 > s2) result1 = result1+1;
            else if(s1 < s2) result2 = result2+1;
        }
        if(info.get("score9") != null & info.get("score10") != null) {
            long s1 = (long) info.get("score9");
            long s2 = (long) info.get("score10");
            if(s1 > s2) result1 = result1+1;
            else if(s1 < s2) result2 = result2+1;
        }
        info.put("resultscore1", result1);
        info.put("resultscore2", result2);
    }

    private void random() {
        Random random = new Random();

        //1세트
        int rand = random.nextInt(2);   //0또는 1
        int score = random.nextInt(11); //0~10사이 숫자
        if(rand == 0) {
            info.put("score1", (long)11);
            info.put("score2", (long)score);
        } else {
            info.put("score1", (long)score);
            info.put("score2", (long)11);
        }

        //2세트
        rand = random.nextInt(2);   //0또는 1
        score = random.nextInt(11); //0~10사이 숫자
        if(rand == 0) {
            info.put("score3", (long)11);
            info.put("score4", (long)score);
        }else {
            info.put("score3", (long)score);
            info.put("score4", (long)11);
        }

        //3세트
        rand = random.nextInt(2);   //0또는 1
        score = random.nextInt(11); //0~10사이 숫자
        if(rand == 0) {
            info.put("score5", (long)11);
            info.put("score6", (long)score);
        }else {
            info.put("score5", (long)score);
            info.put("score6", (long)11);
        }

    }

}
