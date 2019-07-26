package com.lpwoowatpokpt.passportrankingjava.UI;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.lpwoowatpokpt.passportrankingjava.Adapter.PassportAdapter;
import com.lpwoowatpokpt.passportrankingjava.Common.Common;
import com.lpwoowatpokpt.passportrankingjava.Common.TinyDB;
import com.lpwoowatpokpt.passportrankingjava.Common.Utils;
import com.lpwoowatpokpt.passportrankingjava.Model.Country;
import com.lpwoowatpokpt.passportrankingjava.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

import es.dmoral.toasty.Toasty;
import io.github.inflationx.calligraphy3.CalligraphyConfig;
import io.github.inflationx.calligraphy3.CalligraphyInterceptor;
import io.github.inflationx.viewpump.ViewPump;
import io.github.inflationx.viewpump.ViewPumpContextWrapper;

public class CountryDetail extends AppCompatActivity {

    TinyDB tinyDB;

    MapView mMapView;
    ImageView no_internet;

    TextView txtTotalScore, txtVisaFree, txtVisaOnArraival, txtEta, txtVisaRequiered;

    RecyclerView recyclerView;

    ProgressBar loadingInfoBar;

    FloatingActionButton fab;

    PassportAdapter passportAdapter;

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
                                .setDefaultFontPath("fonts/Roboto-Medium.ttf")
                                .setFontAttrId(R.attr.fontPath)
                                .build())).build());

        tinyDB = new TinyDB(this);
        Utils.onActivityCreateSetTheme(this, tinyDB.getInt(Common.THEME_ID));
        setContentView(R.layout.activity_country_detail);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar()!=null){
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        AppBarLayout appBarLayout = findViewById(R.id.appbar);
        CoordinatorLayout.LayoutParams params =
                (CoordinatorLayout.LayoutParams) appBarLayout.getLayoutParams();
        AppBarLayout.Behavior behavior = new AppBarLayout.Behavior();
        behavior.setDragCallback(new AppBarLayout.Behavior.DragCallback() {
            @Override
            public boolean canDrag(AppBarLayout appBarLayout) {
                return false;
            }
        });
        params.setBehavior(behavior);

        CollapsingToolbarLayout ctl = findViewById(R.id.collapsing_toolbar);
        ctl.setTitle(Common.COUNTRY);

        mMapView = findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);
        mMapView.onResume();

        no_internet = findViewById(R.id.no_internet);



        try {
            MapsInitializer.initialize(getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }



        if (Common.isConnectedToInternet(getApplicationContext())){
            new LoadMap().execute();
        }else {
            mMapView.setVisibility(View.GONE);
            no_internet.setVisibility(View.VISIBLE);
        }

        txtTotalScore = findViewById(R.id.total);
        txtVisaOnArraival = findViewById(R.id.visa_on_arrival);
        txtEta = findViewById(R.id.eTa);
        txtVisaFree = findViewById(R.id.visa_free);
        txtVisaRequiered = findViewById(R.id.visaRequiered);


        loadingInfoBar = findViewById(R.id.loading_recycler);

        fab = findViewById(R.id.fab);
        fab.setOnClickListener(v -> {
            Intent browserIntent = new Intent(CountryDetail.this, WebViewActivity.class);
            startActivity(browserIntent);
        });


        recyclerView = findViewById(R.id.recycler);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        passportAdapter = new PassportAdapter(getApplicationContext(), getCountries(), tinyDB);
    }

    private ArrayList<Country> getCountries(){
        final ArrayList<Country>countryList = new ArrayList<>();

        Query query = Common.getDatabase().getReference(Common.Countries)
                .orderByKey().equalTo(Common.COUNTRY);

        query.addValueEventListener(new ValueEventListener() {
            @SuppressLint("NewApi")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnap: dataSnapshot.getChildren()){

                    Map<String,Long> data = (Map)postSnap.getValue();
                    assert data != null;
                    Map<String, Long> treeMap = new TreeMap<>(data);

                    ArrayList<Long>status = new ArrayList<>();

                    for (Map.Entry<String,Long> entry : treeMap.entrySet()){
                        countryList.addAll(Collections.singleton(new Country(entry.getKey(), entry.getValue())));

                        status.add(entry.getValue());

                        int visa_free = Collections.frequency(status, (long) 3);
                        int visa_eta = Collections.frequency(status, (long) 2);
                        int visa_onArraival = Collections.frequency(status, (long) 1);
                        int visa_requiered = Collections.frequency(status, (long) 0);
                        int total = visa_free+visa_onArraival+visa_eta;

                        txtTotalScore.setText(String.valueOf(total));
                        txtVisaFree.setText(String.valueOf(visa_free));
                        txtEta.setText(String.valueOf(visa_eta));
                        txtVisaOnArraival.setText(String.valueOf(visa_onArraival));
                        txtVisaRequiered.setText(String.valueOf(visa_requiered));

                        recyclerView.setAdapter(passportAdapter);
                        passportAdapter.notifyDataSetChanged();
                        loadingInfoBar.setVisibility(View.GONE);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        return countryList;

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


    @SuppressLint("StaticFieldLeak")
    public class LoadMap extends AsyncTask<Void,Void,Void>{
        @Override
        protected Void doInBackground(Void... voids) {
            Query mapQuery = Common.getDatabase().getReference(Common.Country_Model)
                    .orderByChild(Common.Name).equalTo(Common.COUNTRY);

            mapQuery.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot postSnap: dataSnapshot.getChildren()){

                        final Double latitude = (Double) postSnap.child("Latitude").getValue();
                        final Double longitude = (Double) postSnap.child("Longitude").getValue();

                        String flagPath = (String)postSnap.child(Common.Flag).getValue();

                        mMapView.getMapAsync(map -> {
                            if(latitude!=null&&longitude!=null){
                                LatLng current = new LatLng(latitude,longitude);
                                float scale = 6f;
                                if (Common.bigCountries().contains(Common.COUNTRY))
                                    scale = 3f;
                                map.moveCamera(CameraUpdateFactory.newLatLngZoom(current, scale));


                                ImageView flag = findViewById(R.id.flag);
                                Glide.with(getBaseContext()).load(flagPath).into(flag);
                                float finalScale = scale;
                                flag.setOnClickListener(view -> map.moveCamera(CameraUpdateFactory.newLatLngZoom(current, finalScale)));

                            }
                        });

                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toasty.error(getBaseContext(), getString(R.string.error_toast) + databaseError.getMessage(),5).show();
                }
            });
            return null;
        }
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
