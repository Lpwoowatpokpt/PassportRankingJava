package com.lpwoowatpokpt.passportrankingjava.UI;

import android.Manifest;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.swiperefreshlayout.widget.CircularProgressDrawable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.flaviofaria.kenburnsview.KenBurnsView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.lpwoowatpokpt.passportrankingjava.Common.Common;
import com.lpwoowatpokpt.passportrankingjava.Common.TinyDB;
import com.lpwoowatpokpt.passportrankingjava.Model.CountryModel;
import com.lpwoowatpokpt.passportrankingjava.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import in.galaxyofandroid.spinerdialog.SpinnerDialog;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class MainActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks{

    private WifiManager wifiManager;

    ArrayList<String> countryNames = new ArrayList<>();
    TinyDB tinyDB;

    Button btnAdd, btnSubmit;
    SpinnerDialog spinnerDialog;
    ImageView passportCover;
    KenBurnsView background;
    ConstraintLayout root;
    FloatingActionButton fabLocation;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/TravelingTypewriter.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build());
        setContentView(R.layout.activity_main);

        tinyDB = new TinyDB(this);

        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        background = findViewById(R.id.background);
        btnAdd = findViewById(R.id.addBtn);
        btnSubmit = findViewById(R.id.submitBtn);
        btnSubmit.setVisibility(View.INVISIBLE);
        btnSubmit.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, Home.class)));
        root = findViewById(R.id.root);
        passportCover = findViewById(R.id.passportCover);

        fabLocation = findViewById(R.id.fab_find);
        fabLocation.setOnClickListener(view -> {
            String[] perms = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
           if (EasyPermissions.hasPermissions(this, perms))
               setCountryBasedOnUserLocation();
           else
               askForLocationPermission();
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter intentFilter = new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION);
        registerReceiver(wifiStateReceiver, intentFilter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(wifiStateReceiver);
    }

    private void showSnackbar() {
       Snackbar snackbar = Snackbar
                .make(root, "No internet connection!", Snackbar.LENGTH_INDEFINITE)
                .setAction("CONNECT", view -> {
                    if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.Q)
                    wifiManager.setWifiEnabled(true);
                    else
                        startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                });

        snackbar.setActionTextColor(Color.RED);
        snackbar.setTextColor(Color.WHITE);
        snackbar.show();
    }

    @AfterPermissionGranted(123)
    protected void askForLocationPermission() {
        String[] perms = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};

        if (EasyPermissions.hasPermissions(this, perms)){
            setCountryBasedOnUserLocation();

            Log.e("slut", "has permission");

            final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            assert manager != null;
            if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                buildAlertMessageNoGps();
            }

        }else {
            EasyPermissions.requestPermissions(this, getString(R.string.location_rationale),
                    123, perms);
        }
    }

    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", (dialog, id) -> {
                    startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    fabLocation.setVisibility(View.VISIBLE);
                })
                .setNegativeButton("No", (dialog, id) -> dialog.cancel());
        final AlertDialog alert = builder.create();
        alert.show();
    }

    private void setCountryBasedOnUserLocation() {
        String country_name;
        LocationManager lm = (LocationManager)getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        Geocoder geocoder = new Geocoder(getApplicationContext());
        assert lm != null;
        for(String provider: lm.getAllProviders()) {
            @SuppressWarnings("ResourceType") Location location = lm.getLastKnownLocation(provider);
            if(location!=null) {
                try {
                    List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                    if(addresses != null && addresses.size() > 0) {
                        country_name = addresses.get(0).getCountryName();
                        selectCountry(country_name);
                        break;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void selectCountry(final String country_name) {
       DatabaseReference country = Common.getDatabase().getReference(Common.Country_Model);
       Query query = country.orderByChild(Common.Name).equalTo(country_name);
       query.addListenerForSingleValueEvent(new ValueEventListener() {
           @Override
           public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
               for (DataSnapshot postSnap:dataSnapshot.getChildren()){
                   CountryModel model = postSnap.getValue(CountryModel.class);

                   CircularProgressDrawable circularProgressDrawable = new CircularProgressDrawable(getBaseContext());
                   circularProgressDrawable.setStrokeWidth(5f);
                   circularProgressDrawable.setCenterRadius(90f);
                   circularProgressDrawable.start();

                   assert model != null;
                   Glide.with(getApplicationContext())
                           .load(model.getCover())
                           .apply(RequestOptions.placeholderOf(circularProgressDrawable))
                           .into(passportCover);

                   btnAdd.setText(country_name);

                   tinyDB.putString(Common.COUNTRY_NAME, country_name);
                   tinyDB.putString(Common.COVER, model.getCover());
                   tinyDB.putDouble(Common.LATITUDE, model.getLatitude());
                   tinyDB.putDouble(Common.LONGITUDE, model.getLongitude());

                   btnSubmit.setVisibility(View.VISIBLE);
               }
           }

           @Override
           public void onCancelled(@NonNull DatabaseError databaseError) {
               Common.ShowToast(getApplicationContext(), "Error: " + databaseError.getMessage());
           }
       });
    }

    private void getDataFromFirebase() {
        DatabaseReference country_model = Common.getDatabase().getReference(Common.Country_Model);
        country_model.keepSynced(true);
        country_model.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnap: dataSnapshot.getChildren()){
                    CountryModel model = postSnap.getValue(CountryModel.class);
                    Common.countryModel.add(model);

                    String _countryNames = postSnap.child(Common.Name).getValue(String.class);

                    countryNames.add(_countryNames);

                    tinyDB.putListString(Common.COUNTRY_LIST, countryNames);

                    tinyDB.putBoolean(Common.IS_EXPAND, true);

                    spinnerDialog = new SpinnerDialog(MainActivity.this, countryNames, getString(R.string.select_your_country));
                    spinnerDialog.bindOnSpinerListener((s, pos) -> {

                        CircularProgressDrawable circularProgressDrawable = new CircularProgressDrawable(getBaseContext());
                        circularProgressDrawable.setStrokeWidth(5f);
                        circularProgressDrawable.setCenterRadius(90f);
                        circularProgressDrawable.start();

                        Glide.with(getApplicationContext())
                                .load(Common.countryModel.get(pos).getCover())
                                .apply(RequestOptions.placeholderOf(circularProgressDrawable))
                                .into(passportCover);

                        String countryName = Common.countryModel.get(pos).getName();

                        btnAdd.setText(countryName);

                       tinyDB.putString(Common.COUNTRY_NAME, countryName);
                       tinyDB.putString(Common.COVER, Common.countryModel.get(pos).getCover());
                       tinyDB.putDouble(Common.LATITUDE, Common.countryModel.get(pos).getLatitude());
                       tinyDB.putDouble(Common.LONGITUDE, Common.countryModel.get(pos).getLongitude());

                       btnSubmit.setVisibility(View.VISIBLE);
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Common.ShowToast(getApplicationContext(), "Error: " + databaseError);
            }
        });
    }

    private void startSplashScreen() {
        startActivity(new Intent(MainActivity.this, Home.class));
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        setCountryBasedOnUserLocation();
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {

    }

    private BroadcastReceiver wifiStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int wifiStateExtra = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE,
                    WifiManager.WIFI_STATE_UNKNOWN);

            switch (wifiStateExtra) {
                case WifiManager.WIFI_STATE_ENABLED:

                    askForLocationPermission();
                    getDataFromFirebase();

                    if (!tinyDB.getString(Common.COUNTRY_NAME).isEmpty())
                        startSplashScreen();

                    btnAdd.setVisibility(View.VISIBLE);
                    btnAdd.setOnClickListener(v ->   spinnerDialog.showSpinerDialog());
                    break;
                case WifiManager.WIFI_STATE_DISABLED:
                   showSnackbar();
                   if (!tinyDB.getString(Common.COUNTRY_NAME).isEmpty()){
                       btnSubmit.setText(getString(R.string.proceed_offline));
                       btnSubmit.setTextColor(Color.RED);
                       btnSubmit.setVisibility(View.VISIBLE);
                       btnAdd.setVisibility(View.GONE);
                       btnSubmit.setOnClickListener(view -> startSplashScreen());
                   }

                    break;
            }
        }
    };
}
