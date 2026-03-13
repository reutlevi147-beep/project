package com.mycasa.app;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;
import android.net.Uri;
import android.content.Intent;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import android.provider.MediaStore;
import android.graphics.Bitmap;
import java.io.ByteArrayOutputStream;
import android.graphics.Bitmap;
import android.Manifest;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class EditUserActivity extends AppCompatActivity {

    private TextInputEditText etName, etPhone, etEmail;
    private MaterialButton btnSave;

    private MaterialCardView cardChild, cardParent;
    private TextView tvChild, tvParent;
    private ImageView iconChildCheck, iconParentCheck;

    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private StorageReference storageRef;

    private ImageView imgProfile;
    private static final int REQUEST_GALLERY = 100;
    private static final int REQUEST_CAMERA = 200;
    private Uri imageUri;
    private String userId;
    private String groupId;

    private String selectedRole = "child";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_user);

        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();
        // ===== Views =====
        etName = findViewById(R.id.etName);
        etPhone = findViewById(R.id.etPhone);
        etEmail = findViewById(R.id.etEmail);
        btnSave = findViewById(R.id.btnSave);

        cardChild = findViewById(R.id.cardChild);
        cardParent = findViewById(R.id.cardParent);

        tvChild = findViewById(R.id.tvChild);
        tvParent = findViewById(R.id.tvParent);

        iconChildCheck = findViewById(R.id.iconChildCheck);
        iconParentCheck = findViewById(R.id.iconParentCheck);
        imgProfile = findViewById(R.id.imgProfile);
        imgProfile.setOnClickListener(v -> showImagePickerBottomSheet());        userId = getIntent().getStringExtra("USER_ID");
        groupId = getIntent().getStringExtra("GROUP_ID");

        if (userId == null || groupId == null) {
            Toast.makeText(this, "נתוני משתמש חסרים", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // ===== לחיצות =====
        cardChild.setOnClickListener(v -> selectRole("child"));
        cardParent.setOnClickListener(v -> selectRole("parent"));

        btnSave.setOnClickListener(v -> saveUserData());

        // ברירת מחדל
        selectRole("child");

        loadUserData();
    }

    // ==========================
    // בחירת תפקיד
    // ==========================
    private void selectRole(String role) {

        selectedRole = role;

        if ("child".equals(role)) {

            // CHILD SELECTED
            cardChild.setCardBackgroundColor(getColor(R.color.gray_dark));
            cardChild.setStrokeWidth(3);
            cardChild.setStrokeColor(getColor(R.color.gray_dark));
            tvChild.setTextColor(getColor(android.R.color.white));
            iconChildCheck.setVisibility(View.VISIBLE);

            cardParent.setCardBackgroundColor(getColor(android.R.color.white));
            cardParent.setStrokeWidth(2);
            cardParent.setStrokeColor(getColor(R.color.gray_light));
            tvParent.setTextColor(getColor(R.color.gray_dark));
            iconParentCheck.setVisibility(View.GONE);

        } else {

            // PARENT SELECTED
            cardParent.setCardBackgroundColor(getColor(R.color.gray_dark));
            cardParent.setStrokeWidth(3);
            cardParent.setStrokeColor(getColor(R.color.gray_dark));
            tvParent.setTextColor(getColor(android.R.color.white));
            iconParentCheck.setVisibility(View.VISIBLE);

            cardChild.setCardBackgroundColor(getColor(android.R.color.white));
            cardChild.setStrokeWidth(2);
            cardChild.setStrokeColor(getColor(R.color.gray_light));
            tvChild.setTextColor(getColor(R.color.gray_dark));
            iconChildCheck.setVisibility(View.GONE);
        }
    }
    private void openGallery() {

        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, 100);
    }

    private void showImagePickerBottomSheet() {

        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.bottom_sheet_image_picker, null);

        dialog.setContentView(view);

        View optionCamera = view.findViewById(R.id.optionCamera);
        View optionGallery = view.findViewById(R.id.optionGallery);

        optionCamera.setOnClickListener(v -> {
            openCamera();
            dialog.dismiss();
        });

        optionGallery.setOnClickListener(v -> {
            openGallery();
            dialog.dismiss();
        });

        dialog.show();
    }


    private void openCamera() {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA}, 1);

            return;
        }

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, REQUEST_CAMERA);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null) {

            if (requestCode == REQUEST_GALLERY) {

                imageUri = data.getData();
                imgProfile.setImageURI(imageUri);
                uploadImage();

            } else if (requestCode == REQUEST_CAMERA && data != null && data.getExtras() != null) {

                Bitmap photo = (Bitmap) data.getExtras().get("data");

                if (photo != null) {
                    imgProfile.setImageBitmap(photo);
                    uploadCameraImage(photo);
                }
            }
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(this, "נדרשת הרשאת מצלמה", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void uploadImage() {

        if (imageUri == null) return;

        StorageReference ref =
                storageRef.child("users/" + userId + "/profile.jpg");

        ref.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {

                    ref.getDownloadUrl().addOnSuccessListener(uri -> {

                        String imageUrl = uri.toString();

                        db.collection("groups")
                                .document(groupId)
                                .collection("users")
                                .document(userId)
                                .update("imageUrl", imageUrl);
                    });
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "שגיאה בהעלאת תמונה", Toast.LENGTH_SHORT).show()
                );
    }

    private void uploadCameraImage(Bitmap bitmap) {

        StorageReference ref =
                storageRef.child("users/" + userId + "/profile.jpg");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, baos);
        byte[] data = baos.toByteArray();

        ref.putBytes(data)
                .addOnSuccessListener(taskSnapshot -> {

                    ref.getDownloadUrl().addOnSuccessListener(uri -> {

                        String imageUrl = uri.toString();

                        db.collection("groups")
                                .document(groupId)
                                .collection("users")
                                .document(userId)
                                .update("imageUrl", imageUrl);
                    });

                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "שגיאה בהעלאת תמונה", Toast.LENGTH_SHORT).show()
                );
    }


    // ==========================
    // טעינת נתונים
    // ==========================
    private void loadUserData() {

        btnSave.setEnabled(false);

        db.collection("groups")
                .document(groupId)
                .collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(snapshot -> {

                    btnSave.setEnabled(true);

                    if (snapshot.exists()) {

                        String name = snapshot.getString("name");
                        String phone = snapshot.getString("phone");
                        String email = snapshot.getString("email");
                        String role = snapshot.getString("role");
                        String imageUrl = snapshot.getString("imageUrl");

                        if (imageUrl != null && !imageUrl.isEmpty()) {

                            Glide.with(this)
                                    .load(imageUrl)
                                    .circleCrop()
                                    .into(imgProfile);
                        }
                        etName.setText(name != null ? name : "");
                        etPhone.setText(phone != null ? phone : "");
                        etEmail.setText(email != null ? email : "");

                        if ("parent".equals(role)) {
                            selectRole("parent");
                        } else {
                            selectRole("child");
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    btnSave.setEnabled(true);
                    Toast.makeText(this, "שגיאה בטעינה", Toast.LENGTH_SHORT).show();
                });
    }

    // ==========================
    // שמירה
    // ==========================
    private void saveUserData() {

        String name = etName.getText() != null ? etName.getText().toString().trim() : "";
        String phone = etPhone.getText() != null ? etPhone.getText().toString().trim() : "";
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";

        if (TextUtils.isEmpty(name)) {
            etName.setError("השם חובה");
            return;
        }

        if (!TextUtils.isEmpty(email) &&
                !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("אימייל לא תקין");
            return;
        }

        btnSave.setEnabled(false);

        Map<String, Object> map = new HashMap<>();
        map.put("name", name);
        map.put("phone", phone);
        map.put("email", email);
        map.put("role", selectedRole);

        db.collection("groups")
                .document(groupId)
                .collection("users")
                .document(userId)
                .update(map)
                .addOnSuccessListener(unused -> {

                    btnSave.setEnabled(true);
                    Toast.makeText(this, "עודכן בהצלחה", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                })
                .addOnFailureListener(e -> {
                    btnSave.setEnabled(true);
                    Toast.makeText(this, "שגיאה בשמירה", Toast.LENGTH_SHORT).show();
                });
    }
}
