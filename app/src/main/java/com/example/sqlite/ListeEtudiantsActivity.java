package com.example.sqlite;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sqlite.Adapter.EtudiantAdapter;
import com.example.sqlite.Service.EtudiantService;
import com.example.sqlite.classes.Etudiant;

import java.util.List;

public class ListeEtudiantsActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private EtudiantAdapter adapter;
    private EtudiantService etudiantService;
    private Button btnRetour;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_liste_etudiants);

        etudiantService = new EtudiantService(this);
        recyclerView = findViewById(R.id.recycler_view);
        btnRetour = findViewById(R.id.btn_retour);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        refreshList();

        btnRetour.setOnClickListener(v -> {
            finish(); // Retour à l'activité principale
        });
    }

    private void refreshList() {
        List<Etudiant> etudiants = etudiantService.findAll();
        adapter = new EtudiantAdapter(etudiants, this);
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshList();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (adapter != null) {
            adapter.handleImageSelectionResult(requestCode, resultCode, data);
        }
    }
}