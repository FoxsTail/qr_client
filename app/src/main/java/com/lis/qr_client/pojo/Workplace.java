package com.lis.qr_client.pojo;

import lombok.Data;
import org.chalup.microorm.annotations.Column;

@Data
public class Workplace {
    @Column("wp_id")
    private Integer wp_id;

    @Column("position")
    private String position;

    @Column("department")
    private String department;

    @Column("direction")
    private String direction;

    @Column("remote_workstation")
    private boolean remote_workstation;
}
