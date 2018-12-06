package com.lis.qr_client.pojo;

import com.lis.qr_client.interfaces.InventoryPojo;
import lombok.Data;

@Data
public class Inventory implements InventoryPojo {
    private String name;
    private String inventory_num;
    private Integer room;
    private Integer address_id;
}
