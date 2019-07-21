package com.lpwoowatpokpt.passportrankingjava.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.lpwoowatpokpt.passportrankingjava.Common.Common;
import com.lpwoowatpokpt.passportrankingjava.Model.Country;
import com.lpwoowatpokpt.passportrankingjava.R;
import com.lpwoowatpokpt.passportrankingjava.UI.CountryDetail;

import java.util.ArrayList;

import es.dmoral.toasty.Toasty;

public class PassportAdapter extends RecyclerView.Adapter<PassportAdapter.ViewHolder>
implements Filterable {

     private Context context;
     ArrayList<Country> mCountryList;
     private ArrayList<Country>mFilteredList;
     private CustomFilter filter;

    public PassportAdapter(Context context, ArrayList<Country> mCountryList) {
        this.context = context;
        this.mCountryList = mCountryList;
        this.mFilteredList = mCountryList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.country_item,parent,false);
        return new ViewHolder(view);
    }


    @SuppressLint({"ResourceAsColor", "CheckResult"})
    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, final int pos) {
        viewHolder.countryName.setText(mCountryList.get(pos).getKey());
        viewHolder.countryStatus.setText(String.valueOf(mCountryList.get(pos).getValue()));

        if (viewHolder.countryStatus.getText().toString().matches("0")){
            viewHolder.countryStatus.setText(R.string.visa_required);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                viewHolder.countryStatus.setTextColor(context.getColor(R.color.visa_requiered));
            }
        }
        else if (viewHolder.countryStatus.getText().toString().matches("1")){
            viewHolder.countryStatus.setText(R.string.on_arrival);
             if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                 viewHolder.countryStatus.setTextColor(context.getColor(R.color.visa_on_arrival));
            }
        }
        else if (viewHolder.countryStatus.getText().toString().matches("2")){
            viewHolder.countryStatus.setText(R.string.eTA);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                viewHolder.countryStatus.setTextColor(context.getColor(R.color.eTa));
            }
        }
        else if (viewHolder.countryStatus.getText().toString().matches("3")){
            viewHolder.countryStatus.setText(R.string.visa_free);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                viewHolder.countryStatus.setTextColor(context.getColor(R.color.visa_free));
            }
        }
        else if (viewHolder.countryStatus.getText().toString().matches("-1")){
            viewHolder.countryStatus.setText("-_-");
        }

            if (mCountryList==mFilteredList){
                Glide.with(context)
                        .load(Common.countryModel.get(pos).getImage())
                        .into(viewHolder.countryFlag);
            }else {
                DatabaseReference country_model = Common.getDatabase().getReference(Common.Country_Model);
                country_model.orderByChild(Common.Name).equalTo(mCountryList.get(pos).getKey())
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for (DataSnapshot postSnap: dataSnapshot.getChildren()){
                                    String flag = (String) postSnap.child(Common.Flag).getValue();

                                    Glide.with(context)
                                            .load(flag)
                                            .into(viewHolder.countryFlag);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                Toasty.error(context, context.getString(R.string.error_toast) + databaseError.getMessage(),5).show();
                            }
                        });
            }

        viewHolder.card.setOnClickListener(v -> {
            Common.COUNTRY = mCountryList.get(pos).getKey();
            v.getContext().startActivity(new Intent(context.getApplicationContext(), CountryDetail.class));
        });
    }

    @Override
    public int getItemCount() {
        return mCountryList.size();
    }

    @Override
    public Filter getFilter() {
        if (filter==null){
            filter = new CustomFilter(this, mCountryList);
        }
        return filter;
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        private TextView countryName, countryStatus;
        private ImageView countryFlag;
        private LinearLayout card;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            countryName = itemView.findViewById(R.id.countryName);
            countryStatus = itemView.findViewById(R.id.visa_status);
            countryFlag = itemView.findViewById(R.id.flag);
            card = itemView.findViewById(R.id.header);
        }


    }



}
