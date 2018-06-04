package com.lis.qr_client.data;

import android.content.ContentValues;
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

    String[] names = {"Uno", "Due", "Tre", "Quattro", "Cinque"};
    String[] passwords = {"UnoP", "DueP", "TreP", "QuattroP", "CinqueP"};
    String[] emails = {"UnoP@e", "DueP@e", "TreP@e", "QuattroP@e", "CinqueP@e"};

    int [] id_pData = {1,2,3,5,4};
    String[] dataName = {"DataUno", "DataUnoDue", "DataUnoTre", "DataUnoQuattro", "DataUnoCinque"};


    public DBHelper(Context context) {
        super(context, "qrdb", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        //join запрос и выгрузить на страничку связанные элементы

        ContentValues cv = new ContentValues();
        log.info("----Create Database----");
        sqLiteDatabase.execSQL("create table user (" +
                "username text primary key, " +
                "password text," +
                "email text," +
                "id_pData);");

        for(int i=0; i<names.length;i++) {
            cv.clear();
            cv.put("username", names[i]);
            cv.put("password", passwords[i]);
            cv.put("email", emails[i]);
            cv.put("id_pData", id_pData[i]);
            sqLiteDatabase.insert("user", null, cv);
        }

        sqLiteDatabase.execSQL("create table personal_data (" +
                "id integer primary key, " +
                "data_name text);");

        for (int i = 0; i < id_pData.length; i++){
            cv.clear();

            cv.put("id_pData", id_pData[i]);
            cv.put("data_name", dataName[i]);
            sqLiteDatabase.insert("personal_data", null, cv);
        }



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
