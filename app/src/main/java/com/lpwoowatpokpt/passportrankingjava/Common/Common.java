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

    //tiny db
    public static final String IS_EXPAND = "is_expand";
    public static final String COUNTRY_LIST = "country_list";
    public static final String COUNTRY_NAME = "country";
    public static final String COVER = "cover";
    public static final String LATITUDE = "latitude";
    public static final String LONGITUDE = "longitude";

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
