package com.lis.qr_client.utilities.dialog_fragment;


import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;
import com.lis.qr_client.R;
import com.lis.qr_client.pojo.Inventory;
import com.lis.qr_client.utilities.Utility;
import com.lis.qr_client.utilities.adapter.InventoryAdapter;
import lombok.extern.java.Log;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Log
//TODO: make it abstract along with scan neightbor
public class ItemDialogFragment extends DialogFragment {
    public static final String ARG_TITLE = "title";

    private AlertDialog.Builder alertBuilder;
    private String inventory_num;
    private InventoryAdapter adapter;

    private Context context;
    private Utility utility = new Utility();

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        log.info("---Item on createDialog---");

        Bundle args = getArguments();

        String title = args.getString(ARG_TITLE);

        alertBuilder = new AlertDialog.Builder(getActivity());


        alertBuilder.setTitle(title)
                .setItems(R.array.itemsArray, dialogListener);
        return alertBuilder.create();

    }


    /*handle pressed item from itemsArray*/
    DialogInterface.OnClickListener dialogListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            List<String> itemsOptionsArray = Arrays.asList(context.getResources().getStringArray(R.array.itemsArray));
            int item_info = itemsOptionsArray.indexOf(context.getResources().getString(R.string.item_info));
            int delete_from_list = itemsOptionsArray.indexOf(context.getResources().getString(R.string.delete_from_list));


            if (which == item_info) {
                log.info("Get full inventory info");
                /*knock-knock to server to get info*/

            } else if (which == delete_from_list) {

                //TODO: add one more dialog for deleting, like "r u sure, that u r sure u wanna delete it?"

                log.info("Delete from list "+inventory_num);

                /*get list from adapter*/
                log.info("-----Adapter-----"+adapter.toString());
                log.info("-----ListInv-----"+adapter.getInventories().toString());

                List<Map<String, Object>> inventoryList = adapter.getInventories();

                /*delete from list*/
                Map<String, Object> searched_map;
                int position = -1;


                searched_map = utility.findMapByInventoryNum(inventoryList, inventory_num);
                position = inventoryList.indexOf(searched_map);

                if (position > -1){
                    inventoryList.remove(position);
                }

                /*notifyDataChanged*/
                adapter.notifyDataSetChanged();

            } else {
                log.info("Back to list");

            }
        }
    };


    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        log.info("-----Dismiss-----");
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        log.info("-----Cancel-----");
    }

    public void callDialog(Context context, Bundle bundle, InventoryAdapter adapter, String inventory_num, String tag) {
        log.info("-----callDialog-----");
        this.context = context;
        this.inventory_num = inventory_num;
        this.adapter = adapter;

        bundle.putString(ARG_TITLE, context.getResources().getString(R.string.choose_action));
        this.setArguments(bundle);

        this.show(((AppCompatActivity) context).getFragmentManager(), tag);

    }
}
