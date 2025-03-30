package com.example.sqlite.Adapter;

import static com.example.sqlite.MainActivity.PICK_IMAGE_REQUEST;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.sqlite.R;
import com.example.sqlite.Service.EtudiantService;
import com.example.sqlite.classes.Etudiant;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.List;

public class EtudiantAdapter extends RecyclerView.Adapter<EtudiantAdapter.EtudiantViewHolder> {

    private static final int PICK_IMAGE_REQUEST = 1;
    private List<Etudiant> etudiantList;
    private Context context;
    private EtudiantService etudiantService;
    private byte[] selectedPhoto;
    private ImageView currentDialogImageView;

    public EtudiantAdapter(List<Etudiant> etudiantList, Context context) {
        this.etudiantList = etudiantList;
        this.context = context;
        this.etudiantService = new EtudiantService(context);
    }

    @NonNull
    @Override
    public EtudiantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_etudiant, parent, false);
        return new EtudiantViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EtudiantViewHolder holder, int position) {
        Etudiant etudiant = etudiantList.get(position);

        if (etudiant.getPhoto() != null) {
            Glide.with(context)
                    .load(etudiant.getPhoto())
                    .override(100, 100)
                    .centerCrop()
                    .placeholder(R.drawable.ic_person)
                    .into(holder.ivPhoto);
        } else {
            holder.ivPhoto.setImageResource(R.drawable.ic_person);
        }

        holder.tvId.setText("ID: " + etudiant.getId());
        holder.tvNom.setText(etudiant.getNom());
        holder.tvPrenom.setText(etudiant.getPrenom());
        holder.tvDateNaissance.setText(etudiant.getDateNaissance());

        holder.itemView.setOnClickListener(v -> showOptionsDialog(etudiant, position));
    }

    private void showOptionsDialog(Etudiant etudiant, int position) {
        String[] options = {"Modifier", "Supprimer"};

        new AlertDialog.Builder(context)
                .setTitle("Options pour " + etudiant.getNom() + " " + etudiant.getPrenom())
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        showEditDialog(etudiant, position);
                    } else {
                        showDeleteDialog(etudiant, position);
                    }
                })
                .setNegativeButton("Annuler", null)
                .show();
    }

    private void showEditDialog(Etudiant etudiant, int position) {
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_edit_etudiant, null);

        EditText editNom = dialogView.findViewById(R.id.editNom);
        EditText editPrenom = dialogView.findViewById(R.id.editPrenom);
        EditText editDateNaissance = dialogView.findViewById(R.id.editDateNaissance);
        ImageView ivPhoto = dialogView.findViewById(R.id.iv_photo_dialog);
        Button btnChoosePhoto = dialogView.findViewById(R.id.btn_choose_photo);

        editNom.setText(etudiant.getNom());
        editPrenom.setText(etudiant.getPrenom());
        editDateNaissance.setText(etudiant.getDateNaissance());

        // Afficher la photo actuelle avec Glide
        if (etudiant.getPhoto() != null) {
            Glide.with(context)
                    .load(etudiant.getPhoto())
                    .override(100, 100)
                    .centerCrop()
                    .placeholder(R.drawable.ic_person)
                    .into(ivPhoto);
        } else {
            ivPhoto.setImageResource(R.drawable.ic_person);
        }

        // Sélection de date
        editDateNaissance.setOnClickListener(v -> {
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    context,
                    (view, year1, month1, dayOfMonth) -> {
                        String date = String.format("%04d-%02d-%02d", year1, month1 + 1, dayOfMonth);
                        editDateNaissance.setText(date);
                    },
                    year, month, day);
            datePickerDialog.show();
        });

        btnChoosePhoto.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            ((AppCompatActivity)context).startActivityForResult(intent, PICK_IMAGE_REQUEST);
        });

        AlertDialog dialog = new AlertDialog.Builder(context)
                .setView(dialogView)
                .setTitle("Modifier Étudiant")
                .setPositiveButton("Enregistrer", (dialogInterface, which) -> {
                    etudiant.setNom(editNom.getText().toString());
                    etudiant.setPrenom(editPrenom.getText().toString());
                    etudiant.setDateNaissance(editDateNaissance.getText().toString());

                    if (selectedPhoto != null) {
                        etudiant.setPhoto(selectedPhoto);
                    }

                    etudiantService.update(etudiant);
                    notifyItemChanged(position);
                    Toast.makeText(context, "Étudiant modifié", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Annuler", null)
                .create();

        dialog.setOnShowListener(dialogInterface -> {
            this.currentDialogImageView = ivPhoto;
        });

        dialog.show();
    }

    private void showDeleteDialog(Etudiant etudiant, int position) {
        new AlertDialog.Builder(context)
                .setTitle("Supprimer Étudiant")
                .setMessage("Voulez-vous vraiment supprimer " + etudiant.getNom() + "?")
                .setPositiveButton("Oui", (dialog, which) -> {
                    etudiantService.delete(etudiant);
                    etudiantList.remove(position);
                    notifyItemRemoved(position);
                    Toast.makeText(context, "Étudiant supprimé", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Non", null)
                .show();
    }

    @Override
    public int getItemCount() {
        return etudiantList.size();
    }

    public static class EtudiantViewHolder extends RecyclerView.ViewHolder {
        ImageView ivPhoto;
        TextView tvId, tvNom, tvPrenom, tvDateNaissance;

        public EtudiantViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPhoto = itemView.findViewById(R.id.iv_photo);
            tvId = itemView.findViewById(R.id.tv_id);
            tvNom = itemView.findViewById(R.id.tv_nom);
            tvPrenom = itemView.findViewById(R.id.tv_prenom);
            tvDateNaissance = itemView.findViewById(R.id.tv_date_naissance);
        }
    }

    public void handleImageSelectionResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == AppCompatActivity.RESULT_OK && requestCode == PICK_IMAGE_REQUEST && data != null) {
            Uri uri = data.getData();
            try {
                // Mettre à jour immédiatement l'image dans le dialog avec Glide
                if (currentDialogImageView != null) {
                    Glide.with(context)
                            .load(uri)
                            .override(100, 100)
                            .centerCrop()
                            .placeholder(R.drawable.ic_person)
                            .into(currentDialogImageView);
                }

                // Convertir en tableau d'octets pour le stockage
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), uri);
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                selectedPhoto = stream.toByteArray();
            } catch (IOException e) {
                Toast.makeText(context, "Erreur de chargement", Toast.LENGTH_SHORT).show();
            }
        }
    }
}