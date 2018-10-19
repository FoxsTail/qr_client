package com.lis.qr_client.pojo;

import lombok.Data;

import java.util.HashMap;

@Data
public class EquipmentExpanded {
    private String mAttributes;
    private String mSerial_num;
    private String mUser_Info;


    public EquipmentExpanded(String attributes, String serial_num,
                             String user_Info) {
        mAttributes = attributes;
        mSerial_num = serial_num;
        mUser_Info = user_Info;
    }

    @Override
    public String toString() {
        return "Serial number: " + "\n" + mSerial_num + "\n" +
                "Attributes: "+ mAttributes + "\n" +
                "Responsible: " + "\n" + mUser_Info;
    }
}
