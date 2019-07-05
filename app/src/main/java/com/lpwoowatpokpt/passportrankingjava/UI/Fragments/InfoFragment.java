package com.lpwoowatpokpt.passportrankingjava.UI.Fragments;


import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.lpwoowatpokpt.passportrankingjava.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class InfoFragment extends Fragment {



    public static InfoFragment newInstance()
    {
        return new InfoFragment();
    }

    public InfoFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View myFragment = inflater.inflate(R.layout.fragment_info, container, false);

        TextView apiUrl= myFragment.findViewById(R.id.apiUrl);
        apiUrl.setMovementMethod(LinkMovementMethod.getInstance());;

        return myFragment;
    }
}
