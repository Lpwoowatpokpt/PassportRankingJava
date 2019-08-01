package com.lpwoowatpokpt.passportrankingjava.UI;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

import com.lpwoowatpokpt.passportrankingjava.Common.Common;
import com.lpwoowatpokpt.passportrankingjava.Common.TinyDB;
import com.lpwoowatpokpt.passportrankingjava.Common.Utils;
import com.lpwoowatpokpt.passportrankingjava.R;

import mehdi.sakout.aboutpage.AboutPage;
import mehdi.sakout.aboutpage.Element;

public class InfoActivity extends AppCompatActivity {

    TinyDB tinyDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        Element adsElement = new Element();
        adsElement.setTitle("Advertise with us");


        View aboutView = new AboutPage(this)
                .isRTL(false)
                .setImage(R.mipmap.ic_launcher)
                .addItem(new Element().setTitle(getString(R.string.version)))
                .setDescription(getString(R.string.about_app))
                .addItem(adsElement)
                .addGroup("Connect with us")
                .addEmail("alisasadkovska@gmail.com")
                .addGitHub("Lpwoowatpokpt")
                .addYoutube("youtube",getString(R.string.tutorial))
                .addPlayStore("your_id")
                .create();

        tinyDB = new TinyDB(this);
        Utils.onActivityCreateSetTheme(this, tinyDB.getInt(Common.THEME_ID));
        setContentView(aboutView);
    }
}
