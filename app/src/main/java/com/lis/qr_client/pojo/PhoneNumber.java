package com.lis.qr_client.pojo;

import lombok.*;
import org.chalup.microorm.annotations.Column;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
@NoArgsConstructor
public class PhoneNumber {

    @Column("id")
    private int id;

    @Column("phone_number")
    @NonNull
    private String phone_number;

    @Column("id_pd")
    @NonNull
    private Integer id_pd;


}
