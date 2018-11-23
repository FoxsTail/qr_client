package com.lis.qr_client.utilities.adapter;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import com.lis.qr_client.R;
import com.lis.qr_client.utilities.dialog_fragment.ItemDialogFragment;
import lombok.extern.java.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Log
public class InventoryAdapter extends RecyclerView.Adapter<InventoryAdapter.InventoryViewHolder> {

    private final Context context;
    List<Map<String, Object>> inventories = new ArrayList<>();


    public InventoryAdapter(Context context, List<Map<String, Object>> inventories) {
        log.info("---- InventoryAdapter constructor---");
        this.context = context;
        this.inventories = inventories;


    }

    @Override
    public InventoryViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        log.info("----onCreateViewHolder---");
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.card_expanded_equipment, viewGroup, false);

        return new InventoryViewHolder(view);
    }



/*
    *//*TESTING*//*
    @Override
    public void onBindViewHolder(InventoryViewHolder inventoryViewHolder, int item_id) {
        log.info("----onBindViewHolder---");
        log.info("----I is " + item_id + "--");

        Map<String, Object> inventory = inventories.get(item_id);

        for(Map.Entry<String, Object> map: inventory.entrySet()){
            inventoryViewHolder.tvItemName.setText(map.getKey());
            inventoryViewHolder.tvItemInventoryNum.setText(map.getValue().toString());
        }
    }*/

    @Override
    public void onBindViewHolder(InventoryViewHolder inventoryViewHolder, int item_id) {
        log.info("----onBindViewHolder---");
        log.info("----I is " + item_id + "--");

        Map<String, Object> inventory = inventories.get(item_id);

        log.info("-----------"+inventory+"--------");


        //---check and set name
        Object name = inventory.get("name");
        if (name != null) {
            inventoryViewHolder.tvItemName.setText(name.toString());
        }

        //---check and set inventory_num
        final Object inventory_num = inventory.get("inventory_num");
        if (inventory_num != null) {
            inventoryViewHolder.itemView.setTag(inventory_num);
            log.info("-----TAG SET-----"+inventory_num);
            inventoryViewHolder.tvItemInventoryNum.setText(inventory_num.toString());

            //TODO:!!!ВЫЗЫВАТЬ В ОТДЕЛЬНОМ ПОТОКЕ
            //set onClickListener for each initialized item. use inventory_num as search marker
            inventoryViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ItemDialogFragment dialogFragment = new ItemDialogFragment();
                    Bundle bundle = new Bundle();
                    dialogFragment.callDialog(context, bundle, getThisAdapter(), (String) inventory_num, "item_actions");
                }
            });
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

    public InventoryAdapter getThisAdapter(){
        return this;
    }
}
