package com.example.currencyapp;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class CustomChartFragment extends Fragment {

    private static final String TAG = "CustomChartFragment";

     private final String coin;
     private ArrayList<Double> lastTenDaysRates;
     private ArrayList<LocalDate> lastTenDays;
     private LineChart chart;
     private String lastUpdate = "";



    public CustomChartFragment(String coin) {
        this.coin = coin;

    }



    public static CustomChartFragment newInstance(String coin) {
        CustomChartFragment fragment = new CustomChartFragment(coin);
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: "+coin);

        lastTenDaysRates = new ArrayList<>();
        lastTenDays = new ArrayList<>();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: "+coin);
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.custom_chart, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "onViewCreated: "+coin);
         chart = view.findViewById(R.id.chart);
        chart.setNoDataText("Loading data");

         // proceeds to initialize chart only if there is no connection
         if(!checkConnection())initializeAndCustomizeChart();



    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: "+coin);

         //priority is that every time it can connect it refreshes the list
        if(!checkConnection()) Toast.makeText(getContext(),
                "No internet connection ! "+(!lastTenDaysRates.isEmpty()?lastUpdate:""),Toast.LENGTH_SHORT).show();
        else download();

    }

    private void download (){
        Log.d(TAG, "download chart data"+coin);

        String baseURL = "https://api.exchangeratesapi.io/history?";
        StringBuilder json = new StringBuilder();
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(8, TimeUnit.SECONDS)
                .readTimeout(8,TimeUnit.SECONDS)
                .build();

        //determining time range for charts for the last 10 working days
        LocalDate atThisMoment = LocalDate.now();
        final String end_at = atThisMoment.toString().trim();
        LocalDate from = extractLast10WorkingDays(atThisMoment);
        final String start_at = from.toString().trim();


        /* URL for RON & USD are EUR based :
        *  https://api.exchangeratesapi.io/history?start_at=2021-03-02&end_at=2021-03-12&symbols=
        *  URL for EUR is based on USD :
        * *https://api.exchangeratesapi.io/history?start_at=2021-03-02&end_at=2021-03-12&symbols=EUR&base=USD
        */

        Request request = new Request.Builder()
                          .url(baseURL+"start_at="+start_at+"&end_at="+end_at+"&symbols="+(coin.equals("EUR")?"EUR&base=USD":coin))
                           .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    e.printStackTrace();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                   if(response.isSuccessful()){
                       json.append(response.body().string());
                       Log.d(TAG, "onResponse is successful "+ json.toString());
                       Collections.sort(lastTenDays);

                       parsingJson(atThisMoment, json.toString());

                       getActivity().runOnUiThread(new Runnable() {
                           @Override
                           public void run() {

                                 initializeAndCustomizeChart();
                                 lastUpdate = showUpdateTime();

                           }
                       });
                   }
            }
        });
    }

    private void parsingJson(LocalDate end_at, String json)  {
        int count = 0;
        JSONObject jsonObject = null;
        int length = 0;

        /*when refreshing rates list because it s declared as a field
        * can overpopulate (>10) */
        lastTenDaysRates.clear();
        do {

            /* parsing the json starting from present day, decreasing with every iteration (var count)
            * adding item to last10dayRates arraylist from present to last  */

                try {

                jsonObject = new JSONObject(json);
                 length = jsonObject.getJSONObject("rates").length();
                 Log.d(TAG, "parsingJson: JSON RATES LENGTH = "+length);
                String now = end_at.minusDays(count).toString().trim();
                if (jsonObject != null) lastTenDaysRates.add(jsonObject.getJSONObject("rates").getJSONObject(now).getDouble(coin));

                } catch (JSONException e){
                    e.printStackTrace();
                }
                count++;
        } while (lastTenDaysRates.size() != length && jsonObject != null && count<20);


        Collections.reverse(lastTenDaysRates);
        for(int i  = 0; i<lastTenDaysRates.size(); i++){
            Log.d(TAG, "parsingJson: " +lastTenDaysRates.get(i));
        }
        Log.d(TAG, "parsingJson: Size"+lastTenDaysRates.size());

    }


    //determine the last 10 days which got available data
    private LocalDate extractLast10WorkingDays(LocalDate endAt){
        Log.d(TAG, "extractLast10WorkingDays: "+coin);
        LocalTime now = LocalTime.now();
        int checkIfTodayIsAvailable = 0;

        /*when refreshing rates list because it s declared as a field
         * can overpopulate (>10) */
        lastTenDays.clear();

        //declared holidays by european central bank
        LocalDate [] holidays = {LocalDate.of(2021,1,1),LocalDate.of(2021,4,2),
                LocalDate.of(2021,4,5),LocalDate.of(2021,5,1),
                LocalDate.of(2021,5,9),LocalDate.of(2021,5,13),
                LocalDate.of(2021,5,24),LocalDate.of(2021,6,3),
                LocalDate.of(2021,10,3),LocalDate.of(2021,11,1),
                LocalDate.of(2021,12,24), LocalDate.of(2021,12,25),
                LocalDate.of(2021,12,26), LocalDate.of(2021,12,31),};

        //check if local time is behind bank update hour
        if(now.compareTo(LocalTime.of(17, 45)) < 0) checkIfTodayIsAvailable = 1;

        int count = 0+checkIfTodayIsAvailable; // if local time is behind update hour start count from the day before
        int amountOfWorkingDays = 0;

        while(amountOfWorkingDays < 10){
            Log.d(TAG, "extractLast10WorkingDays: "+ "IN :"+"count ="+count+" working days = "+amountOfWorkingDays);
            LocalDate ld = endAt.minusDays(count);
            DayOfWeek day = ld.getDayOfWeek();
            System.out.println("DAY : "+day.toString() +" at "+ld);

            //check if day is either a weekend day or a holiday
            if(day.getValue() != DayOfWeek.SATURDAY.getValue() && day.getValue() != DayOfWeek.SUNDAY.getValue()) {

                boolean foundHoliday = false;
                for(int i = 0; i<holidays.length; i++){
                    if(holidays[i].compareTo(ld) == 0) foundHoliday = true;
                }

               if(!foundHoliday) {
                   amountOfWorkingDays++;
               lastTenDays.add(ld);

               }

            }

            count++;

            Log.d(TAG, "extractLast10WorkingDays: "+"OUT :"+"count ="+count+" working days = "+amountOfWorkingDays);
        }

        return endAt.minusDays((count-1));
    }


      //find the last update date to show in case there is no internet connetion
    private String showUpdateTime(){
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat format = new SimpleDateFormat(" MMM d, HH:mm:ss" , Locale.US);

        return "Last Updated : "+format.format(calendar.getTime());

    }



    // function to be called whenever it s needed to check connetion
    private boolean checkConnection(){
        ConnectivityManager connMan = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifi = connMan.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo mobile = connMan.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

        if(!wifi.isConnected() && !mobile.isConnected()) return false;

        return true;
    }

    // gettin rates ready for chart LineDataSet
    private ArrayList<Entry> dataValues (){
        ArrayList<Entry> data = new ArrayList<>();
        for(int i = 0 ; i<lastTenDaysRates.size(); i++){
            data.add(new Entry(i,lastTenDaysRates.get(i).floatValue()));
        }

        return data;
    }

    private void initializeAndCustomizeChart(){

        LineDataSet lineDataSet = new LineDataSet(dataValues(),"Working Days (month-day format)");
        lineDataSet.setColor(getResources().getColor(R.color.OrangeDeep));
        lineDataSet.setLineWidth(2);
        lineDataSet.setValueTextColor(getResources().getColor(R.color.whiteish));
        lineDataSet.setValueTextSize(12);

        ArrayList<ILineDataSet> dataSet = new ArrayList<>();
        dataSet.add(lineDataSet);

        chart.setBackgroundColor(getResources().getColor(R.color.blackish));
        chart.setDrawBorders(true);
        chart.setBorderColor(getResources().getColor(R.color.turquoise));
        chart.setTouchEnabled(false);
        chart.getDescription().setEnabled(false);


        Legend legend = chart.getLegend();
        legend.setTextColor(getResources().getColor(R.color.whiteish));


        LineData lineData = new LineData(dataSet);
        chart.setData(lineData);
        chart.invalidate();

        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextSize(9f);
        xAxis.setTextColor(getResources().getColor(R.color.whiteish));
        xAxis.setDrawAxisLine(true);
        xAxis.setDrawLabels(true);
        xAxis.setLabelCount(9);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(formatDates()));


    }
          // format dates obtained from parsing the json in a month-day format
    private String [] formatDates(){
        String [] onlyDayAndMonth = new String[lastTenDays.size()];

        if(!lastTenDays.isEmpty()) {
            for (int i = 0; i<lastTenDays.size(); i++){

                String date = lastTenDays.get(i).toString();
                onlyDayAndMonth[i] = date.substring(5);
                System.out.println(onlyDayAndMonth[i]);
            }
        }

        return onlyDayAndMonth;
    }


    @Override
    public void onAttach(@NonNull Context context) {
        Log.d(TAG, "onAttach: "+coin);
        super.onAttach(context);
    }

    @Override
    public void onPause() {
        Log.d(TAG, "onPause: "+coin);
        super.onPause();
    }

    @Override
    public void onStop() {
        Log.d(TAG, "onStop: "+coin);
        super.onStop();
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy: "+coin);
        super.onDestroy();
    }

    @Override
    public void onDestroyView() {
        Log.d(TAG, "onDestroyView: "+coin);
        super.onDestroyView();
    }






}