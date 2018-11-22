package com.lis.qr_client.pojo;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * cheater class for bundle values
 */
public class UniversalSerializablePojo implements Serializable {
    private Object object;
    private List<Map<String, Object>> mapList;


    public UniversalSerializablePojo(Object object) {
        this.object = object;
    }

    public UniversalSerializablePojo(List<Map<String, Object>> mapList) {
        this.mapList = mapList;
    }

    public Object getObject() {
        return object;
    }

    public List<Map<String, Object>> getMapList() {
        return mapList;
    }
}
