package com.lis.qr_client.extra.fragment;

import android.app.FragmentManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.lis.qr_client.R;
import com.lis.qr_client.application.QrApplication;
import com.lis.qr_client.extra.adapter.InventoryAdapter;
import com.lis.qr_client.pojo.UniversalSerializablePojo;
import lombok.extern.java.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Log
public class InventoryFragment extends Fragment {
    public static final String ARGUMENT_SCAN_LIST = "arg_scan_list";


    static FragmentManager fragmentManager;
    private List<Map<String, Object>> show_list = new ArrayList<>();
    private InventoryAdapter adapter;
    private RecyclerView rv;

    public static InventoryFragment newInstance(UniversalSerializablePojo scanListPojo, FragmentManager fragmentManager) {
        log.info("InventoryFragment newInstance");
        InventoryFragment inventoryFragment = new InventoryFragment();
        Bundle argument = new Bundle();

        argument.putSerializable(ARGUMENT_SCAN_LIST, scanListPojo);
        inventoryFragment.setArguments(argument);
        InventoryFragment.fragmentManager = fragmentManager;
        return inventoryFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        log.info("InventoryFragment onCreate");

        UniversalSerializablePojo univeral_pojo = (UniversalSerializablePojo)
                getArguments().getSerializable(ARGUMENT_SCAN_LIST);
        show_list = univeral_pojo.getMapList();


        //retain from reload
        //  setRetainInstance(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        log.info("InventoryFragment onCreateView");
        rv = (RecyclerView) inflater.inflate(R.layout.scanlist_recycler, null);

        /*upload list from db*/

        adapter = new InventoryAdapter(QrApplication.getInstance(), show_list, fragmentManager);
        log.info("InventoryFragment onCreateView adapter is ");
        if(adapter == null){
            log.info("NULL");

        }else{
            log.info("NOT NULL");
        }

        rv.setAdapter(adapter);
        rv.setHasFixedSize(true);

        /*Form of the output list*/
        rv.setLayoutManager(new LinearLayoutManager(getActivity()));

        //TODO:make custom decoration
        DividerItemDecoration divider = new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL);
        divider.setDrawable(getResources().getDrawable(R.drawable.drawable_divider_list));
        rv.addItemDecoration(divider);

        return rv;
    }

    public InventoryAdapter getAdapter() {
        return adapter;
    }

    public RecyclerView getRv() {
        return rv;
    }
}
