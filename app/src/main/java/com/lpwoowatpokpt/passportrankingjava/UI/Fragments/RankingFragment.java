package com.lpwoowatpokpt.passportrankingjava.UI.Fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.lpwoowatpokpt.passportrankingjava.Adapter.PassportAdapter;
import com.lpwoowatpokpt.passportrankingjava.Common.Common;
import com.lpwoowatpokpt.passportrankingjava.Common.TinyDB;
import com.lpwoowatpokpt.passportrankingjava.Model.Country;
import com.lpwoowatpokpt.passportrankingjava.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

import in.galaxyofandroid.spinerdialog.OnSpinerItemClick;
import in.galaxyofandroid.spinerdialog.SpinnerDialog;

/**
 * A simple {@link Fragment} subclass.
 */
public class RankingFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    private Context context;

    public static RankingFragment newInstance(Context context)
    {
        return new RankingFragment(context);
    }

    private TinyDB tinyDB;

    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private PassportAdapter passportAdapter;

    private SpinnerDialog spinnerDialog;

    private TextView name, txtTotalScore, txtVisaFree, txtVisaOnArraival, txtEta, txtVisaRequiered;
    private ImageView cover;

    private ImageView expandBtn;
    private LinearLayout ranking;

    private RankingFragment(Context context) {
        this.context = context;
        tinyDB = new TinyDB(context);
        setHasOptionsMenu(true);
    }

    public RankingFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View myFragment = inflater.inflate(R.layout.fragment_ranking, container, false);

        name = myFragment.findViewById(R.id.name);
        cover = myFragment.findViewById(R.id.passportCover);
        txtTotalScore = myFragment.findViewById(R.id.total);
        txtVisaOnArraival = myFragment.findViewById(R.id.visa_on_arrival);
        txtEta = myFragment.findViewById(R.id.eTa);
        txtVisaFree = myFragment.findViewById(R.id.visa_free);
        txtVisaRequiered = myFragment.findViewById(R.id.visaRequiered);

        name.setText(tinyDB.getString(Common.COUNTRY_NAME));
        Glide.with(context).load(tinyDB.getString(Common.COVER))
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

        expandBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (tinyDB.getBoolean(Common.IS_EXPAND,true)){
                    ranking.setVisibility(View.GONE);
                    expandBtn.setImageDrawable(getResources().getDrawable(R.drawable.ic_arrow_drop_down_black_24dp));
                    tinyDB.putBoolean(Common.IS_EXPAND, false);
                }else {
                    ranking.setVisibility(View.VISIBLE);
                    expandBtn.setImageDrawable(getResources().getDrawable(R.drawable.ic_arrow_drop_up_black_24dp));
                    tinyDB.putBoolean(Common.IS_EXPAND, true);
                }

            }
        });

        CardView header = myFragment.findViewById(R.id.header);
        header.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog();
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
        alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                showSpinner();
                spinnerDialog.showSpinerDialog();
            }
        });
        alertDialog.show();
    }

    private void showSpinner() {
        spinnerDialog = new SpinnerDialog(getActivity(), tinyDB.getListString(Common.COUNTRY_LIST), getString(R.string.select_your_country));
        spinnerDialog.bindOnSpinerListener(new OnSpinerItemClick() {
            @Override
            public void onClick(String s, int pos) {

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
            }
        });
    }

    private ArrayList<Country> getCountries(){
        final ArrayList<Country>countryList = new ArrayList<>();

        Query query = Common.getDatabase().getReference(Common.Countries)
                .orderByKey().equalTo(tinyDB.getString(Common.COUNTRY_NAME));
        query.addValueEventListener(new ValueEventListener() {
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

                        tinyDB.putListLong(Common.STATUS,status);

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
        return countryList;
    }


    @Override
    public void onRefresh() {
        loadRecyclerViewData();
    }

    private void loadRecyclerViewData() {
        swipeRefreshLayout.setRefreshing(true);
        passportAdapter = new PassportAdapter(context, getCountries());
        recyclerView.setAdapter(passportAdapter);
        swipeRefreshLayout.setRefreshing(false);
        passportAdapter.notifyDataSetChanged();
    }
}
