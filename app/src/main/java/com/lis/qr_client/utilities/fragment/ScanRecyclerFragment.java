package com.lis.qr_client.utilities.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.lis.qr_client.R;
import com.lis.qr_client.utilities.adapter.InventoryAdapter;
import lombok.extern.java.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Log
public class ScanRecyclerFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        RecyclerView rv = (RecyclerView) inflater.inflate(R.layout.scanlist_recycler, container, false);


        List<Map<String, Object>> scan_list = new ArrayList<>();
        Map<String, Object> map = new HashMap<>();
        map.put("Scan", "Ololo");
        scan_list.add(map);


        map = new HashMap<>();
        map.put("Scan", "Ass");
        scan_list.add(map);

        for (int i = 0; i < 10; i++) {
            map = new HashMap<>();
            map.put("Scan", "Kadabra");
            map.put("Scan", "Ololo");
            map.put("Scan", "Ass");
            scan_list.add(map);
        }

        for(Map<String,Object> m: scan_list){
            log.info("Map....: "+ m.toString());
        }

        InventoryAdapter adapter = new InventoryAdapter(scan_list);

        rv.setAdapter(adapter);
        rv.setHasFixedSize(true);

        /*Form of the output list*/
        rv.setLayoutManager(new GridLayoutManager(getActivity(), 2));


        return rv;
    }
}
