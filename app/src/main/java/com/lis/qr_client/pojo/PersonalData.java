package com.lis.qr_client.pojo;

import lombok.*;
import org.chalup.microorm.annotations.Column;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@RequiredArgsConstructor
@AllArgsConstructor
public class PersonalData {

    @Column("id")
    private Integer id;

    @Column("name")
    @NonNull
    private String name;

    @Column("surname")
    @NonNull
    private String surname;

    @Column("patronymic")
    @NonNull
    private String patronymic;

    @Column("passport")
    @NonNull
    private String passport;

    @Column("inn")
    @NonNull
    private String inn;

    @Column(value = "id_tp")
    private Integer id_tp;

    @Column(value = "id_wp")
    private Integer id_wp;

    private Address address;

    private Workplace workplace;

    List<PhoneNumber> phoneNumbers = new ArrayList<>();



}
