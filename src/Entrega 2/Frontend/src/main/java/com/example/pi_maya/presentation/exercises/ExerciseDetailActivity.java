package com.example.pi_maya.presentation.exercises;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.pi_maya.MayaApp;
import com.example.pi_maya.R;
import com.example.pi_maya.domain.model.ExerciseAssignment;
import com.google.android.material.button.MaterialButton;

/**
 * Tela de detalhe do exercício. Mostra instruções, observações da fisio
 * e botão para iniciar com câmera + MediaPipe.
 */
public class ExerciseDetailActivity extends AppCompatActivity {

    private String exerciseTitle;
    private String assignmentId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercise_detail);

        assignmentId = getIntent().getStringExtra(ExercisesFragment.EXTRA_ASSIGNMENT_ID);
        exerciseTitle = getIntent().getStringExtra(ExercisesFragment.EXTRA_EXERCISE_TITLE);

        TextView titleView = findViewById(R.id.exerciseTitle);
        TextView subtitleView = findViewById(R.id.exerciseSubtitle);
        TextView instructionsView = findViewById(R.id.instructionsText);
        TextView notesLabel = findViewById(R.id.notesLabel);
        TextView notesView = findViewById(R.id.notesText);

        titleView.setText(exerciseTitle != null ? exerciseTitle : "Exercício");
        instructionsView.setText("Carregando instruções...");

        findViewById(R.id.backButton).setOnClickListener(v -> finish());

        // Carrega o assignment completo para mostrar instruções e observações
        if (assignmentId != null) {
            MayaApp.get().getExerciseRepository().getMyAssignments()
                    .observe(this, resource -> {
                        if (!resource.isSuccess() || resource.getData() == null) return;
                        for (ExerciseAssignment a : resource.getData()) {
                            if (assignmentId.equals(a.id) && a.exercise != null) {
                                titleView.setText(a.exercise.title);
                                exerciseTitle = a.exercise.title;

                                // --- LINHAS ADICIONADAS PARA A IMAGEM ---
                                android.widget.ImageView coverView = findViewById(R.id.cover);
                                String imageName = a.exercise.thumbnailUrl;
                                if (imageName != null) {
                                    int resId = getResources().getIdentifier(imageName, "drawable", getPackageName());
                                    coverView.setImageResource(resId != 0 ? resId : R.drawable.bg_thumbnail_placeholder);
                                }
                                // ---------------------------------------

                                StringBuilder subtitle = new StringBuilder();
                                if (a.targetSets != null) subtitle.append(a.targetSets).append(" séries");
                                if (a.targetRepetitions != null) {
                                    if (subtitle.length() > 0) subtitle.append(" · ");
                                    subtitle.append(a.targetRepetitions).append(" repetições");
                                }
                                if (a.frequencyPerWeek != null) {
                                    if (subtitle.length() > 0) subtitle.append(" · ");
                                    subtitle.append(a.frequencyPerWeek).append("x por semana");
                                }
                                subtitleView.setText(subtitle.toString());

                                instructionsView.setText(a.exercise.instructions != null
                                        ? a.exercise.instructions
                                        : a.exercise.description != null
                                            ? a.exercise.description
                                            : "Sem instruções disponíveis.");

                                if (a.notes != null && !a.notes.isEmpty()) {
                                    notesLabel.setVisibility(android.view.View.VISIBLE);
                                    notesView.setVisibility(android.view.View.VISIBLE);
                                    notesView.setText(a.notes);
                                }
                                break;
                            }
                        }
                    });
        }

        MaterialButton startButton = findViewById(R.id.startButton);
        startButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, ExerciseCameraActivity.class);
            intent.putExtra(ExerciseCameraActivity.EXTRA_EXERCISE_TITLE, exerciseTitle);
            startActivity(intent);
        });
    }
}
