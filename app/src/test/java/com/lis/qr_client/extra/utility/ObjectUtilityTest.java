package com.lis.qr_client.extra.utility;

import com.lis.qr_client.constants.DbTables;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class ObjectUtilityTest {
    @Test
    public void mapListToStringList() throws Exception {


        List<Map<String, Object>> list = new ArrayList<>();


        Map<String, Object> map = new HashMap<>();
        map.put(DbTables.TABLE_INVENTORY_COLUMN_NAME, "test");
        map.put(DbTables.TABLE_INVENTORY__COLUMN_INV_NUMBER, "test1");
        map.put("test2", "test2");
        map.put("test3", "test3");

        Map<String, Object> map2 = new HashMap<>();
        map2.put(DbTables.TABLE_INVENTORY_COLUMN_NAME, "кіт");
        map2.put(DbTables.TABLE_INVENTORY__COLUMN_INV_NUMBER, "людина");
        map2.put("te2", "їжак");
        map2.put("te3", "щось дивне");

        Map<String, Object> map3 = new HashMap<>();
        map3.put(DbTables.TABLE_INVENTORY_COLUMN_NAME, "дві собаки");
        map3.put(DbTables.TABLE_INVENTORY__COLUMN_INV_NUMBER, "один собака");
        map3.put("дві собаки", "дві собаки");
        map3.put("один собака", "один собака");

        list.add(map);
        list.add(map2);
        list.add(map3);

        List<String> result = ObjectUtility.mapListInventoryToStringList(list);

        assertNotNull(result);
        for(String s: result){
            System.out.println(s);
        }
    }

    @Test
    public void fileStringFromList() throws Exception {

        List<Map<String, Object>> list = new ArrayList<>();
        List<Map<String, Object>> list1 = new ArrayList<>();
        List<Map<String, Object>> list2 = new ArrayList<>();


        Map<String, Object> map = new HashMap<>();
        map.put("test", "test");
        map.put("test1", "test1");
        map.put("test2", "test2");
        map.put("test3", "test3");

        Map<String, Object> map2 = new HashMap<>();
        map2.put("te", "кіт");
        map2.put("te1", "людина");
        map2.put("te2", "їжак");
        map2.put("te3", "щось дивне");

        Map<String, Object> map3 = new HashMap<>();
        map3.put("t", "дві собаки");
        map3.put("t1", "один собака");
        map3.put("дві собаки", "дві собаки");
        map3.put("один собака", "один собака");

        list.add(map);
        list.add(map2);

        list1.add(map2);
        list1.add(map3);

        list2.add(map3);
        list2.add(map);


        String result = ObjectUtility.fileStringFromList(list, "some_val");

        assertNotNull(result);
        System.out.println(result);
    }

    @Test
    public void convertAndSaveListsToCsvFile() throws Exception {
        List<Map<String, Object>> list = new ArrayList<>();
        List<Map<String, Object>> list1 = new ArrayList<>();
        List<Map<String, Object>> list2 = new ArrayList<>();


        Map<String, Object> map = new HashMap<>();
        map.put("test", "test");
        map.put("test1", "test1");
        map.put("test2", "test2");
        map.put("test3", "test3");

        Map<String, Object> map2 = new HashMap<>();
        map2.put("te", "кіт");
        map2.put("te1", "людина");
        map2.put("te2", "їжак");
        map2.put("te3", "щось дивне");

        Map<String, Object> map3 = new HashMap<>();
        map3.put("t", "дві собаки");
        map3.put("t1", "один собака");
        map3.put("дві собаки", "дві собаки");
        map3.put("один собака", "один собака");

        list.add(map2);
        list.add(map);

        list1.add(map2);
        list1.add(map);

        list2.add(map3);
        list2.add(map);
        list2.add(map2);


        ObjectUtility.convertAndSaveListsToCsvFile("test", new String[]{"a", "b", "c"},
                list, list1, list2);
    }

    @Test
    public void convertToStringForCsv() throws Exception {

        List<Map<String, Object>> list = new ArrayList<>();
        List<Map<String, Object>> list1 = new ArrayList<>();
        List<Map<String, Object>> list2 = new ArrayList<>();


        Map<String, Object> map = new HashMap<>();
        map.put("test", "test");
        map.put("test1", "test1");
        map.put("test2", "test2");
        map.put("test3", "test3");

        Map<String, Object> map2 = new HashMap<>();
        map2.put("te", "кіт");
        map2.put("te1", "людина");
        map2.put("te2", "їжак");
        map2.put("te3", "щось дивне");

        Map<String, Object> map3 = new HashMap<>();
        map3.put("t", "дві собаки");
        map3.put("t1", "один собака");
        map3.put("дві собаки", "дві собаки");
        map3.put("один собака", "один собака");

        list.add(map);
        list.add(map2);

        list1.add(map2);
        list1.add(map3);

        list2.add(map3);
        list2.add(map);


        String result = ObjectUtility.convertToStringForCsv(new String[]{"a", "b", "c"}, list, list1, list2);

        assertNotNull(result);
        System.out.println(result);
    }

}