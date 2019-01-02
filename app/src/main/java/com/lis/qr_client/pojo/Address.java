package com.lis.qr_client.pojo;

import lombok.*;
import org.chalup.microorm.annotations.Column;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class Address {

   @Column("tp_id")
   private Integer tp_id;

   @Column("city")
   private String city;

   @Column("street")
   private String street;

   @Column("number")
   private Integer number;

   @Column("floor")
   private Integer floor;

   @Column("room")
   private Integer room;

}