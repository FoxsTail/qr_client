package com.lis.qr_client.pojo;

import lombok.*;
import lombok.extern.java.Log;
import org.chalup.microorm.annotations.Column;

import java.util.HashMap;


@Data
@Log
@NoArgsConstructor
@AllArgsConstructor
@RequiredArgsConstructor
public class Equipment {
    @Column(value = "id",treatNullAsDefault = true)
    //Integer id;
    Long id;


    @Column(value = "type", treatNullAsDefault = true)
    @NonNull
    String type;

    @NonNull
    @Column(value = "vendor",treatNullAsDefault = true)
    String vendor;

    @NonNull
    @Column(value="model",treatNullAsDefault = true)
    String model;

    @NonNull
    @Column(value = "series",treatNullAsDefault = true)
    String series;

    @NonNull
    @Column("inventory_num")
    String inventory_num;

    @Column("attributes")
    //HashMap<String, Object> attributes;
    String attributes;

    @Column("serial_num")
    String serial_num;

    @Column("room")
    //Integer room;
    Long room;

    @Column(value = "id_asDetailIn", treatNullAsDefault = true)
    //Integer id_asDetailIn;
    Long id_asDetailIn;

    @Column(value = "id_tp", treatNullAsDefault = true)
    //Integer id_tp;
    Long id_tp;

    @Column("id_user")
    //Integer id_user;
    Long id_user;

    @Column("user_info")
    String user_info;

    @Column("address")
    String address;



}

