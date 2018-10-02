package com.lis.qr_client.utilities.dialog_fragment;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import lombok.extern.java.Log;

@Log
public class ExitDialogFragment extends ScanDialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);
log.info("Exit on createDialog");
        builder.setPositiveButton("Cancel", dialogListener);
        builder.setNegativeButton("Exit", dialogListener);
    return builder.create();
    }

    DialogInterface.OnClickListener dialogListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            log.info("Exit onClick");
            switch (which) {
                case Dialog.BUTTON_POSITIVE: {
                    Toast.makeText(getActivity(), "Ok", Toast.LENGTH_SHORT).show();
                }
                break;
                case Dialog.BUTTON_NEGATIVE: {
                    dismiss();
                    getActivity().finish();
                }
                break;
            }
        }
    };
}
