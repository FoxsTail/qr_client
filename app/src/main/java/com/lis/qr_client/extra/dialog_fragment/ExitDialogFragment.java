package com.lis.qr_client.extra.dialog_fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Menu;
import android.widget.TextView;
import android.widget.Toast;
import com.lis.qr_client.R;
import com.lis.qr_client.application.QrApplication;
import lombok.extern.java.Log;

@Log
public class ExitDialogFragment extends ScanDialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);

        log.info("Exit on createDialog");

        builder.setPositiveButton(getString(R.string.no), dialogListener);
        builder.setNegativeButton(getString(R.string.yes), dialogListener);

         return builder.create();

    }


    /*reverse*/
    DialogInterface.OnClickListener dialogListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            log.info("Exit onClick");
            switch (which) {
                case Dialog.BUTTON_POSITIVE: {
                    /*stay*/
                    Toast.makeText(getActivity(), getString(R.string.stay), Toast.LENGTH_SHORT).show();
                }
                break;
                case Dialog.BUTTON_NEGATIVE: {
                    /*go away*/
                    dismiss();
                    getActivity().finish();
                }
                break;
            }
        }
    };
}
