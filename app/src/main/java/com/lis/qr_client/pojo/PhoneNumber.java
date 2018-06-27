package com.lis.qr_client.pojo;

import lombok.*;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
@NoArgsConstructor
public class PhoneNumber {
    private int id;
    @NonNull
    private String phone_number;

}
