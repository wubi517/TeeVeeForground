package com.it_tech613.zhe.teevee.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;

import com.it_tech613.zhe.teevee.R;
import com.it_tech613.zhe.teevee.apps.Constants;
import com.it_tech613.zhe.teevee.apps.MyApp;

/**
 * Created by RST on 3/23/2018.
 */

public class TimeFormatDlg extends Dialog implements View.OnClickListener{
    CheckBox checkBox1,checkBox2;
    Button btn_ok,btn_cancel;
    DialogUpdateListener listener;
    public TimeFormatDlg(@NonNull Context context, final DialogUpdateListener listener) {
        super(context);
        this.listener = listener;
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dlg_time_format);
        getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        checkBox1 = findViewById(R.id.checkbox1);
        checkBox2 = findViewById(R.id.checkbox2);
        checkBox1.setOnClickListener(this);
        checkBox2.setOnClickListener(this);
        String time_format = (String) MyApp.instance.getPreference().get(Constants.TIME_FORMAT);
        if(time_format.equalsIgnoreCase("12hour")){
            checkBox1.setChecked(true);
            checkBox2.setChecked(false);
        }else {
            checkBox2.setChecked(true);
            checkBox1.setChecked(false);
        }
        btn_ok = findViewById(R.id.btn_ok);
        btn_cancel = findViewById(R.id.btn_cancel);
        btn_ok.setOnClickListener(this);
        btn_cancel.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_ok:
                if(checkBox1.isChecked()){
                    MyApp.instance.getPreference().put(Constants.TIME_FORMAT,"12hour");
                }else {
                    MyApp.instance.getPreference().put(Constants.TIME_FORMAT,"24hour");
                }
                listener.OnUpdateNowClick(TimeFormatDlg.this);
                break;
            case R.id.btn_cancel:
                listener.OnUpdateSkipClick(TimeFormatDlg.this);
                break;
            case R.id.checkbox1:
                checkBox1.setChecked(true);
                checkBox2.setChecked(false);
                break;
            case R.id.checkbox2:
                checkBox1.setChecked(false);
                checkBox2.setChecked(true);
                break;
        }
    }

    public interface DialogUpdateListener {
        public void OnUpdateNowClick(Dialog dialog);
        public void OnUpdateSkipClick(Dialog dialog);
    }
}
