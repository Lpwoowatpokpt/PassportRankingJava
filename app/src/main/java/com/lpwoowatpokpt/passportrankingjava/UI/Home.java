package com.lpwoowatpokpt.passportrankingjava.UI;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.droidnet.DroidListener;
import com.droidnet.DroidNet;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.lpwoowatpokpt.passportrankingjava.Common.Common;
import com.lpwoowatpokpt.passportrankingjava.Common.TinyDB;
import com.lpwoowatpokpt.passportrankingjava.Common.Utils;
import com.lpwoowatpokpt.passportrankingjava.Model.Country;
import com.lpwoowatpokpt.passportrankingjava.Model.Ranking;
import com.lpwoowatpokpt.passportrankingjava.R;
import com.lpwoowatpokpt.passportrankingjava.UI.Fragments.CompareFragment;
import com.lpwoowatpokpt.passportrankingjava.UI.Fragments.MapFragment;
import com.lpwoowatpokpt.passportrankingjava.UI.Fragments.RankingFragment;
import com.lpwoowatpokpt.passportrankingjava.UI.Fragments.TopFragment;
import com.mahfa.dnswitch.DayNightSwitch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

import es.dmoral.toasty.Toasty;
import io.github.inflationx.calligraphy3.CalligraphyConfig;
import io.github.inflationx.calligraphy3.CalligraphyInterceptor;
import io.github.inflationx.viewpump.ViewPump;
import io.github.inflationx.viewpump.ViewPumpContextWrapper;

public class Home extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, DroidListener {

    Toolbar toolbar;
    TinyDB tinyDB;

    private DroidNet mDroidNet;
    private WifiManager wifiManager;

    FrameLayout root;

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
                                .setDefaultFontPath("fonts/NanumGothic-Bold.ttf")
                                .setFontAttrId(R.attr.fontPath)
                                .build())).build());

        tinyDB = new TinyDB(this);
        Utils.onActivityCreateSetTheme(this, tinyDB.getInt(Common.THEME_ID));
        setContentView(R.layout.activity_home);



        if (ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, Common.RequestCameraPermissionId);
        }

        mDroidNet = DroidNet.getInstance();
        mDroidNet.addInternetConnectivityListener(this);

        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.menu_ranking));
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setItemIconTintList(null);
        navigationView.getMenu().getItem(0).setChecked(true);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        setDefaultFragment();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDroidNet.removeInternetConnectivityChangeListener(this);
    }

    private void showSnackbar() {
        root = findViewById(R.id.container);
        Snackbar snackbar = Snackbar
                .make(root, "No internet connection!", Snackbar.LENGTH_INDEFINITE)
                .setAction("CONNECT", view -> {
                    if(android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.Q)
                        wifiManager.setWifiEnabled(true);
                    else
                        startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                });
        snackbar.setActionTextColor(Color.RED);
        snackbar.setTextColor(Color.WHITE);
        snackbar.show();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Intent intent = new Intent(Home.this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }else if(id == R.id.action_info){
            Intent intent = new Intent(Home.this, InfoActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        Fragment selectedFragment = null;
        int id = item.getItemId();

        switch (id){
            case R.id.nav_ranking:
                selectedFragment = RankingFragment.newInstance(this, tinyDB);
                toolbar.setTitle(getString(R.string.menu_ranking));
                break;
            case R.id.nav_compare:
                selectedFragment = CompareFragment.newInstance(this, tinyDB);
                toolbar.setTitle(getString(R.string.menu_compare));
                break;
            case R.id.nav_top:
                selectedFragment = TopFragment.newInstance(this);
                toolbar.setTitle(getString(R.string.menu_top));
                break;
            case R.id.nav_map:
                selectedFragment = MapFragment.newInstance(this, tinyDB);
                toolbar.setTitle(tinyDB.getString(Common.COUNTRY_NAME));
                break;

        }

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        assert selectedFragment != null;
        transaction.replace(R.id.container, selectedFragment);
        transaction.commit();

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    void setDefaultFragment()
    {
       FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, RankingFragment.newInstance(this, tinyDB));
        transaction.commit();
    }

    @Override
    public void onInternetConnectivityChanged(boolean isConnected) {
        if (!isConnected){
            showSnackbar();
        }
    }
}
