package com.example.currencyapp;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainJsonProcessor {

    private final Map<String,Double> parsedJson;
    private ArrayList<String>countryAndLocalCurrency;
    private ArrayList<Double>rates;
    private String baseCoin = "EUR";


    public MainJsonProcessor(String json){
        parsedJson = new HashMap<>();
        parsingJsonMainList(json);
        arrangeAlphabeticalOrder();
    }

    private void parsingJsonMainList(String json){
        try{
            JSONObject downloaded = new JSONObject(json);
            parsedJson.put("Canadian, Dollar-CAD",downloaded.getJSONObject("rates").getDouble("CAD"));
            parsedJson.put("Hong Kong, Dollar-HKD",downloaded.getJSONObject("rates").getDouble("HKD"));
            parsedJson.put("Iceland, Krone-ISK",downloaded.getJSONObject("rates").getDouble("ISK"));
            parsedJson.put("Philippine, Peso-PHP",downloaded.getJSONObject("rates").getDouble("PHP"));
            parsedJson.put("Danemark, Krone-DKK",downloaded.getJSONObject("rates").getDouble("DKK"));
            parsedJson.put("Hungary, Florint-HUF",downloaded.getJSONObject("rates").getDouble("HUF"));
            parsedJson.put("Czech, Koruna-CZK",downloaded.getJSONObject("rates").getDouble("CZK"));
            parsedJson.put("Australia, Dollar-AUD",downloaded.getJSONObject("rates").getDouble("AUD"));
            parsedJson.put("Romania, Leu-RON",downloaded.getJSONObject("rates").getDouble("RON"));
            parsedJson.put("Sweden, Krone-SEK",downloaded.getJSONObject("rates").getDouble("SEK"));
            parsedJson.put("Indonesia, Rupiah-IDR",downloaded.getJSONObject("rates").getDouble("IDR"));
            parsedJson.put("Indian, Rupee-INR",downloaded.getJSONObject("rates").getDouble("INR"));
            parsedJson.put("Brazil, Real-BRL",downloaded.getJSONObject("rates").getDouble("BRL"));
            parsedJson.put("Russia, Rouble-RUB",downloaded.getJSONObject("rates").getDouble("RUB"));
            parsedJson.put("Croatia, Kuna-HRK",downloaded.getJSONObject("rates").getDouble("HRK"));
            parsedJson.put("Japan, Yen-JPY",downloaded.getJSONObject("rates").getDouble("JPY"));
            parsedJson.put("Thailand, Baht-THB",downloaded.getJSONObject("rates").getDouble("THB"));
            parsedJson.put("Swiss, Franc-CHF",downloaded.getJSONObject("rates").getDouble("CHF"));
            parsedJson.put("Singapore, Dollar-SGD",downloaded.getJSONObject("rates").getDouble("SGD"));
            parsedJson.put("Poland, Zloty-PLN",downloaded.getJSONObject("rates").getDouble("PLN"));
            parsedJson.put("Bulgaria, Leva-BGN",downloaded.getJSONObject("rates").getDouble("BGN"));
            parsedJson.put("Turkey, Lira-TRY",downloaded.getJSONObject("rates").getDouble("TRY"));
            parsedJson.put("China, Yuan-CNY",downloaded.getJSONObject("rates").getDouble("CNY"));
            parsedJson.put("Norway, Krone-NOK",downloaded.getJSONObject("rates").getDouble("NOK"));
            parsedJson.put("New Zealand, Dollar-NZD",downloaded.getJSONObject("rates").getDouble("NZD"));
            parsedJson.put("South Africa, Rand-ZAR",downloaded.getJSONObject("rates").getDouble("ZAR"));
            parsedJson.put("USA, Dollar-USD",downloaded.getJSONObject("rates").getDouble("USD"));
            parsedJson.put("Mexic, Peso-MXN",downloaded.getJSONObject("rates").getDouble("MXN"));
            parsedJson.put("Israel, Shekel-ILS",downloaded.getJSONObject("rates").getDouble("ILS"));
            parsedJson.put("UK, Pound-GBP",downloaded.getJSONObject("rates").getDouble("GBP"));
            parsedJson.put("South Korea, Won-KRW",downloaded.getJSONObject("rates").getDouble("KRW"));
            parsedJson.put("Malaysia, Ringgit-MYR",downloaded.getJSONObject("rates").getDouble("MYR"));

            String checkBaseCoin = downloaded.getString("base");
            if(!checkBaseCoin.equals("EUR")) baseCoin = checkBaseCoin;

            countryAndLocalCurrency = new ArrayList<>(parsedJson.keySet());
            rates = new ArrayList<>(parsedJson.values());

        }catch (JSONException e){
            e.printStackTrace();}
    }

    private void arrangeAlphabeticalOrder(){
        for (int i = 0; i < countryAndLocalCurrency.size(); i++) {
            for (int j = i + 1; j < countryAndLocalCurrency.size(); j++) {
                if (countryAndLocalCurrency.get(i).compareTo(countryAndLocalCurrency.get(j)) > 0) {

                    String shiftKey = countryAndLocalCurrency.get(i);
                    countryAndLocalCurrency.set(i, countryAndLocalCurrency.get(j));
                    countryAndLocalCurrency.set(j, shiftKey);

                    double shiftValue = rates.get(i);
                    rates.set(i, rates.get(j));
                    rates.set(j, shiftValue);

                }
            }
        }
    }

    public ArrayList<String> getCountryAndLocalCurrency() {
        return countryAndLocalCurrency;
    }

    public ArrayList<Double> getRates() {
        return rates;
    }
}
