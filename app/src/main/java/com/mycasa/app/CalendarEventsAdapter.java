package com.mycasa.app;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class CalendarEventsAdapter
        extends RecyclerView.Adapter<CalendarEventsAdapter.EventViewHolder> {

    private final List<DocumentSnapshot> events;

    public CalendarEventsAdapter(List<DocumentSnapshot> events) {
        this.events = events;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_calendar_event, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull EventViewHolder holder,
            int position
    ) {

        DocumentSnapshot doc = events.get(position);

        // ===== כותרת =====
        holder.tvTitle.setText(doc.getString("title"));

        // ===== שעה =====
        String start = doc.getString("startTime");
        String end = doc.getString("endTime");

        if (!TextUtils.isEmpty(start)) {
            holder.tvTime.setText(
                    TextUtils.isEmpty(end) ? start : start + " - " + end
            );
        } else {
            holder.tvTime.setText("");
        }

        // ===== מיועד ל– =====
        String assignedToLabel = doc.getString("assignedToLabel");

        if (TextUtils.isEmpty(assignedToLabel)) {
            holder.tvAssignedTo.setText("מיועד לכולם");
        } else {
            holder.tvAssignedTo.setText("מיועד ל־" + assignedToLabel);
        }

        // ===== צבע =====
        String colorId = doc.getString("color");
        if (!TextUtils.isEmpty(colorId)) {

            int baseColor;
            switch (colorId) {
                case "teal": baseColor = Color.parseColor("#14B8A6"); break;
                case "amber": baseColor = Color.parseColor("#F59E0B"); break;
                case "rose": baseColor = Color.parseColor("#F43F5E"); break;
                case "slate": baseColor = Color.parseColor("#64748B"); break;
                case "emerald": baseColor = Color.parseColor("#10B981"); break;
                case "indigo":
                default: baseColor = Color.parseColor("#6366F1"); break;
            }

            int softColor = Color.argb(
                    40,
                    Color.red(baseColor),
                    Color.green(baseColor),
                    Color.blue(baseColor)
            );

            GradientDrawable bg = new GradientDrawable();
            bg.setColor(softColor);
            bg.setCornerRadius(24f);
            holder.container.setBackground(bg);
        } else {
            holder.container.setBackground(null);
        }

        // ===== עריכה =====
        holder.container.setOnClickListener(v -> {
            Intent intent = new Intent(
                    holder.itemView.getContext(),
                    AddCalendarEventActivity.class
            );
            intent.putExtra("eventId", doc.getId());
            holder.itemView.getContext().startActivity(intent);
        });

        // ===== מחיקה עם דיאלוג =====
        holder.btnDelete.setOnClickListener(v -> {

            String repeatType = doc.getString("repeatType");
            String seriesId = doc.getString("seriesId");

            // חד־פעמי
            if (repeatType == null || repeatType.equals("once")) {

                new AlertDialog.Builder(v.getContext())
                        .setTitle("מחיקת אירוע")
                        .setMessage("האם למחוק את האירוע?")
                        .setPositiveButton("מחק", (d, w) ->
                                FirebaseFirestore.getInstance()
                                        .collection("calendar_events")
                                        .document(doc.getId())
                                        .delete()
                        )
                        .setNegativeButton("ביטול", null)
                        .show();
                return;
            }

            // חוזר
            new AlertDialog.Builder(v.getContext())
                    .setTitle("מחיקת אירוע חוזר")
                    .setItems(
                            new CharSequence[]{
                                    "מחק רק את האירוע הזה",
                                    "מחק את כל הסדרה",
                                    "ביטול"
                            },
                            (dialog, which) ->
                                    handleDeleteChoice(which, doc, seriesId)
                    )
                    .show();
        });
    }

    // ===== טיפול בבחירת מחיקה =====
    private void handleDeleteChoice(
            int which,
            DocumentSnapshot doc,
            String seriesId
    ) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        switch (which) {

            case 0: // רק האירוע הזה
                db.collection("calendar_events")
                        .document(doc.getId())
                        .update("excluded", true);

                break;

            case 1: // כל הסדרה
                if (seriesId == null) return;

                db.collection("calendar_events")
                        .whereEqualTo("seriesId", seriesId)
                        .get()
                        .addOnSuccessListener(snapshot -> {
                            for (DocumentSnapshot d : snapshot.getDocuments()) {
                                d.getReference().delete();
                            }
                        });
                break;

            default: // ביטול
                break;
        }
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    // ===== ViewHolder =====
    static class EventViewHolder extends RecyclerView.ViewHolder {

        LinearLayout container;
        TextView tvTitle, tvTime, tvAssignedTo;
        ImageButton btnDelete;

        EventViewHolder(@NonNull View itemView) {
            super(itemView);
            container = itemView.findViewById(R.id.eventContainer);
            tvTitle = itemView.findViewById(R.id.tvEventTitle);
            tvTime = itemView.findViewById(R.id.tvEventTime);
            tvAssignedTo = itemView.findViewById(R.id.tvAssignedTo);
            btnDelete = itemView.findViewById(R.id.btnDeleteEvent);
        }
    }
}
