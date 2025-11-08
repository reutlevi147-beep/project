package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

public class footerFragment extends Fragment {

    public footerFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_footer, container, false);

        // חיבור לכפתורים דרך ה-View של הפרגמנט
        ImageButton m1 = view.findViewById(R.id.M1);
        ImageButton m2 = view.findViewById(R.id.M2);
        ImageButton m3 = view.findViewById(R.id.M3);
        ImageButton m4 = view.findViewById(R.id.M4);
        ImageButton m5 = view.findViewById(R.id.M5);

        // מאזינים ללחיצות
        m1.setOnClickListener(v -> startActivity(new Intent(requireActivity(), Economy.class)));
        m2.setOnClickListener(v -> startActivity(new Intent(requireActivity(), Display_Calender_Tasks.class)));
        m3.setOnClickListener(v -> startActivity(new Intent(requireActivity(), Home.class)));
        m4.setOnClickListener(v -> startActivity(new Intent(requireActivity(), Shopping.class)));
        m5.setOnClickListener(v -> startActivity(new Intent(requireActivity(), Settings.class)));

        return view; // ✅ חשוב!
    }
}
