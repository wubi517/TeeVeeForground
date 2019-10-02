package com.it_tech613.zhe.teevee.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.NonNull;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;

import com.it_tech613.zhe.teevee.R;

public class ConnectionDlg extends Dialog {

    public ConnectionDlg(@NonNull final Context context, final DialogConnectionListener listener) {
        super(context);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dlg_connection);
        getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        findViewById(R.id.dlg_con_retry_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.OnRetryClick(ConnectionDlg.this);
            }
        });
        findViewById(R.id.dlg_con_help_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.OnHelpClick(ConnectionDlg.this);
            }
        });
    }

    public interface DialogConnectionListener {
        public void OnRetryClick(Dialog dialog);
        public void OnHelpClick(Dialog dialog);
    }

    @Override
    public boolean dispatchKeyEvent(@NonNull KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_MENU) {
            dismiss();
        }
        return super.dispatchKeyEvent(event);
    }
}
