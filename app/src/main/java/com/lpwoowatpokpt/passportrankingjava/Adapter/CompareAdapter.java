package com.lpwoowatpokpt.passportrankingjava.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.lpwoowatpokpt.passportrankingjava.ExcelModel.Cell;
import com.lpwoowatpokpt.passportrankingjava.ExcelModel.ColTitle;
import com.lpwoowatpokpt.passportrankingjava.ExcelModel.RowTitle;
import com.lpwoowatpokpt.passportrankingjava.R;

import cn.zhouchaoyuan.excelpanel.BaseExcelPanelAdapter;

public class CompareAdapter extends BaseExcelPanelAdapter<RowTitle, ColTitle, Cell>{

    private Context context;
    private View.OnClickListener onClickListener;

    public CompareAdapter(Context context, View.OnClickListener onClickListener) {
        super(context);
        this.context = context;
        this.onClickListener = onClickListener;
    }


    @Override
    public RecyclerView.ViewHolder onCreateCellViewHolder(ViewGroup parent, int viewType) {
        View layout = LayoutInflater.from(parent.getContext()).inflate(R.layout.country_status_normal_cell, parent, false);
        return new CellHolder(layout);
    }

    @Override
    public void onBindCellViewHolder(RecyclerView.ViewHolder holder, int verticalPosition, int horizontalPosition){
        Cell cell = getMajorItem(verticalPosition, horizontalPosition);
        CellHolder viewHolder = (CellHolder)holder;

        switch (cell.getStatus()){
            case -1:
                viewHolder.countryStatusTxt.setText("-_-");
                viewHolder.backgroundColor.setBackgroundColor(ContextCompat.getColor(context, R.color.cell_border_color));
                break;
            case 0:
               viewHolder.countryStatusTxt.setText(R.string.visa_required);
               viewHolder.backgroundColor.setBackgroundColor(ContextCompat.getColor(context, R.color.visa_required));
            break;

            case 1:
                viewHolder.countryStatusTxt.setText(R.string.on_arrival);
                viewHolder.backgroundColor.setBackgroundColor(ContextCompat.getColor(context, R.color.visa_on_arrival));
                break;

            case 2:
                viewHolder.countryStatusTxt.setText(R.string.eTA);
                viewHolder.backgroundColor.setBackgroundColor(ContextCompat.getColor(context, R.color.eTa));
                break;

            case 3:
                viewHolder.countryStatusTxt.setText(R.string.visa_free);
                viewHolder.backgroundColor.setBackgroundColor(ContextCompat.getColor(context, R.color.visa_free));
                break;

        }
    }

    static class CellHolder extends RecyclerView.ViewHolder{

        final TextView countryStatusTxt;
        final RelativeLayout backgroundColor;

        CellHolder(@NonNull View itemView) {
            super(itemView);
            countryStatusTxt = itemView.findViewById(R.id.visaStatusTxt);
            backgroundColor = itemView.findViewById(R.id.background);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateTopViewHolder(ViewGroup parent, int viewType) {
        View layout = LayoutInflater.from(parent.getContext()).inflate(R.layout.country_top_cell, parent, false);
        return new TopHolder(layout);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindTopViewHolder(RecyclerView.ViewHolder holder, int position) {
        RowTitle rowTitle = getTopItem(position);

        TopHolder viewHolder = (TopHolder)holder;

        Glide.with(context).load(rowTitle.getCover()).into(viewHolder.passportCover);
        viewHolder.countryName.setText(rowTitle.getCountryName());
        viewHolder.mobilityScore.setText("score : " + rowTitle.getMobilityScore());
    }

    static class TopHolder extends RecyclerView.ViewHolder{

        final TextView countryName;
        final TextView mobilityScore;
        final ImageView passportCover;

        TopHolder(@NonNull View itemView) {
            super(itemView);
            countryName = itemView.findViewById(R.id.countryNameTxt);
            mobilityScore = itemView.findViewById(R.id.mobilityScoreTxt);
            passportCover = itemView.findViewById(R.id.passportCoverImg);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateLeftViewHolder(ViewGroup parent, int viewType) {
        View layout = LayoutInflater.from(parent.getContext()).inflate(R.layout.country_left_cell, parent, false);
        return new LeftHolder(layout);
    }

    @Override
    public void onBindLeftViewHolder(RecyclerView.ViewHolder holder, int position) {
        ColTitle colTitle = getLeftItem(position);

        LeftHolder viewHolder = (LeftHolder)holder;
        viewHolder.countryName.setText(colTitle.getName());
        Glide.with(context).load(colTitle.getImage()).into(viewHolder.countryFlag);
    }

    static class LeftHolder extends RecyclerView.ViewHolder{

        final TextView countryName;
        final ImageView countryFlag;

        LeftHolder(@NonNull View itemView) {
            super(itemView);
            countryName = itemView.findViewById(R.id.countryNameTxt);
            countryFlag = itemView.findViewById(R.id.flagImg);
        }
    }


    @SuppressLint("InflateParams")
    @Override
    public View onCreateTopLeftView() {
        return LayoutInflater.from(context).inflate(R.layout.country_normal_cell,null);
    }
}

