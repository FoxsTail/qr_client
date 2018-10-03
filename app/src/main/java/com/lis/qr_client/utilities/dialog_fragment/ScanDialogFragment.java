package com.lis.qr_client.utilities.dialog_fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;
import com.lis.qr_client.R;
import lombok.extern.java.Log;

@Log
public class ScanDialogFragment extends DialogFragment {
    public static final String ARG_TITLE = "ScanDialogFragment.Title";
    public static final String ARG_MESSAGE = "ScanDialogFragment.Message";
    protected AlertDialog.Builder builder;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        log.info("Scan on createDialog");

        Bundle args = getArguments();
        String title = args.getString(ARG_TITLE);
        String message = args.getString(ARG_MESSAGE);


        builder = new AlertDialog.Builder(getActivity());

           /*set positive/negative buttons*/
        builder.setTitle(title)
                .setPositiveButton(R.string.ok, dialogListener)
               // .setNegativeButton(R.string.cancel, dialogListener)
                .setTitle(title);

        builder.setMessage(message);

        return builder.create();
    }

    private DialogInterface.OnClickListener dialogListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            log.info("Scan onClick");
            switch (which) {
                case Dialog.BUTTON_POSITIVE: {
                    Toast.makeText(getActivity(), "Ok", Toast.LENGTH_SHORT).show();
                }
                break;
                case Dialog.BUTTON_NEGATIVE: {
                    dismiss();
                }
                break;
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

    public void callDialog(Context context, DialogFragment dialogFragment, Bundle bundle, String msg, String tag){
        bundle.putString(ScanDialogFragment.ARG_TITLE, context.getResources().getString(R.string.inventory_scan_result));
        bundle.putString(ScanDialogFragment.ARG_MESSAGE, msg);

        dialogFragment.setArguments(bundle);
        /*even if Jesus asks u, don't put the Activity instead of the AppCompatActivity*/
        dialogFragment.show( ((AppCompatActivity) context).getFragmentManager(), tag);
    }

}
