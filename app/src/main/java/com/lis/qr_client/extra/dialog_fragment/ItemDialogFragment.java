package com.lis.qr_client.extra.dialog_fragment;


import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Pair;
import com.lis.qr_client.R;
import com.lis.qr_client.activity.EquipmentItemActivity;
import com.lis.qr_client.activity.InventoryListActivity;
import com.lis.qr_client.activity.MainMenuActivity;
import com.lis.qr_client.application.QrApplication;
import com.lis.qr_client.constants.DbTables;
import com.lis.qr_client.constants.MyPreferences;
import com.lis.qr_client.data.DBHelper;
import com.lis.qr_client.extra.adapter.InventoryAdapter;
import com.lis.qr_client.extra.async_helpers.AsyncOneDbManager;
import com.lis.qr_client.extra.utility.Utility;
import lombok.extern.java.Log;
import org.springframework.http.HttpMethod;

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

    private String url;

    private Context context;
    private Utility utility = new Utility();

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        log.info("---Item on createDialog---");

        Bundle args = getArguments();

        String title = args.getString(ARG_TITLE);

        dbHelper = new DBHelper(QrApplication.getInstance());

        alertBuilder = new AlertDialog.Builder(getActivity());

        alertBuilder.setTitle(title)
                /*.setItems(R.array.itemsArray, dialogListener)*/
                .setMessage(R.string.show_full_info)
                .setPositiveButton(R.string.yes, dialogListener)
                .setNegativeButton(R.string.back, dialogListener);

        return alertBuilder.create();

    }


    /*handle pressed item from itemsArray*/
    DialogInterface.OnClickListener dialogListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {

            switch (which) {
                case Dialog.BUTTON_NEGATIVE: {
                    log.info("Back to list");
                    dialog.dismiss();
                    break;
                }
                case DialogInterface.BUTTON_POSITIVE: {
                    log.info("Get full inventory info");
                    fullItemInfo();
                    break;
             /*   default: {
            *//* get dialog array items resources *//*
                    List<String> itemsOptionsArray = Arrays.asList(context.getResources().getStringArray(R.array.itemsArray));
                    int item_info = itemsOptionsArray.indexOf(context.getResources().getString(R.string.item_info));

                    if (which == item_info) {
                        log.info("Get full inventory info");

                        fullItemInfo();

                    } else {
                        log.info("Back to list");
                        dialog.dismiss();

                    }*/
                }
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
        String full_url = url + context.getResources().getString(R.string.api_full_inventory_from_equipments)
                + inventory_num;

        String table_name = DbTables.TABLE_EQUIPMENT;

        log.info("----Before load item's full info, save the data---");

      /*  *//*save scannedList and listToScan to the preferences*//*
        InventoryListActivity inventoryActivity = (InventoryListActivity) context;

        if (inventoryActivity != null) {
            inventoryActivity.saveInventoryToPreferences(context, MyPreferences.PREFERENCE_FILE_NAME);
        }*/

        /*load full equipment data from server*/
        AsyncOneDbManager asyncOneManager = new AsyncOneDbManager
                (context, table_name, null, full_url, true,
                        EquipmentItemActivity.class, new int[]{Intent.FLAG_ACTIVITY_NEW_TASK}, new Pair<String, Object>("inventory_num", inventory_num),
                        null, HttpMethod.GET);

        asyncOneManager.runAsyncLoader();

    }

    public void callDialog(Context context, FragmentManager fragmentManager, Bundle bundle,
                           InventoryAdapter adapter, String inventory_num, String tag) {
        log.info("-----callDialog-----");
        this.context = context;
        this.inventory_num = inventory_num;
        this.adapter = adapter;

        url = "http://" + context.getResources().getString(R.string.emu_ip) + ":" + context.getResources().getString(R.string.port);

        bundle.putString(ARG_TITLE, context.getResources().getString(R.string.choose_action));
        this.setArguments(bundle);

        this.show(fragmentManager, tag);

    }
}
