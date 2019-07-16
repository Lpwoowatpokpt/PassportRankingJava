package com.lpwoowatpokpt.passportrankingjava.ViewHolder;

import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.lpwoowatpokpt.passportrankingjava.R;

public class RankingViewHolder extends RecyclerView.ViewHolder {

    public TextView countryNameTxt, countryPositionTxt, total_ScoreTxt, visa_freeTxt, visa_requieredTxt, visa_on_arrivalTxt, eTaTxt;
    public ImageView coverImg, expandBtn, collapseBtn;
    public ProgressBar countryProgress;
    public LinearLayout layout;
    public CardView cardView;


    public RankingViewHolder(@NonNull View itemView) {
        super(itemView);
        countryNameTxt = itemView.findViewById(R.id.countryNameTxt);
        countryPositionTxt = itemView.findViewById(R.id.countryPositionTxt);
        total_ScoreTxt = itemView.findViewById(R.id.total);
        visa_freeTxt = itemView.findViewById(R.id.visa_free);
        visa_requieredTxt = itemView.findViewById(R.id.visaRequiered);
        visa_on_arrivalTxt = itemView.findViewById(R.id.visa_on_arrival);
        eTaTxt = itemView.findViewById(R.id.eTa);
        coverImg = itemView.findViewById(R.id.coverImg);
        countryProgress = itemView.findViewById(R.id.countryProgress);
        expandBtn = itemView.findViewById(R.id.expand);
        collapseBtn = itemView.findViewById(R.id.collapse);
        layout = itemView.findViewById(R.id.ranking);
        cardView = itemView.findViewById(R.id.cardView);
    }
}
