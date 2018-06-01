package com.lis.qr_client.data;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.lis.qr_client.interfaces.IUserDatabaseHandler;
import com.lis.qr_client.pojo.User;
import lombok.extern.java.Log;

import java.util.List;

@Log
public class DBHelper extends SQLiteOpenHelper implements IUserDatabaseHandler {


    public DBHelper(Context context) {
        super(context, "qrdb", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        log.info("----Create Database----");
        sqLiteDatabase.execSQL("create table user (" +
                "username text primary key, " +
                "password text," +
                "email text);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    @Override
    public User getByUsername(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        try(Cursor cursor = db.query("user", null, "username=?", new String[]{username}, null, null, null);){

            if (cursor.moveToFirst()) {
                return new User(cursor.getString(0), cursor.getString(1), cursor.getString(2));
            }
        }
        return null;
    }

    @Override
    public List<User> getAllUsers() {
        return null;
    }

    @Override
    public void deleteAllUsers() {

    }

    @Override
    public void deleteByUsername() {

    }
}
