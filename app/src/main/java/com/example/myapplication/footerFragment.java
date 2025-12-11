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
        ImageButton ic_ststs = view.findViewById(R.id.M1);
        ImageButton ic_list = view.findViewById(R.id.M2);
        ImageButton ic_home = view.findViewById(R.id.M3);
        ImageButton ic_cart = view.findViewById(R.id.M4);
        ImageButton ic_settings = view.findViewById(R.id.M5);

        // מאזינים ללחיצות
        ic_ststs.setOnClickListener(v -> startActivity(new Intent(requireActivity(), Economy.class)));
        ic_list.setOnClickListener(v -> startActivity(new Intent(requireActivity(), Display_Calender_Tasks.class)));
        ic_home.setOnClickListener(v -> startActivity(new Intent(requireActivity(), Home.class)));
        ic_cart.setOnClickListener(v -> startActivity(new Intent(requireActivity(), Shopping.class)));
        ic_settings.setOnClickListener(v -> startActivity(new Intent(requireActivity(), Settings.class)));

        return view; // ✅ חשוב!
    }
}
