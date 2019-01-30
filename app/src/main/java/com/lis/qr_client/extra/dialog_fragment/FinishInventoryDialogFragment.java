package com.lis.qr_client.extra.dialog_fragment;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.Toast;
import com.lis.qr_client.R;
import com.lis.qr_client.application.QrApplication;
import com.lis.qr_client.constants.MyBundle;
import com.lis.qr_client.constants.MyPreferences;
import com.lis.qr_client.extra.utility.ObjectUtility;
import com.lis.qr_client.pojo.UniversalSerializablePojo;
import lombok.extern.java.Log;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Log
public class FinishInventoryDialogFragment extends ScanDialogFragment {
    List<Map<String, Object>> to_scan_list;
    List<Map<String, Object>> scanned_list;
    String room = null;


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);

        log.info("FinishInventory on createDialog");


        /*get list's from bundle*/
        Bundle bundle = getArguments();
        if (bundle != null) {
            room = bundle.getString(MyPreferences.ROOM_ID_PREFERENCES, null);
            to_scan_list = extractElementsFromBundle(bundle, MyBundle.TO_SCAN_LIST);
            scanned_list = extractElementsFromBundle(bundle, MyBundle.SCANNED_LIST);

        } else {
            log.info("Bundle is null");
        }


        builder.setPositiveButton(R.string.finish, onClickListener);
        builder.setNegativeButton(R.string.cancel, onClickListener);
        return builder.create();
    }

    DialogInterface.OnClickListener onClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            log.info("FinishInventory onClick");
            switch (which) {
                case Dialog.BUTTON_POSITIVE: {
                    /*save data to csv etc*/
                    Toast.makeText(QrApplication.getInstance(), getString(R.string.ok), Toast.LENGTH_SHORT).show();
                    ObjectUtility.convertAndSaveListsToCsvFile(room, new String[]{MyBundle.TO_SCAN_LIST,
                            MyBundle.SCANNED_LIST}, to_scan_list, scanned_list, null);
                }
                break;
                case Dialog.BUTTON_NEGATIVE: {
                    dismiss();
                }
                break;
            }
        }
    };

    public List<Map<String, Object>> extractElementsFromBundle(Bundle bundle, String list_name) {
        log.info("extractElementsFromBundle");
        Serializable serializable_bundle = bundle.getSerializable(list_name);

            /*extract to_scan to map list*/
        if (serializable_bundle != null) {
            UniversalSerializablePojo universalBundlePojo = (UniversalSerializablePojo) serializable_bundle;
            return universalBundlePojo.getMapList();
        } else {
            log.info("To scan list is null");
        }
        return null;
    }


}
