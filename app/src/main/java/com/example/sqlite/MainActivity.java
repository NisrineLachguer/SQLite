package com.example.sqlite;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.Manifest;

import android.view.View;
import android.widget.Adapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.sqlite.R;
import com.example.sqlite.Adapter.EtudiantAdapter;
import com.example.sqlite.classes.Etudiant;
import com.example.sqlite.Service.EtudiantService;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    public static final int PICK_IMAGE_REQUEST = 1;
    public static final int REQUEST_IMAGE_CAPTURE = 2;

    private EditText editTextNom, editTextPrenom, editTextDateNaissance, editTextId;
    private Button buttonValider, buttonChercher, buttonShowList, buttonChoosePhoto;
    private ImageView ivPhoto;
    private TextView textViewResultat;
    private RecyclerView recyclerView;
    private EtudiantAdapter adapter;
    private EtudiantService etudiantService;
    private byte[] selectedPhoto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Ajouter un try-catch global
        try {
            setContentView(R.layout.activity_main);

            // Initialisation plus robuste
            if (!initializeApp()) {
                finish(); // Quitter si l'initialisation échoue
                return;
            }
        } catch (Exception e) {
            Log.e("MainActivity", "Critical initialization error", e);
            Toast.makeText(this, "Erreur critique lors du démarrage", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private boolean initializeApp() {
        try {
            // 1. Vérifier les permissions
            checkPermissions();

            // 2. Initialiser le service
            etudiantService = new EtudiantService(this);
            if (etudiantService == null) {
                throw new RuntimeException("Failed to initialize EtudiantService");
            }

            // 3. Initialiser les vues
            if (!initViews()) {
                throw new RuntimeException("View initialization failed");
            }

            // 4. Configurer le RecyclerView
            setupRecyclerView();

            // 5. Configurer les listeners
            setupListeners();

            return true;
        } catch (Exception e) {
            Log.e("MainActivity", "App initialization failed", e);
            Toast.makeText(this, "Échec de l'initialisation de l'application", Toast.LENGTH_LONG).show();
            return false;
        }
    }

    private void checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    private boolean initViews() {
        try {
            editTextNom = findViewById(R.id.nom);
            editTextPrenom = findViewById(R.id.prenom);
            editTextDateNaissance = findViewById(R.id.date_naissance);
            buttonValider = findViewById(R.id.bn);
            editTextId = findViewById(R.id.id);
            buttonChercher = findViewById(R.id.load);
            textViewResultat = findViewById(R.id.res);
            buttonShowList = findViewById(R.id.btn_show_list);
            recyclerView = findViewById(R.id.recycler_view);
            ivPhoto = findViewById(R.id.iv_photo);
            buttonChoosePhoto = findViewById(R.id.btn_choose_photo);

            // Vérification que toutes les vues sont initialisées
            if (editTextNom == null || editTextPrenom == null || recyclerView == null /* etc. */) {
                throw new RuntimeException("One or more views failed to initialize");
            }

            ivPhoto.setImageResource(R.drawable.ic_person);
            return true;
        } catch (Exception e) {
            Log.e("MainActivity", "View initialization error", e);
            return false;
        }
    }
    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        List<Etudiant> etudiants = etudiantService.findAll();
        adapter = new EtudiantAdapter(etudiants, this);
        recyclerView.setAdapter(adapter);
    }

    private void setupListeners() {
        buttonChoosePhoto.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, PICK_IMAGE_REQUEST);
        });

        editTextDateNaissance.setOnClickListener(v -> {
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    this,
                    (view, year1, month1, dayOfMonth) -> {
                        String date = String.format("%04d-%02d-%02d", year1, month1 + 1, dayOfMonth);
                        editTextDateNaissance.setText(date);
                    },
                    year, month, day);
            datePickerDialog.show();
        });

        buttonValider.setOnClickListener(v -> {
            String nom = editTextNom.getText().toString().trim();
            String prenom = editTextPrenom.getText().toString().trim();
            String dateNaissance = editTextDateNaissance.getText().toString().trim();

            if (nom.isEmpty() || prenom.isEmpty() || dateNaissance.isEmpty()) {
                Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                Etudiant etudiant = new Etudiant(nom, prenom, dateNaissance, selectedPhoto);
                etudiantService.create(etudiant);
                clearFields();
                Toast.makeText(this, "Étudiant ajouté avec succès", Toast.LENGTH_SHORT).show();
                refreshList();
            } catch (Exception e) {
                Toast.makeText(this, "Erreur lors de l'ajout: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        });

        buttonChercher.setOnClickListener(v -> {
            String idText = editTextId.getText().toString().trim();

            if (idText.isEmpty()) {
                Toast.makeText(this, "Veuillez entrer un ID", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                int id = Integer.parseInt(idText);
                Etudiant etudiant = etudiantService.findById(id);

                if (etudiant != null) {
                    editTextNom.setText(etudiant.getNom());
                    editTextPrenom.setText(etudiant.getPrenom());
                    editTextDateNaissance.setText(etudiant.getDateNaissance());

                    if (etudiant.getPhoto() != null) {
                        Bitmap bitmap = BitmapFactory.decodeByteArray(etudiant.getPhoto(), 0, etudiant.getPhoto().length);
                        ivPhoto.setImageBitmap(bitmap);
                        selectedPhoto = etudiant.getPhoto();
                    } else {
                        ivPhoto.setImageResource(R.drawable.ic_person);
                        selectedPhoto = null;
                    }

                    textViewResultat.setText("Étudiant trouvé - ID: " + etudiant.getId());
                } else {
                    textViewResultat.setText("Aucun étudiant trouvé avec cet ID");
                    clearFields();
                }
            } catch (NumberFormatException e) {
                Toast.makeText(this, "ID invalide - veuillez entrer un nombre", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(this, "Erreur lors de la recherche: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        });

        buttonShowList.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ListeEtudiantsActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == PICK_IMAGE_REQUEST && data != null && data.getData() != null) {
                Uri uri = data.getData();
                try {
                    // Utilisation de Glide pour charger une image
                    Glide.with(this)
                            .load(uri) // Uri de l'image
                            .override(100, 100) // Redimensionner l'image
                            .centerCrop() // Centrer et recadrer l'image
                            .placeholder(R.drawable.ic_person) // Image de remplacement pendant le chargement
                            .into(ivPhoto); // ImageView où afficher l'image

                    // Convertir en tableau d'octets pour le stockage
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                    selectedPhoto = stream.toByteArray();
                } catch (IOException e) {
                    Toast.makeText(this, "Erreur lors du chargement de l'image", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }
        }
    }

    private void refreshList() {
        try {
            List<Etudiant> etudiants = etudiantService.findAll();
            adapter = new EtudiantAdapter(etudiants, this);
            recyclerView.setAdapter(adapter);
            adapter.notifyDataSetChanged();
        } catch (Exception e) {
            Toast.makeText(this, "Erreur lors du chargement de la liste", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void clearFields() {
        editTextNom.setText("");
        editTextPrenom.setText("");
        editTextDateNaissance.setText("");
        editTextId.setText("");
        ivPhoto.setImageResource(R.drawable.ic_person);
        selectedPhoto = null;
        textViewResultat.setText("");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (etudiantService != null) {
            etudiantService = null;
        }
    }
}