package com.lpwoowatpokpt.passportrankingjava.UI.Fragments;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.lpwoowatpokpt.passportrankingjava.Common.Common;
import com.lpwoowatpokpt.passportrankingjava.Common.TinyDB;
import com.lpwoowatpokpt.passportrankingjava.R;
import com.lpwoowatpokpt.passportrankingjava.UI.CountryDetail;

import java.util.Objects;

import es.dmoral.toasty.Toasty;

/**
 * A simple {@link Fragment} subclass.
 */
public class MapFragment extends Fragment {

    private Context context;

    public static MapFragment newInstance(Context context)
    {
        return new MapFragment(context);
    }

    private TinyDB tinyDB;
    private MapView mMapView;
    private GoogleMap googleMap;

    private MapFragment(Context context) {
        this.context = context;
        tinyDB = new TinyDB(context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View myFragment = inflater.inflate(R.layout.fragment_map, container, false);

        mMapView = myFragment.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);
        mMapView.onResume();

        try {
            MapsInitializer.initialize(context);
        }catch (Exception e) {
            e.printStackTrace();
        }

        mMapView.getMapAsync(mMap -> {
            googleMap = mMap;

            try {

                int res;
                if (tinyDB.getInt(Common.THEME_ID)==0)
                    res = R.raw.mapstyle_light;
                else
                    res = R.raw.mapstyle;

                boolean success = googleMap.setMapStyle(
                        MapStyleOptions.loadRawResourceStyle(
                                context, res));

                if (!success) {
                    Toasty.error(context, "Style parsing failed.",5).show();
                }
            } catch (Resources.NotFoundException e) {
                Toasty.error(context, "Can't find style. Error: " + e,5).show();
            }

            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(Objects.requireNonNull(getActivity()),
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, Common.RequestCameraPermissionId);
                return;
            }

            googleMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setZoomControlsEnabled(true);

            Double lat = tinyDB.getDouble(Common.LATITUDE,0);
            Double longitude = tinyDB.getDouble(Common.LONGITUDE, 0);
            LatLng current = new LatLng(lat,longitude);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(current,6f));

            for (int i=0; i < Common.countryModel.size(); i++){
                createMarker(Common.countryModel.get(i).getLatitude(),
                        Common.countryModel.get(i).getLongitude(),
                        Common.countryModel.get(i).getName(),
                        tinyDB.getListLong(Common.STATUS).get(i));
            }

            googleMap.setOnInfoWindowClickListener(marker -> {
                Common.COUNTRY = marker.getTitle();
                Intent intent = new Intent(getActivity(), CountryDetail.class);
                startActivity(intent);
            });

        });
        return myFragment;
    }

    private void createMarker(Double latitude, Double longitude, String name, Long status){
        String _status = "";
        BitmapDescriptor icon;

        if (status==0){
            _status = getString(R.string.visa_required);
            icon = bitmapDescriptorFromVector(context, R.drawable.ic_vpn_lock_red_24dp);
        }
        else if (status == 1){
            _status = getString(R.string.on_arrival);
            icon = bitmapDescriptorFromVector(context, R.drawable.ic_flight_land_blue_24dp);
        }
        else if (status==2){
            _status = getString(R.string.eTA);
            if (tinyDB.getInt(Common.THEME_ID)==1)
            icon = bitmapDescriptorFromVector(context, R.drawable.ic_important_devices_yellow_24dp);
            else
                icon = bitmapDescriptorFromVector(context, R.drawable.ic_important_devices_orange_24dp);
        }
        else if (status==3){
            _status = getString(R.string.visa_free);
            icon = bitmapDescriptorFromVector(context, R.drawable.ic_flight_green_24dp);
        }
        else {
            if (tinyDB.getInt(Common.THEME_ID)==1)
            icon = bitmapDescriptorFromVector(context, R.drawable.ic_home_white_24dp);
            else
                icon = bitmapDescriptorFromVector(context, R.drawable.ic_home_black_24dp);
        }


        googleMap.addMarker(new MarkerOptions()
                .position(new LatLng(latitude, longitude))
                .anchor(.1f, .1f)
                .title(name)
                .snippet(_status)
                .icon(icon));


    }



    private BitmapDescriptor bitmapDescriptorFromVector(Context context, int vectorResId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }



    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    }
