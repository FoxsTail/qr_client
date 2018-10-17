package com.lis.qr_client.utilities.adapter;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.bignerdranch.expandablerecyclerview.ViewHolder.ChildViewHolder;
import com.bignerdranch.expandablerecyclerview.ViewHolder.ParentViewHolder;
import com.lis.qr_client.R;
import lombok.extern.java.Log;

import java.util.List;
import java.util.Map;

@Log
public class EquipmentAdapter extends RecyclerView.Adapter<EquipmentAdapter.EquipmentViewHolder> {

    private List<Map<String, Object>> equipments;

    public EquipmentAdapter(List<Map<String, Object>> equipments) {
        log.info("---- EquipmentAdapter constructor---");
        this.equipments = equipments;
    }

    @Override
    public EquipmentViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        log.info("----onCreateViewHolder---");
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.card_expanded_equipment, viewGroup, false);
        return new EquipmentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(EquipmentViewHolder viewHolder, int item_id) {
        log.info("----onBindViewHolder---");
        log.info("----I is " + item_id + "--");

        /*set data for tv from each map with equipment*/
        Map<String, Object> map = equipments.get(item_id);

        if (map.get("isSelected") != null && (boolean) map.get("isSelected")) {
            viewHolder.itemView.setBackgroundColor(Color.parseColor("#b9f6ca"));
        }

        /*type*/
        String type;
        if (map.get("type") != null) {
            type = (String) map.get("type");
        } else {
            type = "";
        }

        /*Name*/
        String name;
        String vendor;
        String model;
        String series;

        if (map.get("vendor") == null || map.get("vendor").equals("null")) {
            vendor = "";
        } else {
            vendor = map.get("vendor").toString();
        }

        if (map.get("model") == null || map.get("model").equals("null")) {
            model = "";
        } else {
            model = map.get("model").toString();
        }

        if (map.get("series") == null || map.get("series").equals("null")) {
            series = "";
        } else {
            series = map.get("series").toString();
        }

        name = vendor + " " +
                model + " " +
                series;


        /*inventory_num*/
        String inventory_num;
        if (map.get("inventory_num") != null) {
            inventory_num = (String) map.get("inventory_num");
        } else {
            inventory_num = "";
        }


        log.info(type + " " + name + " " + inventory_num);
        viewHolder.itemView.setTag(inventory_num);

        viewHolder.tvItemName.setText(type + " " + name);
        viewHolder.tvItemInventoryNum.setText(inventory_num);

    }

    @Override
    public int getItemCount() {
        return equipments.size();
    }


    /**
     * ViewHolder for the equipment
     */
    class EquipmentViewHolder extends RecyclerView.ViewHolder {
        // TextView tvItemId;
        public TextView tvItemName;
        public TextView tvItemInventoryNum;
        // TextView tvItemType;

        EquipmentViewHolder(View itemView) {
            super(itemView);
            log.info("---- EquipmentViewHolder constructor---");
            //  tvItemId = itemView.findViewById(R.id.tvItemId);
            //  tvItemType = itemView.findViewById(R.id.tvItemType);
            tvItemName = itemView.findViewById(R.id.tvItemName);
            tvItemInventoryNum = itemView.findViewById(R.id.tvItemInventoryNum);
        }

    }


    //------Getters------


    public List<Map<String, Object>> getEquipments() {
        return equipments;
    }

}
