package com.example.pi_maya.presentation.common;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pi_maya.R;
import com.example.pi_maya.domain.model.Exercise;
import com.example.pi_maya.domain.model.ExerciseAssignment;

import java.util.ArrayList;
import java.util.List;

public class ExerciseCompactAdapter extends RecyclerView.Adapter<ExerciseCompactAdapter.VH> {

    public interface OnExerciseClick {
        void onClick(ExerciseAssignment assignment);
    }

    private final List<ExerciseAssignment> items = new ArrayList<>();
    private final OnExerciseClick listener;

    public ExerciseCompactAdapter(OnExerciseClick listener) {
        this.listener = listener;
    }

    public void submit(List<ExerciseAssignment> data) {
        items.clear();
        if (data != null) items.addAll(data);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_exercise_compact, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        ExerciseAssignment a = items.get(position);
        Exercise e = a.exercise;
        h.title.setText(e != null ? e.title : "Exercício");
        StringBuilder subtitle = new StringBuilder();
        if (a.targetSets != null) subtitle.append(a.targetSets).append(" séries");
        if (a.targetRepetitions != null) {
            if (subtitle.length() > 0) subtitle.append(" · ");
            subtitle.append(a.targetRepetitions).append(" repetições");
        }
        if (subtitle.length() == 0 && e != null && e.durationSeconds != null) {
            subtitle.append(e.durationSeconds / 60).append(" min");
        }
        h.subtitle.setText(subtitle.toString());
        h.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onClick(a);
        });
    }

    @Override public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        final TextView title;
        final TextView subtitle;
        VH(View v) {
            super(v);
            title = v.findViewById(R.id.exerciseTitle);
            subtitle = v.findViewById(R.id.exerciseSubtitle);
        }
    }
}
