package com.clocktower.lullaby.view.Fragments;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.clocktower.lullaby.interfaces.FragmentListener;
import com.clocktower.lullaby.view.activities.Alarm;

public class BaseFragment extends Fragment {

    protected FragmentListener listener;


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        listener = (FragmentListener)context;
    }
}
