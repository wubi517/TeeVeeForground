package com.it_tech613.zhe.teevee.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.it_tech613.zhe.teevee.R;
import com.it_tech613.zhe.teevee.apps.SideMenu;

import java.util.List;

import kotlin.Unit;
import kotlin.jvm.functions.Function3;


public class MultiScreenRecycleAdapter extends RecyclerView.Adapter<MultiScreenRecycleAdapter.HomeListViewHolder> {
    List<SideMenu> list;
    Context context;
    Function3<SideMenu,Integer, Boolean,Unit> clickeListenerFunction;
    int selected_pos = 0;
    int checked_pos = 0;
    public MultiScreenRecycleAdapter(List<SideMenu> list, Context context, Function3<SideMenu, Integer,Boolean, Unit> clickeListenerFunction) {
        this.list = list;
        this.context = context;
        this.clickeListenerFunction = clickeListenerFunction;
    }

    @NonNull
    @Override
    public HomeListViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new HomeListViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_multi_screen,viewGroup,false));
    }

    @Override
    public void onBindViewHolder(@NonNull final HomeListViewHolder viewHolder, @SuppressLint("RecyclerView") final int i) {
        viewHolder.country.setText(list.get(i).getName());
        viewHolder.itemView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus){
                    viewHolder.itemView.setBackgroundResource(R.drawable.round_white1);
                }else {
                    viewHolder.itemView.setBackgroundResource(R.drawable.round_white);
                }
            }
        });

        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onToggleSelected(i);
                clickeListenerFunction.invoke(list.get(i), i,true);
            }
        });

        if(checked_pos==i){
            viewHolder.itemView.requestFocus();
            viewHolder.main_lay.setBackgroundResource(R.drawable.round_white1);
        }else {
            viewHolder.main_lay.setBackgroundResource(R.drawable.round_white);
        }
        if(checked_pos == i){
            viewHolder.radioButton.setChecked(true);
        }else {
            viewHolder.radioButton.setChecked(false);
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class HomeListViewHolder extends RecyclerView.ViewHolder{
        private RelativeLayout main_lay ;
        private TextView country ;
        private RadioButton radioButton;

        public HomeListViewHolder(@NonNull View itemView) {
            super(itemView);
            main_lay = itemView.findViewById(R.id.main_lay);
            country = itemView.findViewById(R.id.textView);
            radioButton = itemView.findViewById(R.id.radio_button);
        }
    }

    public void selectItem(int pos) {
        selected_pos = pos;
        notifyItemChanged(selected_pos);
    }

    private void onToggleSelected(int pos){
        checked_pos = pos;
        notifyDataSetChanged();
    }
}
