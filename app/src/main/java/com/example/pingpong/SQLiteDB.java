package com.example.pingpong;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class SQLiteDB extends SQLiteOpenHelper {
    public SQLiteDB(@Nullable Context context) {
        super(context, "sqlite_file.db",null, 2);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String query = "CREATE TABLE IF NOT EXISTS Message (" +
                "title TEXT NOT NULL, body TEXT NOT NULL, type TEXT NOT NULL, groupID TEXT NOT NULL, " +
                "postID TEXT, writerUID TEXT, name TEXT, text TEXT, timestamp TEXT, gameID TEXT, managerUID TEXT," +
                "time TEXT);";

        sqLiteDatabase.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        String query = "DROP TABLE IF EXISTS Message;";

        sqLiteDatabase.execSQL(query);

        onCreate(sqLiteDatabase);
    }

}
