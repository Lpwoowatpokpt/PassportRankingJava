package com.lpwoowatpokpt.passportrankingjava.UI;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.swiperefreshlayout.widget.CircularProgressDrawable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.lpwoowatpokpt.passportrankingjava.Common.Common;
import com.lpwoowatpokpt.passportrankingjava.Common.TinyDB;
import com.lpwoowatpokpt.passportrankingjava.Model.CountryModel;
import com.lpwoowatpokpt.passportrankingjava.R;

import java.util.ArrayList;

import in.galaxyofandroid.spinerdialog.OnSpinerItemClick;
import in.galaxyofandroid.spinerdialog.SpinnerDialog;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class MainActivity extends AppCompatActivity {

    ArrayList<String> countryNames = new ArrayList<>();
    TinyDB tinyDB;

    Button btnAdd, btnSubmit;
    SpinnerDialog spinnerDialog;
    ImageView passportCover, background;
    ConstraintLayout root;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/expressway.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build());
        setContentView(R.layout.activity_main);

        tinyDB = new TinyDB(this);

        if (ActivityCompat.checkSelfPermission(getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, Common.RequestCameraPermissionId);
        }

        background = findViewById(R.id.background);
        btnAdd = findViewById(R.id.addBtn);
        btnSubmit = findViewById(R.id.submitBtn);
        btnSubmit.setVisibility(View.INVISIBLE);
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, Home.class));
            }
        });
        root = findViewById(R.id.root);
        passportCover = findViewById(R.id.passportCover);

        if (Common.isConnectedToInternet(getBaseContext())){

            getDataFromFirebase();

            if (!tinyDB.getString(Common.COUNTRY_NAME).isEmpty()){
                startSplashScreen();
                btnAdd.setVisibility(View.GONE);
                background.setImageDrawable(getResources().getDrawable(R.drawable.splash));
            }else {
                btnAdd.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!countryNames.isEmpty())
                            spinnerDialog.showSpinerDialog();
                    }
                });
            }
        }else{
            Snackbar snackbar = Snackbar
                    .make(root, getString(R.string.no_internet), Snackbar.LENGTH_LONG);
            snackbar.show();
        }
    }


    private void getDataFromFirebase() {
        DatabaseReference country_model = Common.getDatabase().getReference(Common.Country_Model);
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
                    spinnerDialog.bindOnSpinerListener(new OnSpinerItemClick() {
                        @Override
                        public void onClick(String s, final int pos) {

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
                        }
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
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

        alertDialog.setCancelable(false);

        LayoutInflater inflater = this.getLayoutInflater();
        @SuppressLint("InflateParams") View update_dialog = inflater.inflate(R.layout.splash, null);

        TextView textView = update_dialog.findViewById(R.id.hText);
        textView.setText(getString(R.string.app_name));

        final AlertDialog alert = alertDialog.create();
        alert.setView(update_dialog);
        alert.show();

        Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (alert.isShowing()){
                    alert.dismiss();
                    startActivity(new Intent(MainActivity.this, Home.class));
                    finish();
                }else {
                    startActivity(new Intent(MainActivity.this, Home.class));
                    finish();
                }
            }
        };
        handler.postDelayed(runnable, 2500);
    }
}
