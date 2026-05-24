package com.mycasa.app;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PendingApprovalAdapter
        extends RecyclerView.Adapter<PendingApprovalAdapter.VH> {

    // =========================
    // Listener לעדכון ה־Activity
    // =========================
    public interface OnPendingChangedListener {
        void onPendingChanged(int remainingCount);
    }

    private OnPendingChangedListener listener;

    public void setOnPendingChangedListener(OnPendingChangedListener listener) {
        this.listener = listener;
    }

    // =========================
    // Data
    // =========================
    private final List<FlowItem> items;

    public PendingApprovalAdapter(List<FlowItem> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pending_approval, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        FlowItem item = items.get(position);

        boolean isIncome =
                item.getCategoryId() != null &&
                        item.getCategoryId().startsWith("income");

        // ===== שם =====
        h.tvName.setText(
                isIncome
                        ? "💰 " + item.getTitle()
                        : "💸 " + item.getTitle()
        );

        // ===== תדירות =====
        h.tvFrequency.setText(item.getFrequency());

        // ===== סכום עריך =====
        h.etAmount.setText(String.valueOf(item.getAmount()));

        h.btnApprove.setOnClickListener(v -> {

            int pos = h.getAdapterPosition();
            if (pos == RecyclerView.NO_POSITION) return;

            String val = h.etAmount.getText().toString().trim();
            int newAmount = val.isEmpty() ? 0 : Integer.parseInt(val);

            // 1️⃣ עדכון מקומי
            Date approvedDate = new Date();

            item.setAmount(newAmount);
            item.setLastApprovedAt(approvedDate);
            item.setApproved(true);

            FinanceRepository.addApprovalHistory(
                    AppSession.getGroupId(),
                    item,
                    newAmount,
                    approvedDate
            );

            FinanceRepository.saveOrUpdateFlowItem(
                    AppSession.getGroupId(),
                    item
            );

            // 3️⃣ הסרה מהרשימה
            items.remove(pos);
            notifyItemRemoved(pos);

            // 4️⃣ דיווח ל־Activity (כדי להסתיר את הבלוק אם נגמר)
            if (listener != null) {
                listener.onPendingChanged(items.size());
            }
        });
    }



    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    // =========================
    // ViewHolder
    // =========================
    static class VH extends RecyclerView.ViewHolder {

        TextView tvName, tvFrequency;
        EditText etAmount;
        Button btnApprove;

        VH(View v) {
            super(v);
            tvName = v.findViewById(R.id.tvName);
            tvFrequency = v.findViewById(R.id.tvFrequency);
            etAmount = v.findViewById(R.id.etAmount);
            btnApprove = v.findViewById(R.id.btnApprove);
        }
    }
}
