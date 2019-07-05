package com.lpwoowatpokpt.passportrankingjava.UI;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.lpwoowatpokpt.passportrankingjava.Common.Common;
import com.lpwoowatpokpt.passportrankingjava.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class CountryDetail extends AppCompatActivity {

    MapView mMapView;

    TextView txtTotalScore, txtVisaFree, txtVisaOnArraival, txtEta, txtVisaRequiered;

    FloatingActionButton fab;

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
        setContentView(R.layout.activity_country_detail);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar()!=null){
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        CollapsingToolbarLayout ctl = findViewById(R.id.collapsing_toolbar);
        ctl.setTitle(Common.COUNTRY);

        mMapView = findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);
        mMapView.onResume();

        try {
            MapsInitializer.initialize(getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        Query _query = Common.getDatabase().getReference(Common.Country_Model)
                .orderByChild(Common.Name).equalTo(Common.COUNTRY);


        _query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnap: dataSnapshot.getChildren()){
                    final Double latitude = (Double) postSnap.child("Latitude").getValue();
                    final Double longitude = (Double) postSnap.child("Longitude").getValue();

                    mMapView.getMapAsync(new OnMapReadyCallback() {
                        @Override
                        public void onMapReady(GoogleMap map) {
                            LatLng current = new LatLng(latitude,longitude);
                            map.moveCamera(CameraUpdateFactory.newLatLngZoom(current, 5f));
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });


        txtTotalScore = findViewById(R.id.total);
        txtVisaOnArraival = findViewById(R.id.visa_on_arrival);
        txtEta = findViewById(R.id.eTa);
        txtVisaFree = findViewById(R.id.visa_free);
        txtVisaRequiered = findViewById(R.id.visaRequiered);

        fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(CountryDetail.this, WebViewActivity.class);
                startActivity(browserIntent);
            }
        });

        calculateCountryScore();
    }

    private void calculateCountryScore() {
        Query query = Common.getDatabase().getReference(Common.Countries)
                .orderByKey().equalTo(Common.COUNTRY);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnap: dataSnapshot.getChildren()){
                    Map<String,Long> data = (Map)postSnap.getValue();
                    Map<String, Long> treeMap = new TreeMap<>(data);

                    ArrayList<Long> status = new ArrayList<>();
                    for (Map.Entry<String,Long> entry : treeMap.entrySet()){
                        status.add(entry.getValue());
                        int visa_free = Collections.frequency(status, (long) 3);
                        int visa_eta = Collections.frequency(status, (long) 2);
                        int visa_onArraival = Collections.frequency(status, (long) 1);
                        int visa_requiered = Collections.frequency(status, (long) 0);
                        int total = visa_free+visa_onArraival;
                        txtTotalScore.setText(String.valueOf(total));
                        txtVisaFree.setText(String.valueOf(visa_free));
                        txtEta.setText(String.valueOf(visa_eta));
                        txtVisaOnArraival.setText(String.valueOf(visa_onArraival));
                        txtVisaRequiered.setText(String.valueOf(visa_requiered));
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            Intent intent = new Intent(CountryDetail.this, Home.class);
            startActivity(intent);
            finish();
        }
        return super.onOptionsItemSelected(menuItem);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(CountryDetail.this, Home.class);
        startActivity(intent);
        finish();
    }


    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }
}
