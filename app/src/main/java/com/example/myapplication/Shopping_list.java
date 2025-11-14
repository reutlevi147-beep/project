import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.Add_Shopping;
import com.example.myapplication.R;
import com.example.myapplication.ShoppingAdapter;
import com.example.myapplication.ShoppingItem;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class Shopping_list extends AppCompatActivity {

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    RecyclerView recyclerView;
    ShoppingAdapter adapter;
    List<ShoppingItem> items = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_shopping_list);

        // ממשק
        recyclerView = findViewById(R.id.shoppingRecycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new ShoppingAdapter(items);
        recyclerView.setAdapter(adapter);

        // מאזין בזמן אמת לכל שינוי!
        loadItems();

        // כפתור פלוס
        ImageButton plos = findViewById(R.id.Plos);
        plos.setOnClickListener(v -> {
            Intent intent = new Intent(Shopping_list.this, Add_Shopping.class);
            startActivity(intent);
        });
    }

    private void loadItems() {
        db.collection("shopping_list")
                .orderBy("createdAt", Query.Direction.ASCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null || value == null) return;

                    items.clear();
                    for (var doc : value.getDocuments()) {
                        items.add(new ShoppingItem(
                                doc.getId(),
                                doc.getString("name")
                        ));
                    }
                    adapter.notifyDataSetChanged();
                });
    }
}
