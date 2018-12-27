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

    @Column("username")
    private String username;

    @Column("email")
    @NonNull
    private String email;

    @Column("password")
    @NonNull
    private String password;

    @Column(value = "id_tp", treatNullAsDefault = true)
    private Integer id_tp;

    @Column(value = "id_wp", treatNullAsDefault = true)
    private Integer id_wp;
}