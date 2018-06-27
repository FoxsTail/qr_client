package com.lis.qr_client.pojo;

import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@RequiredArgsConstructor
@AllArgsConstructor
public class PersonalData {
    private int id;
    @NonNull
    private String name;
    @NonNull
    private String surname;
    @NonNull
    private String patronymic;
    @NonNull
    private String passport;
    @NonNull
    private String inn;

    List<PhoneNumber> phoneNumbers;

//  private int room_id;


}
