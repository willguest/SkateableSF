package com.example.skateable_sf.WT901BLE.view;

import android.os.Bundle;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;



import com.example.skateable_sf.WT901BLE.databinding.PsDialogBinding;

/**
 * Created by 葛文博 on 2017/10/23.
 */
public class DeviceNameDialog extends BDialog {

    EditText et_passWord;
    PsDialogCallBack psDialogCallBack;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        com.example.skateable_sf.WT901BLE.databinding.PsDialogBinding binding = PsDialogBinding.inflate(getLayoutInflater());
        binding.btAbolish.setOnClickListener(v -> {
            if (psDialogCallBack != null) {
                psDialogCallBack.abolish();
            }
            dismiss();
        });
        binding.btSure.setOnClickListener(v -> {
            String str = et_passWord.getText().toString();
            if (psDialogCallBack != null) {
                psDialogCallBack.sure(str);
            }
            dismiss();
        });
        et_passWord = binding.etPutPs;
        return binding.getRoot();
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

    public interface PsDialogCallBack {

        void sure(String value);

        void abolish();

    }
}
