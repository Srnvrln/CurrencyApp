package com.example.currencyapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class MainPageAdapter extends RecyclerView.Adapter<MainPageAdapter.AdapterVH> {

       private Context context;
       private ArrayList<String> countryAndLocalCurrency;
       private ArrayList<Double>rates;
       private String baseCoin ;


       public MainPageAdapter(Context context, String baseCoin){
           this.context = context;
           this.baseCoin = baseCoin;
           countryAndLocalCurrency = new ArrayList<>();
           rates = new ArrayList<>();

       }

    @NonNull
    @Override
    public AdapterVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
           View view = LayoutInflater.from(context).inflate(R.layout.main_list_item,parent,false);
           return new AdapterVH(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull AdapterVH holder, int position) {
        String item = countryAndLocalCurrency.get(position);

        for(int i = 0; i<item.length(); i++){
            if(item.charAt(i) == ','){
                holder.showCountry.setText(item.substring(0,i).trim());
                holder.showLocalCurrency.setText(item.substring(i+1).trim());
            }
        }
        holder.showRate.setText(rates.get(position).toString());
        String showBaseCoin = "Base : "+baseCoin;
        holder.showBaseCoin.setText(showBaseCoin);
    }



    @Override
    public int getItemCount() {
        return countryAndLocalCurrency.size();
    }

    public void setCountryAndLocalCurrency(ArrayList<String> countryAndLocalCurrency) {
        this.countryAndLocalCurrency = countryAndLocalCurrency;
    }

    public void setRates(ArrayList<Double> rates) {
        this.rates = rates;
    }

    public void setBaseCoin(String baseCoin) {
        this.baseCoin = baseCoin;
    }

    public static class AdapterVH extends RecyclerView.ViewHolder{

           public TextView showCountry;
           public TextView showLocalCurrency;
           public TextView showBaseCoin;
           public TextView showRate;

        public AdapterVH(@NonNull View itemView) {
            super(itemView);

            showCountry = itemView.findViewById(R.id.country);
            showLocalCurrency = itemView.findViewById(R.id.localcurrency);
            showBaseCoin = itemView.findViewById(R.id.base);
            showRate = itemView.findViewById(R.id.rate);
        }
    }


}
