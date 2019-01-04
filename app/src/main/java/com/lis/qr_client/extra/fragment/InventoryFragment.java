package com.lis.qr_client.extra.fragment;

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
import com.lis.qr_client.pojo.UniversalSerializablePojo;
import com.lis.qr_client.extra.adapter.InventoryAdapter;
import lombok.extern.java.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Log
public class InventoryFragment extends Fragment {
    public static final String ARGUMENT_SCAN_LIST = "arg_scan_list";



    private List<Map<String, Object>> show_list = new ArrayList<>();
    private InventoryAdapter adapter;
    private RecyclerView rv;

    public static InventoryFragment newInstance(UniversalSerializablePojo scanListPojo) {
        InventoryFragment inventoryFragment = new InventoryFragment();
        Bundle argument = new Bundle();

        argument.putSerializable(ARGUMENT_SCAN_LIST, scanListPojo);
        inventoryFragment.setArguments(argument);
        return inventoryFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
            UniversalSerializablePojo univeral_pojo = (UniversalSerializablePojo)
                    getArguments().getSerializable(ARGUMENT_SCAN_LIST);
            show_list = univeral_pojo.getMapList();

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {


        rv = (RecyclerView) inflater.inflate(R.layout.scanlist_recycler, null);

        /*upload list from db*/

        adapter = new InventoryAdapter(getContext(), show_list);

        rv.setAdapter(adapter);
        rv.setHasFixedSize(true);

        /*Form of the output list*/
        rv.setLayoutManager(new LinearLayoutManager(getActivity()));

        //TODO:make custom decoration
        DividerItemDecoration divider = new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL);
        divider.setDrawable(getResources().getDrawable( R.drawable.divider));
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
