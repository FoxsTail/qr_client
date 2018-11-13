package com.lis.qr_client.utilities.adapter;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.lis.qr_client.R;
import lombok.extern.java.Log;

import java.util.List;
import java.util.Map;

@Log
public class InventoryAdapter extends RecyclerView.Adapter<InventoryAdapter.InventoryViewHolder> {

    List<Map<String, Object>> inventories;

    public InventoryAdapter(List<Map<String, Object>> inventories) {
        log.info("---- InventoryAdapter constructor---");
        this.inventories = inventories;
    }

    @Override
    public InventoryViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        log.info("----onCreateViewHolder---");
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.card_expanded_equipment, viewGroup, false);
        return new InventoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(InventoryViewHolder inventoryViewHolder, int item_id) {
        log.info("----onBindViewHolder---");
        log.info("----I is " + item_id + "--");

        Map<String, Object> inventory = inventories.get(item_id);

        //---if inventory was scanned - mark viewHolder with green
        if (inventory.get("isSelected") != null && (boolean) inventory.get("isSelected")) {
            inventoryViewHolder.itemView.setBackgroundColor(Color.parseColor("#b9f6ca"));
        }

        //---check and set name
        Object name = inventory.get("name");
        if (name != null) {
            inventoryViewHolder.tvItemName.setText(name.toString());
        }

        //---check and set inventory_num
        Object inventory_num = inventory.get("inventory_num");
        if (inventory_num != null) {
            inventoryViewHolder.itemView.setTag(inventory_num);
            log.info("-----TAG SET-----"+inventory_num);
            inventoryViewHolder.tvItemInventoryNum.setText(inventory_num.toString());
        }
    }

    @Override
    public int getItemCount() {
        return inventories.size();
    }

    class InventoryViewHolder extends RecyclerView.ViewHolder {
        TextView tvItemName;
        TextView tvItemInventoryNum;

        public InventoryViewHolder(View itemView) {
            super(itemView);
            log.info("---- InventoryViewHolder constructor---");
            tvItemName = itemView.findViewById(R.id.tvItemName);
            tvItemInventoryNum = itemView.findViewById(R.id.tvItemInventoryNum);
        }
    }

    public List<Map<String, Object>> getInventories() {
        return inventories;
    }
}
