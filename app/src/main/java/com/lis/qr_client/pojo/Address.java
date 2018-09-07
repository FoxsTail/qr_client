package com.lis.qr_client.pojo;

import lombok.*;


@Data
@NoArgsConstructor
@RequiredArgsConstructor
@AllArgsConstructor
public class Address {
    private Integer tp_id;
 @NonNull
    private String city;
    @NonNull
    private String street;
    @NonNull
    private Integer number;
    @NonNull
    private Integer floor;
    @NonNull
    private Integer room;

}