package com.lis.qr_client.pojo;

import lombok.*;
import org.chalup.microorm.annotations.Column;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class Address {

   @Column("id")
   private Integer id;

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

    public String getFullAddress() {
        return city+" "+street+" "+number+"/ "+room;
    }
}