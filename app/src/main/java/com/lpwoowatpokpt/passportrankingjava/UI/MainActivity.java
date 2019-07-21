package com.lpwoowatpokpt.passportrankingjava.UI;

import android.Manifest;
import android.animation.Animator;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.swiperefreshlayout.widget.CircularProgressDrawable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.droidnet.DroidListener;
import com.droidnet.DroidNet;
import com.dx.dxloadingbutton.lib.LoadingButton;
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

import es.dmoral.toasty.Toasty;
import in.galaxyofandroid.spinerdialog.SpinnerDialog;
import io.github.inflationx.calligraphy3.CalligraphyConfig;
import io.github.inflationx.calligraphy3.CalligraphyInterceptor;
import io.github.inflationx.viewpump.ViewPump;
import io.github.inflationx.viewpump.ViewPumpContextWrapper;
import kotlin.Unit;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks, DroidListener {


    ArrayList<String> countryNames = new ArrayList<>();
    TinyDB tinyDB;

    private DroidNet mDroidNet;
    private WifiManager wifiManager;
    View animateView;

    LoadingButton btnAdd, btnSubmit;
    SpinnerDialog spinnerDialog;
    ImageView passportCover;
    ImageView background;
    TextView countryName;
    ConstraintLayout root;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ViewPump.init(ViewPump.builder()
                .addInterceptor(new CalligraphyInterceptor(
                        new CalligraphyConfig.Builder()
                                .setDefaultFontPath("fonts/TravelingTypewriter.ttf")
                                .setFontAttrId(R.attr.fontPath)
                                .build())).build());

            setContentView(R.layout.activity_main);

        tinyDB = new TinyDB(this);

        getDataFromFirebase();

        mDroidNet = DroidNet.getInstance();
        mDroidNet.addInternetConnectivityListener(this);

        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        countryName = findViewById(R.id.countryName);

        background = findViewById(R.id.background);
        animateView = findViewById(R.id.animate_view);

        btnAdd = findViewById(R.id.addBtn);
        btnSubmit = findViewById(R.id.submitBtn);
        btnSubmit.setEnabled(false);
        btnSubmit.setOnClickListener(v -> {
            btnSubmit.startLoading();
            btnSubmit.postDelayed(() -> {
                btnSubmit.loadingSuccessful();
                btnSubmit.setAnimationEndAction(animationType -> {
                    toNextPage();
                    return Unit.INSTANCE;
                });
            },500);
        });
        root = findViewById(R.id.root);
        passportCover = findViewById(R.id.passportCover);

        if (!tinyDB.getString(Common.COUNTRY_NAME).isEmpty()) {
            goToHomeActivity();
            btnAdd.setVisibility(View.GONE);
            btnSubmit.setVisibility(View.GONE);
        }
    }

    private void toNextPage() {
        int cx = (btnSubmit.getLeft() + btnSubmit.getRight()) / 2;
        int cy = (btnSubmit.getTop() + btnSubmit.getBottom()) / 2;

        Animator animator = ViewAnimationUtils.createCircularReveal(animateView,cx,cy,0,getResources().getDisplayMetrics().heightPixels * 1.2f);
        animator.setDuration(2000);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animateView.setVisibility(View.VISIBLE);
        animator.start();
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                startActivity(new Intent(MainActivity.this, Home.class));
                btnSubmit.reset();
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDroidNet.removeInternetConnectivityChangeListener(this);
    }


    @AfterPermissionGranted(123)
    protected void askForLocationPermission() {
        String[] perms = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};

        if (EasyPermissions.hasPermissions(this, perms)){
            setCountryBasedOnUserLocation();

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

                    countryName.setText(country_name);

                    tinyDB.putString(Common.COUNTRY_NAME, country_name);
                    tinyDB.putString(Common.COVER, model.getCover());
                    tinyDB.putDouble(Common.LATITUDE, model.getLatitude());
                    tinyDB.putDouble(Common.LONGITUDE, model.getLongitude());

                    btnSubmit.setEnabled(true);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toasty.error(getBaseContext(), getString(R.string.error_toast) + databaseError.getMessage(),5).show();
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

                    spinnerDialog=new SpinnerDialog(MainActivity.this,countryNames,getString(R.string.select_your_country), R.style.DialogAnimations_SmileWindow,"Close");

                    spinnerDialog.setCancellable(false);
                    spinnerDialog.setShowKeyboard(false);

                    spinnerDialog.setTitleColor(getResources().getColor(R.color.colorPrimaryText));
                    spinnerDialog.setCloseColor(getResources().getColor(R.color.visa_requiered));

                    spinnerDialog.bindOnSpinerListener((s, pos) -> {

                        CircularProgressDrawable circularProgressDrawable = new CircularProgressDrawable(getBaseContext());
                        circularProgressDrawable.setStrokeWidth(5f);
                        circularProgressDrawable.setCenterRadius(90f);
                        circularProgressDrawable.start();

                        Glide.with(getApplicationContext())
                                .load(Common.countryModel.get(pos).getCover())
                                .apply(RequestOptions.placeholderOf(circularProgressDrawable))
                                .into(passportCover);

                        String _countryName = Common.countryModel.get(pos).getName();

                        countryName.setText(_countryName);

                        tinyDB.putString(Common.COUNTRY_NAME, _countryName);
                        tinyDB.putString(Common.COVER, Common.countryModel.get(pos).getCover());
                        tinyDB.putDouble(Common.LATITUDE, Common.countryModel.get(pos).getLatitude());
                        tinyDB.putDouble(Common.LONGITUDE, Common.countryModel.get(pos).getLongitude());

                        btnSubmit.setEnabled(true);
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toasty.error(getBaseContext(), getString(R.string.error_toast) + databaseError.getMessage(),5).show();
            }
        });
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

    private void goToHomeActivity() {
        btnAdd.setEnabled(false);
        btnSubmit.setEnabled(false);
        background.setImageResource(R.drawable.splash);

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

        LayoutInflater inflater = this.getLayoutInflater();
        View update_dialog = inflater.inflate(R.layout.splash, null);

        final AlertDialog alert = alertDialog.create();
        alert.setView(update_dialog);
        alert.setCancelable(true);
        alert.show();

        Handler handler = new Handler();
        Runnable runnable = () -> {
            if (alert.isShowing()){
                alert.dismiss();
                startActivity(new Intent(MainActivity.this, Home.class));
                finish();
            }else {
                startActivity(new Intent(MainActivity.this, Home.class));
                finish();
            }
        };
        handler.postDelayed(runnable, 2000);
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


    @Override
    public void onInternetConnectivityChanged(boolean isConnected) {
        if (isConnected){
            if (tinyDB.getString(Common.COUNTRY_NAME).isEmpty()){
                askForLocationPermission();
                btnAdd.setEnabled(true);
                btnAdd.setOnClickListener(v -> {
                    if (countryNames.size()>0){
                        spinnerDialog.showSpinerDialog();
                    }
                    else{
                        Toasty.error(getApplicationContext(), getString(R.string.poor_internet), Toast.LENGTH_SHORT, true).show();
                    }
                });
            }
        }else {
            btnAdd.setEnabled(false);
            if (tinyDB.getString(Common.COUNTRY_NAME).isEmpty())
            showSnackbar();
            else
                Toasty.warning(getApplicationContext(), getString(R.string.no_internet), Toast.LENGTH_SHORT, true).show();
        }
    }
}