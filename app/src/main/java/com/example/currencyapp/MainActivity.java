package com.example.currencyapp;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    // initialized baseCoin & refreshingRate with default values
    private String baseCoin = "EUR";
    private int refreshingRate = 3000;
    private String lastDate = "";


    private  final Handler refreshHandling = new Handler(Looper.getMainLooper());
    Runnable runnable;

    private ArrayList<String> countryAndLocalCurrency;
    private ArrayList<Double>rates;

    private MainPageAdapter rwMainPageAdapter;

    private TextView showUpdate;
    private CustomDialog customDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "onCreate");

        generalInitialization();
        initializeRecyclerView();

        if(savedInstanceState != null){

            if(checkConnection()){
                baseCoin = savedInstanceState.getString("baseCoin");
                refreshingRate = savedInstanceState.getInt("refreshRate");
            }
            else{

            baseCoin = savedInstanceState.getString("baseCoin");
            refreshingRate = savedInstanceState.getInt("refreshRate");
            lastDate = savedInstanceState.getString("lastDate");
            countryAndLocalCurrency = savedInstanceState.getStringArrayList("currencyData");

            double [] array = savedInstanceState.getDoubleArray("rates");
            for(int i = 0; i<array.length; i++) {
                rates.add(array[i]);
            }
            rwMainPageAdapter.setCountryAndLocalCurrency(countryAndLocalCurrency);
            rwMainPageAdapter.setRates(rates);
            rwMainPageAdapter.notifyDataSetChanged();
            showUpdate.setText(lastDate);
            }
        }

    }

    private void initializeRecyclerView(){
        Log.d(TAG, "initializeRecyclerView");

            RecyclerView rwMainList = findViewById(R.id.currencyList);
            rwMainPageAdapter = new MainPageAdapter(this,baseCoin);
            RecyclerView.LayoutManager rwManager = new LinearLayoutManager(this);

            rwMainList.setAdapter(rwMainPageAdapter);
            rwMainList.setLayoutManager(rwManager);
            rwMainList.addItemDecoration(new RWItemSpacing(10,null));

    }

    private void generalInitialization(){
        Log.d(TAG, "generalInitialization");

        countryAndLocalCurrency = new ArrayList<>();
        rates = new ArrayList<>();
        showUpdate = findViewById(R.id.showLastUpdate);
        customDialog = new CustomDialog();

        /**custom listener declared in CustomDialog class
         * to communicate between user coin choice and Main Activity
         * upon user click
         * @param String coinName
           */
        customDialog.setListener(new CustomDialog.onDialogItemListener() {
            @Override
            public void onItemClick(String coin) {

                if(!checkConnection()) showToast("No internet connection ! List can not be updated with "+coin);
                else{ baseCoin = coin;
                    rwMainPageAdapter.setBaseCoin(coin);
                    showToast("Base coin changed to "+coin);}
            }
        });


         /* Initialize button for launching a popupwindow for last 10days charts
         *  conditioning that it launches only if there is internet connection available    */
        Button startCharts = findViewById(R.id.graph);
         startCharts.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                     if(checkConnection())  startPopUpWindow(v);
                            else showToast("No internet connection ! Can not retrieve data to chart coin evolution");
             }
         });



    }

    private void downloadContent(){
        Log.d(TAG, "Started donwloading");

        String url = "https://api.exchangeratesapi.io/latest";
        final StringBuilder json = new StringBuilder();

        //change url according to user choice
        if(!baseCoin.equals("EUR")) url = url+"?base="+baseCoin;

        OkHttpClient client = new OkHttpClient();
        Request req = new Request.Builder()
                      .url(url)
                      .build();
        client.newCall(req).enqueue(new Callback() {

            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                     if(response.isSuccessful()) {
                        json.append(response.body().string());
                         Log.d(TAG, " RESPONSE : "+response.code() +" "+response.message());

                        MainActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                if(!json.toString().isEmpty()){
                                    MainJsonProcessor processor = new MainJsonProcessor(json.toString());
                                    countryAndLocalCurrency = processor.getCountryAndLocalCurrency();
                                    rates = processor.getRates();
                                    rwMainPageAdapter.setCountryAndLocalCurrency(countryAndLocalCurrency);
                                    rwMainPageAdapter.setRates(rates);
                                    rwMainPageAdapter.notifyDataSetChanged();
                                    lastDate = showUpdateTime();
                                    showUpdate.setText(lastDate);
                                }
                            }
                        });
                     }  else Log.d(TAG, "FAILED RESPONSE : "+response.code());

            }
        });


    }



    //general toast function to use when needed
    private void showToast(String msg) {    Toast.makeText(this,msg,Toast.LENGTH_SHORT).show();
    }

    //function used for getting the updated time
    private String showUpdateTime(){
        Log.d(TAG, "showUpdateTime: ");

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat format = new SimpleDateFormat(" MMM d, yyyy  HH:mm:ss" , Locale.US);
        String time = "Last Updated : "+format.format(calendar.getTime());
        return time;

    }


    //method that handles popupwindow
    private void startPopUpWindow(View v){
        final ViewGroup parent = null;
        View view = LayoutInflater.from(this).inflate(R.layout.chartspage,parent);

       final  PopupWindow popup = new PopupWindow(view, ConstraintLayout.LayoutParams.MATCH_PARENT,
                ConstraintLayout.LayoutParams.MATCH_PARENT,true);
       popup.showAtLocation(v, Gravity.TOP,0,0);

       //initialize tablayout & viewpager2
        TabLayout tabLayout =view.findViewById(R.id.tabLayout);
        ViewPager2 viewPager=view.findViewById(R.id.viewPager);
        viewPager.setAdapter(new ChartPageAdaptor(MainActivity.this));

        //customize tablayout
        tabLayout.setSelectedTabIndicatorColor(getResources().getColor(R.color.OrangeDeep));
        tabLayout.setTabTextColors(getResources().getColor(R.color.whiteish),getResources().getColor(R.color.OrangeDeep));
        TabLayoutMediator mediator = new TabLayoutMediator(tabLayout, viewPager, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                       switch(position) {
                           case 0: tab.setText("RON");
                                   break;

                           case 1: tab.setText("EUR");
                                   break;

                           case 2: tab.setText("USD");
                                   break;
                       }
            }
        });
        mediator.attach();


       // initialize button for closing the popupwindow and check if there is internet connection
        Button closeCharts = view.findViewById(R.id.closeCharts);
        closeCharts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popup.dismiss();
                if(!checkConnection()) showToast("No internet connection ! Can not refresh list");

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if(item.getItemId() == R.id.threeseconds){
            refreshingRate = 3000;
            showToast("Refresh rate changed to 3 sec");
            return true;
        }
        else if(item.getItemId() ==R.id.fiveseconds ){
            refreshingRate = 5000;
            showToast("Refresh rate changed to 5 sec");
            return true;
        }
        else if(item.getItemId() == R.id.fifteenseconds){
            refreshingRate = 15000;
            showToast("Refresh rate changed to 15 sec");
            return true;
        }
        else if(item.getItemId() == R.id.secondItem){

            if(!countryAndLocalCurrency.isEmpty()){
                customDialog.setList(countryAndLocalCurrency);
                customDialog.show(getSupportFragmentManager(),"List");
                return true;
            }else showToast("List is empty ! Check your internet connection");
        }


        return super.onOptionsItemSelected(item);
    }

    //function used for checking if the internet connection is available
    private boolean checkConnection(){
        Log.d(TAG, "checkConnection: ");
        ConnectivityManager connMan = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifi = connMan.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo mobile = connMan.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

        if(!wifi.isConnected() && !mobile.isConnected()) return false;

        return true;
    }

    //repetitive handler for the downdloading currency data
    private void startHandler(){
        refreshHandling.postDelayed(runnable = new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "run: Handler");
                refreshHandling.postDelayed(runnable,refreshingRate);
                if(checkConnection()) downloadContent();
            }
        }, refreshingRate);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");

        /*  make first call so when the UI is loaded the list
         *   is populated in case of any delay in the connection
         *   check if there is a wifi/mobile internet         */

        if(checkConnection()) downloadContent();
        else showToast("No internet connection");
    }

    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");

        //starting the downloading handler when the take/retakes foreground
          startHandler();


    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause: ");
        
        // stop handler when activity loses foreground or pre-enters background
        refreshHandling.removeCallbacksAndMessages(null);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop: ");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: ");
    }



    @Override
    public void onSaveInstanceState( Bundle outState) {
        Log.d(TAG, "onSaveInstanceState: ");

                outState.putString("baseCoin",baseCoin);
                outState.putInt("refreshRate",refreshingRate);
                outState.putStringArrayList("currencyData",countryAndLocalCurrency);

                double [] array = new double[rates.size()];
                for(int i = 0; i<rates.size(); i++){
                    array[i] = rates.get(i);
                }
                outState.putDoubleArray("rates", array);
                outState.putString("lastDate",lastDate);

        super.onSaveInstanceState(outState);
    }


    @Override
    public void onBackPressed() {
        return;
    }
}