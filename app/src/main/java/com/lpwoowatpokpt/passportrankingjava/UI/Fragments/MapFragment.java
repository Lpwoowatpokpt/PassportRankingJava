package com.lpwoowatpokpt.passportrankingjava.UI.Fragments;


import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.koushikdutta.ion.Ion;
import com.lpwoowatpokpt.passportrankingjava.Common.Common;
import com.lpwoowatpokpt.passportrankingjava.Common.TinyDB;
import com.lpwoowatpokpt.passportrankingjava.R;

import java.util.List;
import java.util.concurrent.ExecutionException;

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

        mMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap mMap) {
                googleMap = mMap;

                try {
                    boolean success = googleMap.setMapStyle(
                            MapStyleOptions.loadRawResourceStyle(
                                    context, R.raw.mapstyle));

                    if (!success) {
                        Log.e("Map", "Style parsing failed.");
                    }
                } catch (Resources.NotFoundException e) {
                    Log.e("Map", "Can't find style. Error: ", e);
                }

                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(getActivity(),
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, Common.RequestCameraPermissionId);
                    return;
                }

                googleMap.setMyLocationEnabled(true);

                mMap.getUiSettings().setZoomControlsEnabled(true);

                Double lat = tinyDB.getDouble(Common.LATITUDE,0);
                Double longitude = tinyDB.getDouble(Common.LONGITUDE, 0);
                LatLng current = new LatLng(lat,longitude);
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(current,6f));

            }
        });
        return myFragment;
    }


    /*
    private void createMarker(Long latitude, Long longitude, String name, String image, Long status) {
        double lat = Double.parseDouble(String.valueOf(latitude));
        double lon = Double.parseDouble(String.valueOf(longitude));

        try {
            Bitmap bmImg = Ion.with(context)
                    .load(image)
                    .asBitmap()
                    .get();

            int height = 25;
            int width = 50;

            Bitmap smallMarker = Bitmap.createScaledBitmap(bmImg, width, height, false);

            String _status = null;
            if (status==0)
                _status = getString(R.string.visa_required);
            else if (status == 1)
                _status = getString(R.string.on_arrival);
            else if (status==2)
                _status = getString(R.string.eTA);
            else if (status==3)
                _status = getString(R.string.visa_free);

            googleMap.addMarker(new MarkerOptions()
                    .position(new LatLng(lat,lon))
                    .anchor(0.1f,0.1f)
                    .title(name)
                    .snippet(_status)
                    .icon(BitmapDescriptorFactory.fromBitmap(smallMarker)));



        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }*/

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
