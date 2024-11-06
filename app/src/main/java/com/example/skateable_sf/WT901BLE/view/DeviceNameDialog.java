package com.example.skateable_sf.WT901BLE.view;

import android.os.Bundle;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import butterknife.ButterKnife;
import butterknife.BindView;
import butterknife.OnClick;
import com.example.skateable_sf.R2;


/**
 * Created by 葛文博 on 2017/10/23.
 */
public class DeviceNameDialog extends BDialog {

    @BindView(R2.id.et_putPs)
    EditText et_passWord;

    PsDialogCallBack psDialogCallBack;

    public void setPsDialogCallBack(PsDialogCallBack psDialogCallBack) {
        this.psDialogCallBack = psDialogCallBack;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R2.layout.ps_dialog, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }


    public DeviceNameDialog() {

    }

    public static DeviceNameDialog newIntence() {
        DeviceNameDialog passWordDialog = new DeviceNameDialog();
        return passWordDialog;
    }


    @OnClick(R2.id.bt_sure)
    void sure() {
        String str = et_passWord.getText().toString();
        if (psDialogCallBack != null) {
            psDialogCallBack.sure(str);
        }
        dismiss();
    }

    @OnClick(R2.id.bt_abolish)
    void abolish() {
        if (psDialogCallBack != null) {
            psDialogCallBack.abolish();
        }
        dismiss();
    }

    public interface PsDialogCallBack {

        void sure(String value);

        void abolish();

    }

}
