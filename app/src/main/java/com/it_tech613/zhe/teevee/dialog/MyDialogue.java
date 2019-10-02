package com.it_tech613.zhe.teevee.dialog;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.it_tech613.zhe.teevee.R;

public class MyDialogue extends DialogFragment {

    public MyDialogue(){}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, android.R.style.Theme_DeviceDefault_Light_Dialog);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.sync_progress_dialogue, container, false);
        ((TextView)view.findViewById(R.id.title)).setText(getArguments().getString("title"));
        ((TextView)view.findViewById(R.id.message)).setText(getArguments().getString("message"));
        return view;
    }
}
