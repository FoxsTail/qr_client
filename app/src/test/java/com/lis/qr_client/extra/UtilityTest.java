package com.lis.qr_client.extra;

import android.database.Cursor;
import com.lis.qr_client.extra.utility.Utility;
import com.lis.qr_client.pojo.User;
import org.junit.Test;

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