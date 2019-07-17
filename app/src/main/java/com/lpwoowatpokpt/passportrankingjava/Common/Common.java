package com.lpwoowatpokpt.passportrankingjava.Common;

import android.content.Context;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.FirebaseDatabase;
import com.lpwoowatpokpt.passportrankingjava.Model.CountryModel;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class Common {

    public static List<CountryModel> countryModel = new ArrayList<>();

    public static final String WIKIPEDIA = "https://en.m.wikipedia.org/wiki/";

    public static String COUNTRY;

    public static final int RequestCameraPermissionId = 1001;

    //firebase
    public static final String Country_Model ="CountryModel";
    public static final String Countries = "Countries";
    public static final String Top = "TopCountries";
    public static final String Name = "Name";
    public static final String Flag = "Image";

    //tiny db
    public static final String STATUS = "status";
    public static final String IS_EXPAND = "is_expand";
    public static final String COUNTRY_LIST = "country_list";
    public static final String COUNTRY_NAME = "country";
    public static final String COVER = "cover";
    public static final String LATITUDE = "latitude";
    public static final String LONGITUDE = "longitude";
    public static final String IS_DARK_MODE = "is_dark_mode";
    public static final String THEME_ID = "theme_id";

    //big fat
    public static List<String>bigCountries(){
        List<String>big = new ArrayList<>();
        big.add("Russian Federation");
        big.add("China");
        big.add("United States");
        big.add("Brazil");
        big.add("India");
        big.add("Australia");
        big.add("Canada");
        big.add("Argentina");
        big.add("Chile");
        big.add("Peru");
        big.add("Kazakhstan");
        return big;
    }


    private static FirebaseDatabase mDatabase;
    public static FirebaseDatabase getDatabase() {
        if (mDatabase == null)
            mDatabase = FirebaseDatabase.getInstance();
        return mDatabase;
    }

    public static boolean isConnectedToInternet(Context context)
    {
        ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null)
        {
            NetworkInfo[]info = connectivityManager.getAllNetworkInfo();

            for (NetworkInfo networkInfo : info) {
                if (networkInfo.getState() == NetworkInfo.State.CONNECTED)
                    return true;
            }
        }
        return false;
    }

    public static void ShowToast(Context context, String message){
        Toast toast = Toast.makeText(context,
                message, Toast.LENGTH_LONG);
        toast.show();
    }



}
