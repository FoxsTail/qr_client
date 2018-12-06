package com.lis.qr_client.pojo;

import com.bignerdranch.expandablerecyclerview.Model.ParentObject;
import com.lis.qr_client.interfaces.InventoryPojo;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
@Data
public class EquipmentParent implements ParentObject, InventoryPojo {
    private List<Object> equipmentExpanded = new ArrayList<>();
    private boolean isSelected = false;

    private int id;
    private String mType;
    private String mVendor;
    private String mModel;
    private String mSeries;
    private String mInventory_num;

    public EquipmentParent(int id, String type, String vendor, String model, String series, String inventory_num) {
        this.id = id;
        this.mType = type;
        this.mVendor = vendor;
        this.mModel = model;
        this.mSeries = series;
        this.mInventory_num = inventory_num;
    }

    @Override
    public List<Object> getChildObjectList() {
        return equipmentExpanded;
    }

    @Override
    public void setChildObjectList(List<Object> list) {
        equipmentExpanded = list;
    }


    public String getEquipmentName(){
        return mType+" "+mVendor+" "+mModel+" "+mSeries;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}
