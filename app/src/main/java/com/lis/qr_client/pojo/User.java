package com.lis.qr_client.pojo;

import lombok.*;

@Data
@RequiredArgsConstructor
@NoArgsConstructor

public class User {
    private int id;
    @NonNull
    private String username;
    @NonNull
    private String password;
    @NonNull
    private String email;

    private PersonalData personalData;
}