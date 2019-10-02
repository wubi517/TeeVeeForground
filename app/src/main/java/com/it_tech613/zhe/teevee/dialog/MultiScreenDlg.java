package com.it_tech613.zhe.teevee.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.Window;
import android.widget.Button;

import com.it_tech613.zhe.teevee.R;
import com.it_tech613.zhe.teevee.adapter.MultiScreenRecycleAdapter;
import com.it_tech613.zhe.teevee.apps.SideMenu;

import java.util.ArrayList;
import java.util.List;

import kotlin.Unit;
import kotlin.jvm.functions.Function3;

public class MultiScreenDlg extends Dialog  implements View.OnClickListener {
    Context context;
    DialogSearchListener listener;
    private Button btn_ok,btn_cancel;
    private List<SideMenu> sideMenus;
    private RecyclerView multi_recylerview;
    private MultiScreenRecycleAdapter adapter;
    private int seelcted_pos=0;
    public MultiScreenDlg(@NonNull Context context, final DialogSearchListener listener) {
        super(context);
        this.context = context;
        this.listener = listener;
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dlg_multi_screen);
        getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        multi_recylerview = findViewById(R.id.multi_view);
        btn_ok = findViewById(R.id.btn_ok);
        btn_cancel = findViewById(R.id.btn_cancel);
        btn_ok.setOnClickListener(this);
        btn_cancel.setOnClickListener(this);

        sideMenus = new ArrayList<>();
        sideMenus.add(new SideMenu("Four Way Screen"));
        sideMenus.add(new SideMenu("Three Way Screen"));
        sideMenus.add(new SideMenu("Dual Screen"));

        adapter = new MultiScreenRecycleAdapter(sideMenus, getContext(), new Function3<SideMenu, Integer, Boolean, Unit>() {
            @Override
            public Unit invoke(SideMenu sideMenu, Integer position, Boolean is_clicked) {
                seelcted_pos = position;
                return null;
            }
        });
        multi_recylerview.setAdapter(adapter);
        multi_recylerview.setLayoutManager(new LinearLayoutManager(getContext()));
        multi_recylerview.requestFocus();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_ok:
                listener.OnSearchClick(MultiScreenDlg.this,seelcted_pos,true);
                break;
            case R.id.btn_cancel:
                listener.OnSearchClick(MultiScreenDlg.this,seelcted_pos,false);
                break;
        }
    }


    public interface DialogSearchListener {
        public void OnSearchClick(Dialog dialog, Integer sel_Channel,Boolean is_clicked);
    }
}
