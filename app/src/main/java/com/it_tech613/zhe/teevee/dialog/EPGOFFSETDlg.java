package com.it_tech613.zhe.teevee.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.NonNull;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.it_tech613.zhe.teevee.R;
import com.it_tech613.zhe.teevee.apps.Constants;
import com.it_tech613.zhe.teevee.apps.MyApp;

public class EPGOFFSETDlg extends Dialog implements View.OnClickListener{
    private ImageButton btn_left,btn_right;
    private TextView txt_current;
    private LinearLayout ly_click;
    private int epg_offset =0;
    private EpisodeDlgListener listener;
    Context context;
    public EPGOFFSETDlg(@NonNull final Context context, final EpisodeDlgListener listener){
        super(context);
        this.context = context;
        this.listener = listener;
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dlg_osd);
        getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        if (MyApp.instance.getPreference().get(Constants.EPG_OFFSET)!=null) epg_offset = (int) MyApp.instance.getPreference().get(Constants.EPG_OFFSET);
        btn_left = (ImageButton)findViewById(R.id.btn_left);
        btn_right = (ImageButton)findViewById(R.id.btn_right);
        btn_left .setOnClickListener(this);
        btn_right.setOnClickListener(this);

        txt_current = (TextView)findViewById(R.id.txt_current);
        txt_current.setText(String.valueOf(epg_offset));

        ly_click = (LinearLayout)findViewById(R.id.ly_click);
        ly_click.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_left:
               if(epg_offset >-12){
                   epg_offset--;
               }
                txt_current.setText(String.valueOf(epg_offset));
                break;
            case R.id.btn_right:
                if (epg_offset<12) {
                    epg_offset++;
                }
                txt_current.setText(String.valueOf(epg_offset));
                break;
            case R.id.ly_click:
                listener.OnYesClick(EPGOFFSETDlg.this, epg_offset);
                break;
        }
    }

    public interface EpisodeDlgListener{
        public void OnYesClick(Dialog dialog, int episode_num);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN){
            switch (event.getKeyCode()){
                case KeyEvent.KEYCODE_BACK:
                    dismiss();
                    break;
                case KeyEvent.KEYCODE_DPAD_LEFT:
                    if(epg_offset >-12){
                        epg_offset--;
                    }
                    txt_current.setText(String.valueOf(epg_offset));
                    listener.OnYesClick(EPGOFFSETDlg.this, epg_offset);
                    break;
                case KeyEvent.KEYCODE_DPAD_RIGHT:
                    if (epg_offset<12) {
                        epg_offset++;
                    }
                    txt_current.setText(String.valueOf(epg_offset));
                    listener.OnYesClick(EPGOFFSETDlg.this, epg_offset);
                    break;
                case KeyEvent.KEYCODE_DPAD_CENTER:
                    listener.OnYesClick(EPGOFFSETDlg.this, epg_offset);
                    break;
            }
        }
        return super.dispatchKeyEvent(event);
    }
}

