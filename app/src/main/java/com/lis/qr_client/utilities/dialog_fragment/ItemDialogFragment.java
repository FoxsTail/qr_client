package com.lis.qr_client.utilities.dialog_fragment;


import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Pair;
import com.lis.qr_client.R;
import com.lis.qr_client.activity.EquipmentItemActivity;
import com.lis.qr_client.activity.InventoryListActivity;
import com.lis.qr_client.activity.MainMenuActivity;
import com.lis.qr_client.data.DBHelper;
import com.lis.qr_client.utilities.Utility;
import com.lis.qr_client.utilities.adapter.InventoryAdapter;
import com.lis.qr_client.utilities.async_helpers.AsyncMultiDbManager;
import com.lis.qr_client.utilities.async_helpers.AsyncOneDbManager;
import lombok.extern.java.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Log
//TODO: make it abstract along with scan neightbor
public class ItemDialogFragment extends DialogFragment {
    public static final String ARG_TITLE = "title";

    private AlertDialog.Builder alertBuilder;
    private String inventory_num;
    private InventoryAdapter adapter;

    private DBHelper dbHelper;
    private SQLiteDatabase db;

    private String url;

    private Context context;
    private Utility utility = new Utility();

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        log.info("---Item on createDialog---");

        Bundle args = getArguments();

        String title = args.getString(ARG_TITLE);

        dbHelper = new DBHelper(context);
        db = dbHelper.getWritableDatabase();

        alertBuilder = new AlertDialog.Builder(getActivity());


        alertBuilder.setTitle(title)
                .setItems(R.array.itemsArray, dialogListener);
        return alertBuilder.create();

    }


    /*handle pressed item from itemsArray*/
    DialogInterface.OnClickListener dialogListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {

            /* get dialog array items resources */
            List<String> itemsOptionsArray = Arrays.asList(context.getResources().getStringArray(R.array.itemsArray));
            int item_info = itemsOptionsArray.indexOf(context.getResources().getString(R.string.item_info));
            int delete_from_list = itemsOptionsArray.indexOf(context.getResources().getString(R.string.delete_from_list));


            if (which == item_info) {
                log.info("Get full inventory info");

                fullItemInfo();

            }/* else if (which == delete_from_list) {
                deleteItemFromList();
            }*/ else {
                log.info("Back to list");
                dialog.dismiss();

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

    private void fullItemInfo() {
        /*knock-knock to server to get info*/
        String full_url = url + "/equipments/inventory_num/full/" + inventory_num;

        String table_name = "equipment";

        log.info("----Before load item's full info, save the data---");

        /*save scannedList and listToScan to the preferences*/
        InventoryListActivity inventoryActivity = (InventoryListActivity) context;

        if(inventoryActivity != null) {
            inventoryActivity.saveInventoryToPreferences(context, MainMenuActivity.PREFERENCE_FILE_NAME);
        }

        /*load full equipment data from server*/
        AsyncOneDbManager asyncOneDbManager = new AsyncOneDbManager(true, table_name, full_url,
                new Pair<String, Object>("inventory_num", inventory_num), context, dbHelper, db, EquipmentItemActivity.class);

        asyncOneDbManager.runAsyncOneDbManager();

    }

    private void deleteItemFromList() {

        //TODO: add one more dialog for deleting, like "r u sure, that u r sure u wanna delete it?"

        log.info("Delete from list " + inventory_num);

        utility.deleteInventoryFromList(adapter, inventory_num);

                 /*notifyDataChanged*/
        adapter.notifyDataSetChanged();

    }

    public void callDialog(Context context, Bundle bundle, InventoryAdapter adapter, String inventory_num, String tag) {
        log.info("-----callDialog-----");
        this.context = context;
        this.inventory_num = inventory_num;
        this.adapter = adapter;

        url = "http://" + context.getResources().getString(R.string.emu_ip) + ":" + context.getResources().getString(R.string.port);

        bundle.putString(ARG_TITLE, context.getResources().getString(R.string.choose_action));
        this.setArguments(bundle);

        this.show(((AppCompatActivity) context).getFragmentManager(), tag);

    }
}
