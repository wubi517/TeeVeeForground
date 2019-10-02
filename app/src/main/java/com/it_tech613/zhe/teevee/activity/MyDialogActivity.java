package com.it_tech613.zhe.teevee.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import com.it_tech613.zhe.teevee.R;
import com.it_tech613.zhe.teevee.dialog.MyDialogue;

public class MyDialogActivity extends Activity {
    MyDialogue dialogue;
    Bundle bundle;
    AlertDialog dialog;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_my_dialog);
//        final AlertDialog alertDialog = new AlertDialog.Builder(this).create();
//        alertDialog.setTitle("your title");
//        alertDialog.setMessage("your message");
//        alertDialog.setIcon(R.mipmap.ic_launcher);
//        alertDialog.show();

        dialog = new AlertDialog.Builder(this).create();
        dialog.setIcon(R.drawable.icon);
        dialog.setTitle(getIntent().getExtras().getString("title"));
        dialog.setMessage(getIntent().getExtras().getString("message"));
        //dialog.setIcon(R.drawable.icon);
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                finish();
            }
        });
        dialog.show();

//        dialogue = new MyDialogue();
//        bundle = new Bundle();
//        bundle.putString("title",getIntent().getExtras().getString("title"));
//        bundle.putString("message",getIntent().getExtras().getString("message"));
//        dialogue.setArguments(bundle);
//        dialogue.show(getSupportFragmentManager(), "tag");
    }
}