package com.lpwoowatpokpt.passportrankingjava.UI.Fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.CircularProgressDrawable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.lpwoowatpokpt.passportrankingjava.Common.Common;
import com.lpwoowatpokpt.passportrankingjava.Model.Ranking;
import com.lpwoowatpokpt.passportrankingjava.R;
import com.lpwoowatpokpt.passportrankingjava.ViewHolder.RankingViewHolder;

import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 */
public class TopFragment extends Fragment {

    private DatabaseReference ranking;
    private Context context;

    private TopFragment(Context context) {
        this.context = context;
        ranking = Common.getDatabase().getReference(Common.Top);
        ranking.keepSynced(true);
    }

    public static TopFragment newInstance(Context context)
    {
        return new TopFragment(context);
    }


    private RecyclerView recyclerView;
    private FirebaseRecyclerAdapter<Ranking, RankingViewHolder>adapter;
    private ProgressBar loadingInfoBar;

    public TopFragment() {
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View myFragment = inflater.inflate(R.layout.fragment_top, container, false);
        recyclerView = myFragment.findViewById(R.id.recycler);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        recyclerView.setHasFixedSize(true);

        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        loadingInfoBar = myFragment.findViewById(R.id.loading_recycler);

        if (Common.isConnectedToInternet(context))
        populateTopList();

        return myFragment;
    }

    private void populateTopList() {
        FirebaseRecyclerOptions<Ranking> options = new FirebaseRecyclerOptions.Builder<Ranking>()
                .setQuery(ranking.orderByChild("totalScore"), Ranking.class)
                .build();

        adapter = new FirebaseRecyclerAdapter<Ranking, RankingViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final RankingViewHolder rankingViewHolder, final int position, @NonNull Ranking ranking) {
                rankingViewHolder.countryPositionTxt.setText(String.valueOf(ranking.getTotalScore()));
                rankingViewHolder.countryNameTxt.setText(ranking.getName());

                CircularProgressDrawable circularProgressDrawable = new CircularProgressDrawable(context);
                circularProgressDrawable.setStrokeWidth(5f);
                circularProgressDrawable.setCenterRadius(30f);
                circularProgressDrawable.start();

                Glide.with(context)
                        .load(ranking.getCover())
                        .apply(RequestOptions.placeholderOf(circularProgressDrawable))
                        .into(rankingViewHolder.coverImg);

                rankingViewHolder.countryProgress.setProgress(ranking.getVisaOnArrival());
                rankingViewHolder.countryProgress.setSecondaryProgress(ranking.getTotalScore());

                rankingViewHolder.total_ScoreTxt.setText(String.valueOf(ranking.getTotalScore()));
                rankingViewHolder.visa_freeTxt.setText(String.valueOf(ranking.getVisaFree()));
                rankingViewHolder.visa_on_arrivalTxt.setText(String.valueOf(ranking.getVisaOnArrival()));
                rankingViewHolder.visa_requieredTxt.setText(String.valueOf(ranking.getVisaRequiered()));
                rankingViewHolder.eTaTxt.setText(String.valueOf(ranking.geteTa()));


                rankingViewHolder.cardView.setOnClickListener(view -> {
                    if (rankingViewHolder.expandBtn.getVisibility()==View.VISIBLE){
                        rankingViewHolder.expandBtn.setVisibility(View.GONE);
                        rankingViewHolder.collapseBtn.setVisibility(View.VISIBLE);
                        rankingViewHolder.layout.setVisibility(View.VISIBLE);
                    }else {
                        rankingViewHolder.expandBtn.setVisibility(View.VISIBLE);
                        rankingViewHolder.collapseBtn.setVisibility(View.GONE);
                        rankingViewHolder.layout.setVisibility(View.GONE);
                    }
                });

            }

            @NonNull
            @Override
            public RankingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.top_item, parent, false);
                return new RankingViewHolder(itemView);
            }
        };

        recyclerView.setAdapter(adapter);
        Objects.requireNonNull(recyclerView.getAdapter()).notifyDataSetChanged();

        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                if (itemCount!=0)
                    loadingInfoBar.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        if(adapter!=null)
            adapter.startListening();
    }

    @Override
    public void onStop() {
        if(adapter!=null)
            adapter.stopListening();
        super.onStop();
    }

    @Override
    public void onResume() {
        super.onResume();
        if(adapter!=null)
            adapter.startListening();
    }

}
