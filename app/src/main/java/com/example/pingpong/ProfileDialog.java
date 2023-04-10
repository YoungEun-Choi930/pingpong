package com.example.pingpong;

import android.app.Dialog;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;

import java.util.HashMap;

public class ProfileDialog extends Dialog implements View.OnClickListener{
    ImageView xButton;
    ImageView profile;
    TextView name, degree, team, gender, winlose, percent;
    HashMap<String,Object> map;
    public ProfileDialog(@NonNull Context context, HashMap<String, Object> map) {
        super(context);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_profile);
        setCanceledOnTouchOutside(false);   //주변터치방지
        this.map = map;

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_notification_del:
                dismiss();
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        xButton = findViewById(R.id.btn_notification_del);
        xButton.setOnClickListener(this);
        profile = findViewById(R.id.dialog_image);
        name = findViewById(R.id.name);
        degree = findViewById(R.id.degree);
        team = findViewById(R.id.team);
        gender = findViewById(R.id.gender);
        winlose = findViewById(R.id.winlose);
        percent = findViewById(R.id.percent);

        String strname = (String) map.get("name");
        String strdegree = (String) map.get("degree");
        String strteam = (String) map.get("team");
        String strgender = (String) map.get("gender");
        Uri image = (Uri) map.get("image");
        if(strgender.equals("1")) strgender = "여";
        else if(strgender.equals("0")) strgender = "남";
        else strgender = "비공개";
        String strwinlose = (String) map.get("winlose");
        String strpercent = (String) map.get("percent");

        Glide.with(getContext()).load(image).into(profile);
        name.setText(strname);
        degree.setText(strdegree);
        team.setText(strteam);
        gender.setText(strgender);
        winlose.setText(strwinlose);
        percent.setText(strpercent);

    }
}
