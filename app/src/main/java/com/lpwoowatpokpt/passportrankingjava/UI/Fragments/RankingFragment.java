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

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.CircularProgressDrawable;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
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


public class RankingFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    private Context context;
    private TinyDB tinyDB;
    private DatabaseReference topRanking, countries;

    private RankingFragment(Context context, TinyDB tinyDB) {
        this.context = context;
        this.tinyDB = tinyDB;
        countries = Common.getDatabase().getReference(Common.Countries);
        countries.keepSynced(true);
        topRanking = Common.getDatabase().getReference(Common.Top);
        setHasOptionsMenu(true);
    }

    public static RankingFragment newInstance(Context context, TinyDB tinyDB)
    {
        return new RankingFragment(context, tinyDB);
    }

    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private PassportAdapter passportAdapter;

    private TextView txtTotalScore;
    private TextView txtVisaFree;
    private TextView tatVisaOnArrival;
    private TextView txtEta;
    private TextView tatVisaRequired;

    private ImageView expandBtn;
    private LinearLayout ranking;



    public RankingFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View myFragment = inflater.inflate(R.layout.fragment_ranking, container, false);

        TextView name = myFragment.findViewById(R.id.name);
        ImageView cover = myFragment.findViewById(R.id.passportCover);
        cover.setOnClickListener(view -> scaleCoverImage(tinyDB.getString(Common.COVER)));

        txtTotalScore = myFragment.findViewById(R.id.total);
        tatVisaOnArrival = myFragment.findViewById(R.id.visa_on_arrival);
        txtEta = myFragment.findViewById(R.id.eTa);
        txtVisaFree = myFragment.findViewById(R.id.visa_free);
        tatVisaRequired = myFragment.findViewById(R.id.visaRequiered);

        name.setText(tinyDB.getString(Common.COUNTRY_NAME));

        CircularProgressDrawable circularProgressDrawable = new CircularProgressDrawable(context);
        circularProgressDrawable.setStrokeWidth(5f);
        circularProgressDrawable.setCenterRadius(30f);
        circularProgressDrawable.start();

        Glide.with(context)
                .load(tinyDB.getString(Common.COVER))
                .apply(RequestOptions.placeholderOf(circularProgressDrawable))
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

        CardView header = myFragment.findViewById(R.id.header);
        header.setOnClickListener(v -> {
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
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
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
                        int visa_onArrival = Collections.frequency(status, (long) 1);
                        int visa_required = Collections.frequency(status, (long) 0);
                        int total = visa_free+visa_onArrival+visa_eta;

                        updateTop(tinyDB.getString(Common.COUNTRY_NAME), tinyDB.getString(Common.COVER), total, visa_free, visa_onArrival, visa_required, visa_eta);

                        txtTotalScore.setText(String.valueOf(total));
                        txtVisaFree.setText(String.valueOf(visa_free));
                        txtEta.setText(String.valueOf(visa_eta));
                        tatVisaOnArrival.setText(String.valueOf(visa_onArrival));
                        tatVisaRequired.setText(String.valueOf(visa_required));

                        tinyDB.putInt(Common.MOBILITY_SCORE, total);
                        tinyDB.putListLong(Common.STATUS,status);

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

    @Override
    public void onRefresh() {
        loadRecyclerViewData();
    }

    private void updateTop(String countryName, String coverPath, int total, int visa_free, int visa_onArrival, int visa_requiered, int visa_eta) {
        topRanking.child(countryName)
                .setValue(new Ranking(countryName,coverPath,total,visa_free,visa_onArrival,visa_eta,visa_requiered));
    }

    private void loadRecyclerViewData() {
        swipeRefreshLayout.setRefreshing(true);
        passportAdapter = new PassportAdapter(context, getCountries(), tinyDB);
        recyclerView.setAdapter(passportAdapter);
    }
}