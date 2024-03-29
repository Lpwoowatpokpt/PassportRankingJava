package com.lpwoowatpokpt.passportrankingjava.UI;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.lpwoowatpokpt.passportrankingjava.Common.Common;
import com.lpwoowatpokpt.passportrankingjava.Common.TinyDB;
import com.lpwoowatpokpt.passportrankingjava.Common.Utils;
import com.lpwoowatpokpt.passportrankingjava.R;
import com.mahfa.dnswitch.DayNightSwitch;

import es.dmoral.toasty.Toasty;
import in.galaxyofandroid.spinerdialog.SpinnerDialog;
import io.github.inflationx.calligraphy3.CalligraphyConfig;
import io.github.inflationx.calligraphy3.CalligraphyInterceptor;
import io.github.inflationx.viewpump.ViewPump;
import io.github.inflationx.viewpump.ViewPumpContextWrapper;

public class SettingsActivity extends AppCompatActivity {

    Toolbar toolbar;
    TinyDB tinyDB;
    LinearLayout view;

    TextView day_nightTxt;
    DayNightSwitch dayNightSwitch;

    private SpinnerDialog spinnerDialog;

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
                                .setDefaultFontPath("fonts/NanumGothic-Regular.ttf")
                                .setFontAttrId(R.attr.fontPath)
                                .build())).build());

        tinyDB = new TinyDB(this);
        Utils.onActivityCreateSetTheme(this, tinyDB.getInt(Common.THEME_ID));
        setContentView(R.layout.activity_settings);

        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.action_settings));
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        view = findViewById(R.id.view);

        Button changeBtn = findViewById(R.id.changeCountryBtn);
        changeBtn.setOnClickListener(view -> {
            if (Common.isConnectedToInternet(getApplicationContext())){
                showSpinner();
                spinnerDialog.showSpinerDialog();
            }

            else {
                Toasty.warning(getApplicationContext(), getString(R.string.no_internet), Toast.LENGTH_SHORT, true).show();
            }
        });

        day_nightTxt = findViewById(R.id.day_night_switchTxt);
        dayNightSwitch = findViewById(R.id.day_night_switch);
        dayNightSwitch.setDuration(500);

        if (tinyDB.getBoolean(Common.IS_DARK_MODE, true)) {
            darkModeOn();
            dayNightSwitch.setIsNight(true);
        } else
            darkModeOff();


        dayNightSwitch.setListener(isNight -> {
            if (!isNight) {
                Toasty.info(getBaseContext(), getString(R.string.dark_mode_off), Toast.LENGTH_SHORT, true).show();
                darkModeOff();
            } else {
                Toasty.info(getBaseContext(), getString(R.string.dark_mode_on), Toast.LENGTH_SHORT, true).show();
                darkModeOn();
            }
        });
    }

    private void showSpinner() {
        spinnerDialog=new SpinnerDialog(this, tinyDB.getListString(Common.COUNTRY_LIST), getString(R.string.select_your_country), R.style.DialogAnimations_SmileWindow,"Close");

        spinnerDialog.setCancellable(true);
        spinnerDialog.setShowKeyboard(false);

        spinnerDialog.setTitleColor(getResources().getColor(R.color.colorAccent));
        spinnerDialog.setTitleColor(getResources().getColor(R.color.colorPrimaryText));
        spinnerDialog.setCloseColor(getResources().getColor(R.color.visa_required));

        spinnerDialog.bindOnSpinerListener((s, pos) -> {
            String countryName = Common.countryModel.get(pos).getName();

            toolbar.setTitle(countryName);

            tinyDB.putString(Common.COUNTRY_NAME, countryName);
            tinyDB.putString(Common.COVER, Common.countryModel.get(pos).getCover());

            tinyDB.putDouble(Common.LATITUDE, Common.countryModel.get(pos).getLatitude());
            tinyDB.putDouble(Common.LONGITUDE, Common.countryModel.get(pos).getLongitude());

            Toasty.success(this, "changed to " + countryName, Toasty.LENGTH_SHORT).show();
        });
    }

    private void darkModeOff() {
        view.setBackgroundColor(getResources().getColor(R.color.colorWhite));
        day_nightTxt.setTextColor(getResources().getColor(R.color.colorBlack));
        day_nightTxt.setText(getString(R.string.dark_mode_off));
        tinyDB.putBoolean(Common.IS_DARK_MODE, false);
        tinyDB.putInt(Common.THEME_ID,0);
    }

    private void darkModeOn() {
        view.setBackgroundColor(getResources().getColor(R.color.colorBlack));
        day_nightTxt.setTextColor(getResources().getColor(R.color.colorWhite));
        day_nightTxt.setText(getString(R.string.dark_mode_on));
        tinyDB.putBoolean(Common.IS_DARK_MODE, true);
        tinyDB.putInt(Common.THEME_ID,1);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            goToHome();
        }

        return super.onOptionsItemSelected(item);
    }

    private void goToHome() {
        Intent intent = new Intent(SettingsActivity.this, Home.class);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        goToHome();
    }
}