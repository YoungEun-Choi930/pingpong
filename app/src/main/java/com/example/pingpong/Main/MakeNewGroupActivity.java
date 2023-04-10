package com.example.pingpong.Main;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.example.pingpong.LoginActivity;
import com.example.pingpong.R;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class MakeNewGroupActivity extends AppCompatActivity {
    int REQUEST_IMAGE_CODE = 1001;
    int REQUEST_EXTERNAL_STORAGE_PERMISSION = 1002;
    int REQUEST_CROP_PICTURE = 1003;
    EditText groupName, groupComment;
    ImageView groupImage, changeImage;
    RadioGroup groupType;
    String groupID;
    boolean ischange;
    byte[] image;
    HashMap<String, Object> buisnessInfo;
    MakeNewGroupActivity.ProgressDialog progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_make_new_group);

        Toolbar myToolbar = findViewById(R.id.newgroup_toolbar);
        setSupportActionBar(myToolbar);//툴바달기
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        //권한 요청
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        REQUEST_EXTERNAL_STORAGE_PERMISSION);
            }
        }
        groupName = findViewById(R.id.newgroup_name);
        groupComment = findViewById(R.id.newgroup_comment);
        groupImage = findViewById(R.id.newgroup_image);
        changeImage = findViewById(R.id.newgroup_change_image);
        changeImage.setOnClickListener(view -> {
            Intent in = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(in, REQUEST_CROP_PICTURE);
        });
        groupType = findViewById(R.id.newgroup_type);
        //로딩창
        progress = new ProgressDialog(this);
        progress.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));  //배경 투명

        Intent intent = getIntent();
        ischange = intent.getBooleanExtra("ischange", false);
        if (ischange) {
            Objects.requireNonNull(getSupportActionBar()).setTitle("그룹 정보 수정하기");

            HashMap<String, Object> info = (HashMap) intent.getSerializableExtra("groupInfo");

            groupID = (String) info.get("groupID");
            String name = (String) info.get("groupName");
            String comment = (String) info.get("comment");
            Uri image = (Uri) info.get("Uri");
            long type = (long) info.get("type");

            groupName.setText(name);
            groupComment.setText(comment);
            Glide.with(this).load(image).into(groupImage);

            RadioButton type0 = findViewById(R.id.newgroup_type0);
            RadioButton type1 = findViewById(R.id.newgroup_type1);

            if (type == 0) type0.setChecked(true);
            else type1.setChecked(true);
        } else {
            buisnessInfo = (HashMap) intent.getSerializableExtra("buisness");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CODE) {
            //크롭된 이미지 가져와서 이미지뷰에 보여주기
            if (resultCode == RESULT_OK) {
                assert data != null;
                if (data.hasExtra("data")) { //데이터를 가지고 있는지 확인
                    final Bundle extras = data.getExtras();

                    if (extras != null) {
                        Bitmap photo = extras.getParcelable("data"); //크롭한 이미지 가져오기
                        groupImage.setImageBitmap(photo); //이미지뷰에 넣기

                        ByteArrayOutputStream stream = new ByteArrayOutputStream(); //db에 저장할 용도
                        photo.compress(Bitmap.CompressFormat.PNG, 100, stream);
                        image = stream.toByteArray();
                    }
                }
            }
        } else if (requestCode == REQUEST_CROP_PICTURE) {
            if (resultCode == RESULT_OK) {
                // 이미지를 가져온 이후의 리사이즈할 이미지 크기를 결정
                // 이후에 이미지 크롭 어플리케이션을 호출

                assert data != null;
                Uri img = data.getData();

                Intent intent = new Intent("com.android.camera.action.CROP");
                intent.setDataAndType(img, "image/*");

                intent.putExtra("outputX", 300); //크롭한 이미지 x축 크기
                intent.putExtra("outputY", 300); //크롭한 이미지 y축 크기
                intent.putExtra("aspectX", 1); //크롭 박스의 x축 비율
                intent.putExtra("aspectY", 1); //크롭 박스의 y축 비율
                intent.putExtra("scale", true);
                intent.putExtra("return-data", true);
                startActivityForResult(intent, REQUEST_IMAGE_CODE);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.complete, menu); //툴바에 메뉴 설정
        return true;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_complete://확인버튼
                String resultName = groupName.getText().toString();
                String resultComment = groupComment.getText().toString();
                int typeId = groupType.getCheckedRadioButtonId();
                RadioButton resultTypeBtn = (RadioButton) findViewById(typeId);
                if (!ischange & image == null) {
                    Toast myToast = Toast.makeText(getApplicationContext(),
                            "정보를 모두 입력바랍니다.", Toast.LENGTH_SHORT);
                    myToast.show();
                }
                else if (resultName.equals("") | resultComment.equals("") | resultTypeBtn == null) {
                    Toast myToast = Toast.makeText(getApplicationContext(),
                            "정보를 모두 입력바랍니다.", Toast.LENGTH_SHORT);
                    myToast.show();
                } else {
                    progress.show();
                    String resultTypeStr = resultTypeBtn.getResources().getResourceName(typeId);
                    resultTypeStr = resultTypeStr.substring(resultTypeStr.length() - 1);
                    int resultType = Integer.parseInt(resultTypeStr);

                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    ///DB에 저장
                    if (ischange) {

                        Map<String, Object> info = new HashMap<>();
                        info.put("name", resultName);
                        info.put("comment", resultComment);
                        info.put("type", resultType);

                        db.collection("Groups").document(groupID).update(info);
                    } else { //init이면
                        String userUID = LoginActivity.userUID;
                        Map<String, Object> info = buisnessInfo;
                        info.put("name", resultName);
                        info.put("comment", resultComment);
                        info.put("type", resultType);
                        info.put("manager", userUID);
                        info.put("member", 1);

                        List<String> list = new ArrayList();
                        list.add(userUID);
                        info.put("people", list);
                        Timestamp time = Timestamp.now();
                        info.put("openingDate", time);

                        // groupID
                        Date dtime = time.toDate();
                        @SuppressLint("SimpleDateFormat") DateFormat f = new SimpleDateFormat("yyMMddhhmmss");
                        String stime = f.format(dtime);
                        groupID = userUID.substring(0, 8) + stime;

                        db.collection("Groups").document(groupID).set(info);
                        db.collection("Users").document(userUID).update("groupList", FieldValue.arrayUnion(groupID));

                        FirebaseMessaging.getInstance().subscribeToTopic(groupID);


                    }

                    if (image != null) {
                        //firebase storage에 업로드 -> uri를 firestore에 저장 -> 완료후 화면 이동
                        StorageReference mStorageRef = FirebaseStorage.getInstance().getReference();

                        String filename = "main.jpg";
                        StorageReference riversRef = mStorageRef.child("group_img/" + groupID + "/" + filename);
                        riversRef.putBytes(image).addOnSuccessListener(taskSnapshot ->
                                riversRef.getDownloadUrl().addOnSuccessListener(uri ->
                                        db.collection("Groups").document(groupID).update("image", uri.toString())
                                .addOnSuccessListener(unused -> {
                                    progress.dismiss();
                                    //화면이동
                                    finish();
                                })));
                    } else {
                        progress.dismiss();
                        //화면이동
                        finish();
                    }


                }
                break;
            case android.R.id.home://뒤로가기 버튼
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    static class ProgressDialog extends Dialog {
        public ProgressDialog(Context context) {
            super(context);
            requestWindowFeature(Window.FEATURE_NO_TITLE);// 다이얼 로그 제목을 안보이게...
            setContentView(R.layout.dialog_progress);
            setCanceledOnTouchOutside(false);   //주변터치방지
            setCancelable(false);   // 뒤로가기 방지
        }
    }
}