package com.lis.qr_client.utilities.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.bignerdranch.expandablerecyclerview.Adapter.ExpandableRecyclerAdapter;
import com.bignerdranch.expandablerecyclerview.Model.ParentObject;
import com.bignerdranch.expandablerecyclerview.ViewHolder.ChildViewHolder;
import com.bignerdranch.expandablerecyclerview.ViewHolder.ParentViewHolder;
import com.lis.qr_client.R;
import com.lis.qr_client.pojo.Equipment;
import com.lis.qr_client.pojo.EquipmentExpanded;
import lombok.extern.java.Log;

import java.util.List;

@Log
public class EquipmentExpandableAdapter extends ExpandableRecyclerAdapter<EquipmentExpandableAdapter.EquipmentParentViewHolder,
        EquipmentExpandableAdapter.EquipmentChildViewHolder> {
    LayoutInflater mInflater;

    List<ParentObject> mParentItemList;

    public EquipmentExpandableAdapter(Context context, List<ParentObject> parentItemList) {
        super(context, parentItemList);
        log.info("---- EquipmentExpandableAdapter constructor---");
        mInflater = LayoutInflater.from(context);
        mParentItemList = parentItemList;
    }


    @Override
    public EquipmentParentViewHolder onCreateParentViewHolder(ViewGroup viewGroup) {
        log.info("----onCreateParentViewHolder---");
        View view = mInflater.inflate(R.layout.recycler_item_2, viewGroup, false);
        return new EquipmentParentViewHolder(view);
    }

    @Override
    public EquipmentChildViewHolder onCreateChildViewHolder(ViewGroup viewGroup) {
        log.info("----onCreateChildViewHolder---");
        View view = mInflater.inflate(R.layout.recycler_item, viewGroup, false);
        return new EquipmentChildViewHolder(view);
    }

    @Override
    public void onBindParentViewHolder(EquipmentParentViewHolder parentViewHolder, int i, Object o) {
        log.info("----onBindParentViewHolder---");
        log.info("----I is " + i + "--");
        Equipment equipment = (Equipment) o;
        String inventory_num = equipment.getMInventory_num();

        if (equipment.isSelected()) {
            parentViewHolder.itemView.setBackgroundColor(Color.parseColor("#b9f6ca"));
        }

        parentViewHolder.itemView.setTag(inventory_num);
        parentViewHolder.tvItemName.setText(equipment.getEquipmentName());
        parentViewHolder.tvItemInventoryNum.setText(inventory_num);
    }

    @Override
    public void onBindChildViewHolder(EquipmentChildViewHolder equipmentChildViewHolder, int i, Object o) {
        log.info("----onBindChildViewHolder---");
        EquipmentExpanded equipmentExpanded = (EquipmentExpanded) o;
        equipmentChildViewHolder.tvTest.setText(equipmentExpanded.toString());

    }

    class EquipmentParentViewHolder extends ParentViewHolder {
        public TextView tvItemName;
        public TextView tvItemInventoryNum;


        EquipmentParentViewHolder(View itemView) {
            super(itemView);
            log.info("---- EquipmentParentViewHolder constructor---");
            tvItemName = itemView.findViewById(R.id.tvItemName);
            tvItemInventoryNum = itemView.findViewById(R.id.tvItemInventoryNum);
        }

    }

    class EquipmentChildViewHolder extends ChildViewHolder {

        public TextView tvTest;

        public EquipmentChildViewHolder(View itemView) {
            super(itemView);
            log.info("---- EquipmentChildViewHolder constructor---");
            this.tvTest = itemView.findViewById(R.id.tvTest);
        }
    }

    public List<ParentObject> getmParentItemList() {
        return mParentItemList;
    }

    public void addParentList(List<ParentObject> parentItemList){
        this.mParentItemList = parentItemList;
        notifyDataSetChanged();
    }
}
