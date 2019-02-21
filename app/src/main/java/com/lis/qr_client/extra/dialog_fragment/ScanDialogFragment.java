package com.lis.qr_client.extra.dialog_fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.Gravity;
import android.widget.TextView;
import android.widget.Toast;
import com.lis.qr_client.R;
import lombok.extern.java.Log;

@Log
public class ScanDialogFragment extends android.support.v4.app.DialogFragment {
    public static final String ARG_TITLE = "ScanDialogFragment.Title";
    public static final String ARG_MESSAGE = "ScanDialogFragment.Message";
    public static final String ARG_ICON = "ScanDialogFragment.Icon";
    protected AlertDialog.Builder builder;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        log.info("Scan on createDialog");

        /*Ger all arguments*/
        Bundle args = getArguments();
        builder = new AlertDialog.Builder(getActivity(), R.style.MyDialogs);

        if (args != null) {
            String title = args.getString(ARG_TITLE);
            String message = args.getString(ARG_MESSAGE);
            int icon = args.getInt(ARG_ICON);

        /*build new dialog with arguments*/

            builder.setMessage(message);

            if (icon != 0) {
                builder.setIcon(icon);
            }
           /*set positive/negative buttons*/
            builder.setTitle(title)
                    .setPositiveButton(R.string.ok, dialogListener)
                    .setTitle(title);
        } else {
            log.info("Args are null");
        }

        return builder.create();

    }

    @Override
    public void onStart() {
        super.onStart();
        log.info("-----Start dialog-----");
        final TextView textView = getDialog().findViewById(android.R.id.message);
        textView.setTextSize(18);

        textView.setGravity(Gravity.CENTER_VERTICAL);
        textView.setPadding(90,textView.getPaddingTop(),textView.getPaddingRight(),0);

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        log.info("-----Attach-----");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        log.info("-----Detach-----");
    }

    private DialogInterface.OnClickListener dialogListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            log.info("Scan onClick");
            switch (which) {
                case Dialog.BUTTON_POSITIVE: {
                    Toast.makeText(getActivity(), getString(R.string.ok), Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
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

    public void callDialog(FragmentManager fragmentManager, Bundle bundle, String msg, String title, int drawable_icon,
                           String tag) {
        bundle.putString(ScanDialogFragment.ARG_TITLE, title);
        bundle.putString(ScanDialogFragment.ARG_MESSAGE, msg);
        bundle.putInt(ScanDialogFragment.ARG_ICON, drawable_icon);

        log.info("-----Call dialog-----");
        log.info("-----Show args-----" + bundle.size());


        /*FragmentManager fragmentManager  =((AppCompatActivity) context).getFragmentManager();
        Fragment old_fragment = fragmentManager.getFragment(bundle, tag);

        *//*avoid duplicate fragment error*//*
        if(old_fragment != null){
            fragmentManager.beginTransaction().remove(old_fragment).commit();
        }*/
        this.setArguments(bundle);

        /*even if Jesus asks u, don't put the Activity instead of the AppCompatActivity*/
        this.show(fragmentManager, tag);

    }

}
