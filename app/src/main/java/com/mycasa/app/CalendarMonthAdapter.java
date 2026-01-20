package com.mycasa.app;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CalendarMonthAdapter
        extends RecyclerView.Adapter<CalendarMonthAdapter.DayViewHolder> {

    public interface OnDayClickListener {
        void onDayClicked(int day);
    }

    private final List<Integer> days;
    private final List<Integer> daysWithEvents;
    private int selectedDay;
    private final OnDayClickListener listener;

    public CalendarMonthAdapter(
            List<Integer> days,
            List<Integer> daysWithEvents,
            int selectedDay,
            OnDayClickListener listener
    ) {
        this.days = days;
        this.daysWithEvents = daysWithEvents;
        this.selectedDay = selectedDay;
        this.listener = listener;
    }

    // שינוי יום נבחר
    public void setSelectedDay(int day) {
        this.selectedDay = day;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public DayViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_calendar_day, parent, false);
        return new DayViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull DayViewHolder holder,
            int position
    ) {
        int day = days.get(position);

        // תא ריק
        if (day == 0) {
            holder.tvDay.setText("");
            holder.tvDay.setBackground(null);
            holder.dot.setVisibility(View.GONE);
            holder.itemView.setOnClickListener(null);
            return;
        }

        holder.tvDay.setText(String.valueOf(day));

        // יום נבחר
        if (day == selectedDay) {
            holder.tvDay.setBackgroundResource(R.drawable.bg_day_selected);
            holder.tvDay.setTextColor(0xFFFFFFFF);
        } else {
            holder.tvDay.setBackgroundResource(R.drawable.bg_day_normal);
            holder.tvDay.setTextColor(0xFF111827);
        }

        // ● סימון יום עם אירועים
        holder.dot.setVisibility(
                daysWithEvents.contains(day)
                        ? View.VISIBLE
                        : View.GONE
        );

        holder.itemView.setOnClickListener(v -> {
            setSelectedDay(day);
            listener.onDayClicked(day);
        });
    }

    @Override
    public int getItemCount() {
        return days.size();
    }

    static class DayViewHolder extends RecyclerView.ViewHolder {

        TextView tvDay;
        View dot;

        DayViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDay = itemView.findViewById(R.id.tvDay);
            dot = itemView.findViewById(R.id.viewDot);
        }
    }
}
