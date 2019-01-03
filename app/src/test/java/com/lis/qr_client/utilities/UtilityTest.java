package com.lis.qr_client.utilities;

import android.database.Cursor;
import com.lis.qr_client.pojo.User;
import org.junit.Test;

import static org.junit.Assert.*;

public class UtilityTest {

    @Test
    public void cursorToClass(){
        Cursor cursor = null;
        Object o = Utility.cursorToClass(cursor, User.class.getName());

        if(o != null){
            System.out.println(o.toString());
        }
    }

}