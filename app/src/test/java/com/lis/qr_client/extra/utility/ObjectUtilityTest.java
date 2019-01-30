package com.lis.qr_client.extra.utility;

import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class ObjectUtilityTest {
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
        map2.put("te", "te");
        map2.put("te1", "te1");
        map2.put("te2", "te2");
        map2.put("te3", "te3");

        Map<String, Object> map3 = new HashMap<>();
        map3.put("t", "t");
        map3.put("t1", "t1");
        map3.put("t2", "t2");
        map3.put("t3", "t3");

        list.add(map2);
        list.add(map);

        /*list1.add(map2);
        list1.add(map);*/

        list2.add(map3);
        list2.add(map);
        list2.add(map2);

        String result = ObjectUtility.convertToStringForCsv(new String[]{"a", "b", "c"}, list, list1, null);

        assertNotNull(result);
        System.out.println(result);
    }

}