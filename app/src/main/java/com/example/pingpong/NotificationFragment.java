package com.example.pingpong;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class NotificationFragment extends Fragment {
    List<HashMap<String, Object>> noticeList;
    RequestManager requestManager;
    SwipeRefreshLayout mSwipe;
    SwipeRefreshLayout.OnRefreshListener listener;
    public NotificationFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestManager = Glide.with(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notification, container, false);
        setHasOptionsMenu(true);

        RecyclerView recy = view.findViewById(R.id.recy_notification);
        TextView notxt = view.findViewById(R.id.txt_notification_none);

        noticeList = getNotiList();
        if(noticeList.size() == 0) notxt.setVisibility(View.VISIBLE);
        else notxt.setVisibility(View.GONE);

        NotificationAdapter adapter = new NotificationAdapter(noticeList, requestManager, notxt);
        recy.setAdapter(adapter);
        recy.setLayoutManager(new LinearLayoutManager(getContext()));
        DividerItemDecoration dividerItemDecoration =
                new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL);
        recy.addItemDecoration(dividerItemDecoration);


        //새로고침
        mSwipe = view.findViewById(R.id.notification_layout);
        listener = () -> {
            noticeList = getNotiList();
            adapter.setList(noticeList);
            if(noticeList.size() == 0) notxt.setVisibility(View.VISIBLE);
            else notxt.setVisibility(View.GONE);
            mSwipe.setRefreshing(false);
        };
        mSwipe.setOnRefreshListener(listener);

        return view;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.delall, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menu_del_all) {
            delMessage();
            mSwipe.setRefreshing(true);
            listener.onRefresh();
        }
        return super.onOptionsItemSelected(item);
    }

    private void delMessage() {
        SQLiteDB sqLiteDB = new SQLiteDB(getContext());
        SQLiteDatabase db = sqLiteDB.getReadableDatabase();
        String sql = "DELETE FROM Message;";

        db.execSQL(sql);
    }

    private List<HashMap<String, Object>> getNotiList() {
        SQLiteDB sqLiteDB = new SQLiteDB(getContext());
        SQLiteDatabase db = sqLiteDB.getReadableDatabase();
        String sql = "SELECT * FROM Message;";

        List<HashMap<String, Object>> list = new ArrayList();

        Cursor cursor = db.rawQuery(sql, null);
        if (cursor!=null)
        {
            // 칼럼의 마지막까지
            while( cursor.moveToNext() ) {
                HashMap<String, Object> info = new HashMap<>();
                info.put("body", cursor.getString(1));
                info.put("type", cursor.getString(2));
                info.put("groupID", cursor.getString(3));
                info.put("postID", cursor.getString(4));
                info.put("writerUID", cursor.getString(5));
                info.put("name", cursor.getString(6));
                info.put("text", cursor.getString(7));
                info.put("timestamp", cursor.getString(8));
                info.put("gameID", cursor.getString(9));
                info.put("managerUID", cursor.getString(10));
                info.put("time", cursor.getString(11));

                list.add(0, info);
            }
            cursor.close();
        }
        db.close();
        return list;
    }
}