package com.lis.qr_client.pojo;

import lombok.*;
import org.chalup.microorm.annotations.Column;

@Data
@RequiredArgsConstructor
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Column("id")
    private Integer id;

    @Column("email")
    @NonNull
    private String email;

    @Column("password")
    @NonNull
    private String password;

    @Column("id_pd")
    private Integer id_pd;

    private PersonalData personalData;

}