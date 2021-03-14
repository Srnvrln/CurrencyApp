package com.example.currencyapp;

import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

import java.util.ArrayList;

public class CustomDialog extends AppCompatDialogFragment {

    private String [] arrayOfCountryAndCurrency;
    private String [] listOfCurrencyInternationalCode;


    private onDialogItemListener listener;

    public CustomDialog(ArrayList<String>list){
        arrayOfCountryAndCurrency = convertToArrayandSeparate(list);

    }

    public CustomDialog(){

    }

    @NonNull
    @Override
    public android.app.Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Select Base Coin")
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setItems(arrayOfCountryAndCurrency, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                                listener.onItemClick(listOfCurrencyInternationalCode[which]);
                    }
                });

        return builder.create();
    }

    public void setList(ArrayList<String> list) {
        arrayOfCountryAndCurrency = convertToArrayandSeparate(list);
    }


    public interface onDialogItemListener {
        void onItemClick(String coin);
    }

    public void setListener(onDialogItemListener listener){
        this.listener = listener;
    }

    //separating country name from local currency & currency code
    private String [] convertToArrayandSeparate(ArrayList<String> list){
        String [] toSend = new String [list.size()];
        listOfCurrencyInternationalCode = new String[list.size()];
        for(int i = 0; i<list.size(); i++){
        String item = list.get(i);
        toSend[i] = item;
        for(int j = 0; j<item.length();j++){
            if(item.charAt(j) == '-') listOfCurrencyInternationalCode[i] = item.substring(j+1);
            System.out.println(toSend[i]);
        }

        }

        return toSend;
    }
}
