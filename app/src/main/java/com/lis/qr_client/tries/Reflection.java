package com.lis.qr_client.tries;

import android.os.Message;
import com.lis.qr_client.pojo.Inventory;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class Reflection {
    public static void main(String[] args) {
        Testo t = new Testo();
        try {
            Field field = t.getClass().getDeclaredField("a");
            field.setAccessible(true);
            int a = (int) field.get(t);

            field.set(t, 23);

            a = (int) field.get(t);

            System.out.println(a);

            Method method = t.getClass().getDeclaredMethod("myMeth");
            method.setAccessible(true);
            method.invoke(t);


            Testo testo = null;

            Class clazz = Class.forName(Testo.class.getClass().getName());
            testo = (Testo) clazz.newInstance();

            System.out.println(testo.c);

        } catch (NoSuchFieldException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
    }
}


    class Testo {

        private int a = 2;
        private int b;
        public int c  = 78;

        private void myMeth(){
            System.out.println("hahahahhaha");
        }
    }



