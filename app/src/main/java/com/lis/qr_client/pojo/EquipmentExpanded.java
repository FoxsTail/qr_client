package com.lis.qr_client.pojo;

import lombok.Data;

@Data
public class EquipmentExpanded {
    private String mSerial_num;
    private String mId_user;
    private String mId_tp;
    private String mId_asDetailIn;
    private String mRoom;


    public EquipmentExpanded(String serial_num,
                             Integer id_user, Integer id_AsDetailIn, Integer id_tp, Integer room) {
        this.mSerial_num = serial_num;

        if (id_user != null) {
            this.mId_user = id_user.toString();
        }else {
            this.mId_user = "";
        }

        if (id_AsDetailIn != null) {
            this.mId_asDetailIn = id_AsDetailIn.toString();
        }else {
            this.mId_asDetailIn = "";
        }

        if (id_tp != null) {
            this.mId_tp = id_tp.toString();
        }else {
            this.mId_tp = "";
        }


        if (room != null) {
            this.mRoom = room.toString();
        }else {
            this.mRoom = "";
        }
    }

    @Override
    public String toString(){
        return  "Serial number: "+mSerial_num+"\n"+
                "User id: "+mId_user+"\n"+
                "Tp id: "+mId_tp+"\n"+
                "As detail in: "+mId_asDetailIn +"\n"+
                "room: "+mRoom;
    }

}
