package com.lpwoowatpokpt.passportrankingjava.UI.Fragments;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.lpwoowatpokpt.passportrankingjava.Adapter.PassportAdapter;
import com.lpwoowatpokpt.passportrankingjava.Common.Common;
import com.lpwoowatpokpt.passportrankingjava.Common.TinyDB;
import com.lpwoowatpokpt.passportrankingjava.Model.Country;
import com.lpwoowatpokpt.passportrankingjava.Model.Ranking;
import com.lpwoowatpokpt.passportrankingjava.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

import es.dmoral.toasty.Toasty;
import in.galaxyofandroid.spinerdialog.SpinnerDialog;


public class RankingFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    private Context context;
    private TinyDB tinyDB;
    private DatabaseReference topRanking, countries;

    private RankingFragment(Context context) {
        this.context = context;
        tinyDB = new TinyDB(context);
        countries = Common.getDatabase().getReference(Common.Countries);
        countries.keepSynced(true);
        topRanking = Common.getDatabase().getReference(Common.Top);
        setHasOptionsMenu(true);
    }

    public static RankingFragment newInstance(Context context)
    {
        return new RankingFragment(context);
    }

    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private PassportAdapter passportAdapter;

    private SpinnerDialog spinnerDialog;

    private TextView name, txtTotalScore, txtVisaFree, tatVisaOnArrival, txtEta, tatVisaRequired;
    private ImageView cover;

    private ImageView expandBtn;
    private LinearLayout ranking;



    public RankingFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View myFragment = inflater.inflate(R.layout.fragment_ranking, container, false);

        name = myFragment.findViewById(R.id.name);
        cover = myFragment.findViewById(R.id.passportCover);
        cover.setOnClickListener(view -> scaleCoverImage(tinyDB.getString(Common.COVER)));

        txtTotalScore = myFragment.findViewById(R.id.total);
        tatVisaOnArrival = myFragment.findViewById(R.id.visa_on_arrival);
        txtEta = myFragment.findViewById(R.id.eTa);
        txtVisaFree = myFragment.findViewById(R.id.visa_free);
        tatVisaRequired = myFragment.findViewById(R.id.visaRequiered);

        name.setText(tinyDB.getString(Common.COUNTRY_NAME));

        Glide.with(context)
                .load(tinyDB.getString(Common.COVER))
                .into(cover);

        expandBtn = myFragment.findViewById(R.id.expand);
        ranking = myFragment.findViewById(R.id.ranking);

        if (tinyDB.getBoolean(Common.IS_EXPAND,true)){
            ranking.setVisibility(View.VISIBLE);
            expandBtn.setImageDrawable(getResources().getDrawable(R.drawable.ic_arrow_drop_up_black_24dp));
        }else {
            ranking.setVisibility(View.GONE);
            expandBtn.setImageDrawable(getResources().getDrawable(R.drawable.ic_arrow_drop_down_black_24dp));
        }

        expandBtn.setOnClickListener(v -> {
            if (tinyDB.getBoolean(Common.IS_EXPAND,true)){
                ranking.setVisibility(View.GONE);
                expandBtn.setImageDrawable(getResources().getDrawable(R.drawable.ic_arrow_drop_down_black_24dp));
                tinyDB.putBoolean(Common.IS_EXPAND, false);
            }else {
                ranking.setVisibility(View.VISIBLE);
                expandBtn.setImageDrawable(getResources().getDrawable(R.drawable.ic_arrow_drop_up_black_24dp));
                tinyDB.putBoolean(Common.IS_EXPAND, true);
            }

        });

        CardView header = myFragment.findViewById(R.id.header);
        header.setOnClickListener(v -> {
            if (Common.isConnectedToInternet(context))
                showDialog();
            else {
                Toasty.warning(context, getString(R.string.no_internet), Toast.LENGTH_SHORT, true).show();
            }
        });

        recyclerView = myFragment.findViewById(R.id.recycler);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        swipeRefreshLayout = myFragment.findViewById(R.id.swipe_to_refresh);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorAccent);

        loadRecyclerViewData();
        return myFragment;
    }

    private void scaleCoverImage(String coverPath) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);

        alertDialog.setCancelable(true);

        LayoutInflater inflater = this.getLayoutInflater();
        @SuppressLint("InflateParams") View scale_dialogue = inflater.inflate(R.layout.passport_item, null);

        ImageView coverScaled = scale_dialogue.findViewById(R.id.coverScale);
        Glide.with(context).load(coverPath).into(coverScaled);

        final AlertDialog alert = alertDialog.create();
        alert.setView(scale_dialogue);

        alert.show();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_search, menu);
        MenuItem mSearch = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView)mSearch.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                passportAdapter.getFilter().filter(s);
                return false;
            }
        });
    }

    private void showDialog() {
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = this.getLayoutInflater();
        View update_dialog = inflater.inflate(R.layout.change_region_dialog, null);

        alertDialog.setView(update_dialog);
        alertDialog.setNegativeButton("NO", (dialogInterface, i) -> dialogInterface.dismiss());

        alertDialog.setPositiveButton("YES", (dialogInterface, i) -> {
            dialogInterface.dismiss();
            showSpinner();
            spinnerDialog.showSpinerDialog();
        });
        alertDialog.show();
    }

    private void showSpinner() {
        spinnerDialog=new SpinnerDialog(getActivity(), tinyDB.getListString(Common.COUNTRY_LIST), getString(R.string.select_your_country), R.style.DialogAnimations_SmileWindow,"Close");

        spinnerDialog.setCancellable(true);
        spinnerDialog.setShowKeyboard(false);

        spinnerDialog.setTitleColor(getResources().getColor(R.color.colorAccent));
        spinnerDialog.setTitleColor(getResources().getColor(R.color.colorPrimaryText));
        spinnerDialog.setCloseColor(getResources().getColor(R.color.visa_requiered));

        spinnerDialog.bindOnSpinerListener((s, pos) -> {

            String countryName = Common.countryModel.get(pos).getName();

            name.setText(countryName);

            Glide.with(context)
                    .load(Common.countryModel.get(pos).getCover())
                    .into(cover);

            tinyDB.putString(Common.COUNTRY_NAME, countryName);
            tinyDB.putString(Common.COVER, Common.countryModel.get(pos).getCover());

            tinyDB.putDouble(Common.LATITUDE, Common.countryModel.get(pos).getLatitude());
            tinyDB.putDouble(Common.LONGITUDE, Common.countryModel.get(pos).getLongitude());

            onRefresh();
        });
    }

    private ArrayList<Country> getCountries(){
        final ArrayList<Country>countryList = new ArrayList<>();

        Query query = countries.orderByKey().equalTo(tinyDB.getString(Common.COUNTRY_NAME));
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
                        tatVisaOnArrival.setText(String.valueOf(visa_onArraival));
                        tatVisaRequired.setText(String.valueOf(visa_requiered));
                        tinyDB.putListLong(Common.STATUS,status);

                       // updateTop(tinyDB.getString(Common.COUNTRY_NAME), tinyDB.getString(Common.COVER), total,visa_free,visa_onArraival,visa_requiered,visa_eta);

                        passportAdapter.notifyDataSetChanged();
                        swipeRefreshLayout.setRefreshing(false);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toasty.error(context, getString(R.string.error_toast) + databaseError.getMessage(),5).show();
            }
        });
        return countryList;
    }




    private void updateTop(String countryName, String coverPath, int total, int visa_free, int visa_onArrival, int visa_requiered, int visa_eta) {
        topRanking.child(countryName)
                .setValue(new Ranking(countryName,coverPath,total,visa_free,visa_onArrival,visa_eta,visa_requiered));
    }


    @Override
    public void onRefresh() {
        loadRecyclerViewData();
    }

    private void loadRecyclerViewData() {
        swipeRefreshLayout.setRefreshing(true);
        passportAdapter = new PassportAdapter(context, getCountries());
        recyclerView.setAdapter(passportAdapter);
    }
}