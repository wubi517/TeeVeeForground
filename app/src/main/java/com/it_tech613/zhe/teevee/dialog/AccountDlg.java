package com.it_tech613.zhe.teevee.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.NonNull;
import android.view.KeyEvent;
import android.view.Window;
import android.widget.TextView;

import com.it_tech613.zhe.teevee.R;
import com.it_tech613.zhe.teevee.apps.Constants;
import com.it_tech613.zhe.teevee.apps.MyApp;
import com.it_tech613.zhe.teevee.models.LoginModel;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by RST on 7/27/2017.
 */

public class AccountDlg extends Dialog {
    private LoginModel loginModel = (LoginModel) MyApp.instance.getPreference().get(Constants.LOGIN_INFO);
    TextView user,txt_exp_date,created_at,is_trial,active_connections,max_connections,account_state;
    long milli_second = 0,mili_created=0;
    private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    public AccountDlg(@NonNull Context context, final DialogMacListener listener) {
        super(context);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dlg_account);
        getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        txt_exp_date = (TextView)findViewById(R.id.txt_expire);
        user = (TextView)findViewById(R.id.username);
        created_at = (TextView)findViewById(R.id.txt_created);
        is_trial = (TextView)findViewById(R.id.txt_trial);
        active_connections = (TextView)findViewById(R.id.txt_connection);
        max_connections = (TextView)findViewById(R.id.txt_max);
        account_state = (TextView)findViewById(R.id.txt_active);
        Calendar cl = Calendar.getInstance();
        try{
            milli_second = Long.parseLong(loginModel.getExp_date())*1000;
            cl.setTimeInMillis(milli_second);
            String date1 = dateFormat.format(cl.getTime());
            txt_exp_date.setText(date1);
        }catch (Exception e){
            txt_exp_date.setText("unlimited");
        }
        try {
            mili_created = Long.parseLong(MyApp.created_at);
            cl.setTimeInMillis(mili_created);
            String date2 = dateFormat.format(cl.getTime());
            created_at.setText(date2);
        }catch (Exception e){
            created_at.setText("");
        }
        is_trial.setText(MyApp.is_trail);
        account_state.setText(MyApp.status);
        user.setText(MyApp.user);
        if(MyApp.status.equals("Active")){
            account_state.setBackgroundColor(getContext().getResources().getColor(R.color.red));
        }else {
            account_state.setBackgroundColor(getContext().getResources().getColor(R.color.gray));
        }
        active_connections.setText(MyApp.active_cons);
        max_connections.setText(MyApp.max_cons);

    }

    public interface DialogMacListener {
        public void OnYesClick(Dialog dialog);
    }

    @Override
    public boolean dispatchKeyEvent(@NonNull KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            dismiss();
        }
        if(event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_DPAD_CENTER){
            dismiss();
        }
        return super.dispatchKeyEvent(event);
    }
}
