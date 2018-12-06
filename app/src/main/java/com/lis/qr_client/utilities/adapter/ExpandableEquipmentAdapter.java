package com.lis.qr_client.utilities.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.lis.qr_client.R;
import com.lis.qr_client.pojo.EquipmentParent;
import lombok.extern.java.Log;

import java.util.List;

@Log
public class ExpandableEquipmentAdapter extends RecyclerView.Adapter<ExpandableEquipmentAdapter.EquipmentViewHolder> {
    private List<EquipmentParent> mParentItemList;

    public ExpandableEquipmentAdapter(List<EquipmentParent> mParentItemList) {
        this.mParentItemList = mParentItemList;
    }

    @Override
    public EquipmentViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.card_expanded_equipment, viewGroup, false);
        return new EquipmentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(EquipmentViewHolder equipmentViewHolder, int i) {
        EquipmentParent equipmentParent = mParentItemList.get(i);
        LinearLayout ll_child_items = equipmentViewHolder.ll_child_items;
        List<Object> equipment_childs = equipmentParent.getChildObjectList();

        if (equipmentParent.isSelected()) {
            equipmentViewHolder.itemView.setBackgroundColor(Color.parseColor("#b9f6ca"));
        }

        /*set tag and text in TextViews*/
         equipmentViewHolder.itemView.setTag(equipmentParent.getMInventory_num());
        equipmentViewHolder.tvItemName.setText(equipmentParent.getEquipmentName());
        equipmentViewHolder.tvItemInventoryNum.setText(equipmentParent.getMInventory_num());

        int child_items_size = equipmentParent.getChildObjectList().size();
        int max_child_items = ll_child_items.getChildCount();

        /*hide excess TextViews*/
        TextView excess_tv;
        if (child_items_size < max_child_items) {
            for (int j = child_items_size; i < max_child_items; i++) {
                excess_tv = (TextView) ll_child_items.getChildAt(j);
                excess_tv.setVisibility(View.GONE);
            }
        }


        /*fill child TextView with data*/
        TextView tvChild;
        for (int j = 0; j < child_items_size; j++) {
            tvChild = (TextView) ll_child_items.getChildAt(j);
            tvChild.setText(equipment_childs.get(j).toString());
        }


    }

    @Override
    public int getItemCount() {
        return mParentItemList.size();
    }

    class EquipmentViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        Context context;
        public TextView tvItemName;
        public TextView tvItemInventoryNum;
        LinearLayout ll_child_items;

        public EquipmentViewHolder(View itemView) {
            super(itemView);
            log.info("---- EquipmentChildViewHolder constructor---");

            itemView.setOnClickListener(this);
            tvItemName = itemView.findViewById(R.id.tvItemName);
            tvItemInventoryNum = itemView.findViewById(R.id.tvItemInventoryNum);
            context = itemView.getContext();
            ll_child_items = itemView.findViewById(R.id.layout_card_equipment);
            ll_child_items.setVisibility(View.GONE);

            createMaxChildTextViewInLayout();


        }

        public void createMaxChildTextViewInLayout() {
            log.info("----createMaxChildTextViewInLayout---");

            int max_child_item_value = 0;
            int temp_size;

        /*find max child element value*/
            for (int i = 0; i < mParentItemList.size(); i++) {
                temp_size = mParentItemList.get(i).getChildObjectList().size();
                if (temp_size > max_child_item_value) {
                    max_child_item_value = temp_size;
                }
            }

            TextView tvChild;
        /*create max_child_value TextView*/
            for (int i = 0; i < max_child_item_value; i++) {

                tvChild = new TextView(context);
                tvChild.setId(i);
                tvChild.setPadding(0, 20, 0, 20);
                tvChild.setGravity(Gravity.CENTER);
                tvChild.setBackground(ContextCompat.getDrawable(context, R.drawable.background_sub_module_text));
                tvChild.setOnClickListener(this);

                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
                ll_child_items.addView(tvChild, layoutParams);
            }

            tvItemName.setOnClickListener(this);

        }

        @Override
        public void onClick(View v) {
            log.info("----On click tv---");

            if (ll_child_items.getVisibility() == View.VISIBLE) {
                ll_child_items.setVisibility(View.GONE);
            } else {
                ll_child_items.setVisibility(View.VISIBLE);

            }
        }
    }

    public List<EquipmentParent> getmParentItemList() {
        return mParentItemList;
    }

}

