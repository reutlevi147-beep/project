package com.mycasa.app;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Arrays;
import java.util.List;

public class FlowItemAdapter
        extends RecyclerView.Adapter<FlowItemAdapter.VH> {

    // ===== Listener לעדכון סכומים =====
    public interface OnItemChangedListener {
        void onItemChanged();
    }

    private OnItemChangedListener listener;

    // הגדרת מאזין לעדכון שינויים בפריטים פיננסיים
    public void setOnItemChangedListener(OnItemChangedListener listener) {
        this.listener = listener;
    }

    // ===== Data =====
    private final List<FlowItem> items;

    private final List<String> frequencies =
            Arrays.asList("חודשי", "דו-חודשי", "שנתי");

    public FlowItemAdapter(List<FlowItem> items) {
        this.items = items;
    }

    // יצירת ViewHolder עבור פריט פיננסי ברשימה
    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_flow_item, parent, false);
        return new VH(v);
    }

    // הצגת נתוני הפריט וניהול מצב עריכה ושמירה
    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {

        FlowItem item = items.get(position);

        // ===== שם =====
        holder.tvTitle.setText(item.getTitle());

        // ===== Switch – אינדיקציה בלבד =====
        holder.switchEnabled.setOnCheckedChangeListener(null);
        holder.switchEnabled.setClickable(false);
        holder.switchEnabled.setFocusable(false);
        holder.switchEnabled.setChecked(item.isConfigured());
        holder.switchEnabled.setEnabled(!item.isEditing());

        // ===== מצב עריכה / תצוגה =====
        if (item.isEditing()) {

            holder.displayContainer.setVisibility(View.GONE);
            holder.editContainer.setVisibility(View.VISIBLE);

            holder.etAmount.setText(
                    item.getAmount() > 0
                            ? String.valueOf(item.getAmount())
                            : ""
            );

            // Spinner תדירות
            if (holder.spFrequency.getAdapter() == null) {
                ArrayAdapter<String> adapter =
                        new ArrayAdapter<>(
                                holder.itemView.getContext(),
                                android.R.layout.simple_spinner_item,
                                frequencies
                        );
                adapter.setDropDownViewResource(
                        android.R.layout.simple_spinner_dropdown_item
                );
                holder.spFrequency.setAdapter(adapter);
            }

            int index = frequencies.indexOf(item.getFrequency());
            if (index >= 0) {
                holder.spFrequency.setSelection(index, false);
            }

            holder.spFrequency.setOnItemSelectedListener(
                    new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(
                                AdapterView<?> parent,
                                View view,
                                int pos,
                                long id) {

                            item.setFrequency(frequencies.get(pos));
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {}
                    }
            );

        } else {

            holder.editContainer.setVisibility(View.GONE);
            holder.displayContainer.setVisibility(View.VISIBLE);

            holder.tvStatus.setText(
                    item.getAmount() > 0
                            ? "₪" + item.getAmount()
                            : "לא הוגדר"
            );

            holder.tvFrequency.setText(
                    item.getFrequency() != null
                            ? item.getFrequency()
                            : "חודשי"
            );
        }

        // ===== עריכה =====
        holder.btnEdit.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (pos == RecyclerView.NO_POSITION) return;

            items.get(pos).setEditing(true);
            notifyItemChanged(pos);
        });

        // ===== שמירה =====
        holder.btnSave.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (pos == RecyclerView.NO_POSITION) return;

            FlowItem current = items.get(pos);

            String val = holder.etAmount.getText().toString();
            int amount = val.isEmpty() ? 0 : Integer.parseInt(val);

            current.setAmount(amount);
            current.setEditing(false);

            FinanceRepository.saveOrUpdateFlowItem(
                    AppSession.getGroupId(),
                    current
            );

            notifyItemChanged(pos);

            if (listener != null) {
                listener.onItemChanged();
            }
        });

    }

    // החזרת כמות הפריטים ברשימה
    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    // =========================
    // ViewHolder
    // =========================
    static class VH extends RecyclerView.ViewHolder {

        TextView tvTitle, tvStatus, tvFrequency;
        Switch switchEnabled;
        ImageView btnEdit, iconMoney;
        View displayContainer, editContainer;
        EditText etAmount;
        Spinner spFrequency;
        Button btnSave;

        VH(@NonNull View v) {
            super(v);

            tvTitle = v.findViewById(R.id.tvTitle);
            tvStatus = v.findViewById(R.id.tvStatus);
            tvFrequency = v.findViewById(R.id.tvFrequency);

            switchEnabled = v.findViewById(R.id.switchEnabled);

            btnEdit = v.findViewById(R.id.btnEdit);
            iconMoney = v.findViewById(R.id.iconMoney);

            displayContainer = v.findViewById(R.id.displayContainer);
            editContainer = v.findViewById(R.id.editContainer);

            etAmount = v.findViewById(R.id.etAmount);
            spFrequency = v.findViewById(R.id.spFrequency);
            btnSave = v.findViewById(R.id.btnSave);
        }
    }
}
