package com.lis.qr_client.extra.dialog_fragment;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.Toast;
import com.lis.qr_client.R;
import lombok.extern.java.Log;

@Log
public class ExitDialogFragment extends ScanDialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);


        log.info("Exit on createDialog");
        builder.setPositiveButton(getString(R.string.cancel), dialogListener);
        builder.setNegativeButton(getString(R.string.exit), dialogListener);
        return builder.create();
    }

    DialogInterface.OnClickListener dialogListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            log.info("Exit onClick");
            switch (which) {
                case Dialog.BUTTON_POSITIVE: {
                    Toast.makeText(getActivity(), getString(R.string.exit), Toast.LENGTH_SHORT).show();
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
