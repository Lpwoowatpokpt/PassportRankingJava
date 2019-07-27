package com.lpwoowatpokpt.passportrankingjava.UI;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

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
import com.lpwoowatpokpt.passportrankingjava.Common.Common;
import com.lpwoowatpokpt.passportrankingjava.Common.TinyDB;
import com.lpwoowatpokpt.passportrankingjava.Common.Utils;
import com.lpwoowatpokpt.passportrankingjava.R;
import com.lpwoowatpokpt.passportrankingjava.UI.Fragments.CompareFragment;
import com.lpwoowatpokpt.passportrankingjava.UI.Fragments.InfoFragment;
import com.lpwoowatpokpt.passportrankingjava.UI.Fragments.MapFragment;
import com.lpwoowatpokpt.passportrankingjava.UI.Fragments.RankingFragment;
import com.lpwoowatpokpt.passportrankingjava.UI.Fragments.TopFragment;
import com.mahfa.dnswitch.DayNightSwitch;

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

    TextView day_nightTxt;
    DayNightSwitch dayNightSwitch;
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
                                .setDefaultFontPath("fonts/Roboto-Bold.ttf")
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
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            showToolsDialogue();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showToolsDialogue() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        final View settingsLayout = getLayoutInflater().inflate(R.layout.settings_layout, null);
        builder.setView(settingsLayout);

        day_nightTxt = settingsLayout.findViewById(R.id.day_night_switchTxt);
        dayNightSwitch = settingsLayout.findViewById(R.id.day_night_switch);
        dayNightSwitch.setDuration(300);

        if (tinyDB.getBoolean(Common.IS_DARK_MODE, true)){
            darkModeOn();
            dayNightSwitch.setIsNight(true);
        } else
            darkModeOff();

        dayNightSwitch.setListener(isNight -> {
            if (!isNight){
                Toasty.info(getBaseContext(), getString(R.string.dark_mode_off), Toast.LENGTH_SHORT, true).show();
                darkModeOff();
            }else {
                Toasty.info(getBaseContext(), getString(R.string.dark_mode_on), Toast.LENGTH_SHORT, true).show();
                darkModeOn();
            }

            Utils.ChangeToTheme(this);
        });

        builder.setNegativeButton(getString(R.string.close), (dialogInterface, i) -> dialogInterface.dismiss());
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void darkModeOff() {
        day_nightTxt.setText(getString(R.string.dark_mode_off));
        tinyDB.putBoolean(Common.IS_DARK_MODE, false);
        tinyDB.putInt(Common.THEME_ID,0);
    }

    private void darkModeOn() {
        day_nightTxt.setText(getString(R.string.dark_mode_on));
        tinyDB.putBoolean(Common.IS_DARK_MODE, true);
        tinyDB.putInt(Common.THEME_ID,1);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        Fragment selectedFragment = null;
        int id = item.getItemId();

        switch (id){
            case R.id.nav_ranking:
                selectedFragment = RankingFragment.newInstance(this);
                toolbar.setTitle(getString(R.string.menu_ranking));
                break;
            case R.id.nav_compare:
                selectedFragment = CompareFragment.newInstance(this);
                toolbar.setTitle(getString(R.string.menu_compare));
                break;
            case R.id.nav_top:
                selectedFragment = TopFragment.newInstance(this);
                toolbar.setTitle(getString(R.string.menu_top));
                break;
            case R.id.nav_map:
                selectedFragment = MapFragment.newInstance(this);
                toolbar.setTitle(tinyDB.getString(Common.COUNTRY_NAME));
                break;
            case R.id.nav_info:
                selectedFragment = InfoFragment.newInstance();
                toolbar.setTitle(getString(R.string.menu_info));
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
        transaction.replace(R.id.container, RankingFragment.newInstance(this));
        transaction.commit();
    }

    @Override
    public void onInternetConnectivityChanged(boolean isConnected) {
        if (!isConnected){
            showSnackbar();
        }
    }
}
