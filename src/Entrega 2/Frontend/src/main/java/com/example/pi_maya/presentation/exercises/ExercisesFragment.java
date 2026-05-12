package com.example.pi_maya.presentation.exercises;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.pi_maya.MayaApp;
import com.example.pi_maya.R;
import com.example.pi_maya.presentation.common.ExerciseCompactAdapter;

public class ExercisesFragment extends Fragment {

    public static final String EXTRA_ASSIGNMENT_ID = "extra_assignment_id";
    public static final String EXTRA_EXERCISE_TITLE = "extra_exercise_title";

    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView recycler;
    private TextView emptyText;
    private ExerciseCompactAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_exercises, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        swipeRefresh = view.findViewById(R.id.swipeRefresh);
        recycler = view.findViewById(R.id.exercisesRecycler);
        emptyText = view.findViewById(R.id.emptyText);

        adapter = new ExerciseCompactAdapter(assignment -> {
            Intent intent = new Intent(requireContext(), ExerciseDetailActivity.class);
            intent.putExtra(EXTRA_ASSIGNMENT_ID, assignment.id);
            if (assignment.exercise != null) {
                intent.putExtra(EXTRA_EXERCISE_TITLE, assignment.exercise.title);
            }
            startActivity(intent);
        });
        recycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        recycler.setAdapter(adapter);

        swipeRefresh.setOnRefreshListener(this::loadData);
        loadData();
    }

    private void loadData() {
        swipeRefresh.setRefreshing(true);
        MayaApp.get().getExerciseRepository().getMyAssignments()
                .observe(getViewLifecycleOwner(), resource -> {
                    swipeRefresh.setRefreshing(false);
                    if (resource.isSuccess()) {
                        if (resource.getData() != null && !resource.getData().isEmpty()) {
                            adapter.submit(resource.getData());
                            recycler.setVisibility(View.VISIBLE);
                            emptyText.setVisibility(View.GONE);
                        } else {
                            recycler.setVisibility(View.GONE);
                            emptyText.setVisibility(View.VISIBLE);
                        }
                    } else if (resource.isError()) {
                        recycler.setVisibility(View.GONE);
                        emptyText.setVisibility(View.VISIBLE);
                        emptyText.setText(resource.getMessage());
                    }
                });
    }
}
