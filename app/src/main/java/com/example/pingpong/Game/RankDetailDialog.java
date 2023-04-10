package com.example.pingpong.Game;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pingpong.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;

public class RankDetailDialog extends Dialog implements View.OnClickListener {
    GameTableLeagueItemAdapter adapter;
    RecyclerView recy;

    public RankDetailDialog(@NonNull Context context, GameTableLeagueItemAdapter adapter) {
        super(context);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_rank_score);
        setCanceledOnTouchOutside(false);   //주변터치방지
        this.adapter = adapter;

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.rank_y:
                dismiss();
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Button yes = findViewById(R.id.rank_y);
        yes.setOnClickListener(this);
        recy = findViewById(R.id.recy_rank_detail);
        recy.setAdapter(adapter);
        recy.setLayoutManager(new LinearLayoutManager(getContext()));

        ImageView help = findViewById(R.id.game_help);

        String helpStr = "순위 기준\n1) 승점이 많은 경우\n" +
                "2) 동점자간 세트득실율이 큰 경우\n" +
                "3) 동점자간 점수득실율이 큰 경우\n\n" +
                "승점 \n= (승게임 수 * 2) + 패게임 수\n" +
                "세트득실률 \n= (승세트 수 합계) / (패세트 수 합계)\n" +
                "점수득실률 \n= (얻은 점수 합계) / (잃은 점수 합계)";

        help.setOnClickListener(v -> {
            AlertDialog.Builder alt_bld = new AlertDialog.Builder(v.getContext());
            alt_bld.setMessage(helpStr).setCancelable(false)
                    .setNegativeButton("확인", (dialogInterface, i) -> dialogInterface.cancel());
            AlertDialog alert = alt_bld.create();
            alert.setTitle("도움말");
            alert.show();
        });


    }
}
