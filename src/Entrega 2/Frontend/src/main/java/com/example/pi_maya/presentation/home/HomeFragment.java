package com.example.pi_maya.presentation.home;

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
import com.example.pi_maya.core.session.SessionManager;
import com.example.pi_maya.core.util.DateUtils;
import com.example.pi_maya.domain.model.Appointment;
import com.example.pi_maya.presentation.common.ContentCardAdapter;
import com.example.pi_maya.presentation.common.ExerciseCompactAdapter;
import com.example.pi_maya.presentation.content.ContentDetailActivity;
import com.example.pi_maya.presentation.content.ContentFragment;
import com.example.pi_maya.presentation.exercises.ExerciseDetailActivity;
import com.example.pi_maya.presentation.exercises.ExercisesFragment;
import com.google.android.material.card.MaterialCardView;

public class HomeFragment extends Fragment {

    private TextView greetingText;
    private TextView dateText;
    private MaterialCardView nextSessionCard;
    private TextView nextSessionTime;
    private TextView nextSessionTherapist;
    private TextView noSessionText;

    private RecyclerView exercisesRecycler;
    private TextView noExercisesText;

    private RecyclerView contentRecycler;

    private ExerciseCompactAdapter exerciseAdapter;
    private ContentCardAdapter contentAdapter;

    private SwipeRefreshLayout swipeRefresh;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        swipeRefresh = view.findViewById(R.id.swipeRefresh);
        greetingText = view.findViewById(R.id.greetingText);
        dateText = view.findViewById(R.id.dateText);
        nextSessionCard = view.findViewById(R.id.nextSessionCard);
        nextSessionTime = view.findViewById(R.id.nextSessionTime);
        nextSessionTherapist = view.findViewById(R.id.nextSessionTherapist);
        noSessionText = view.findViewById(R.id.noSessionText);
        exercisesRecycler = view.findViewById(R.id.exercisesRecycler);
        noExercisesText = view.findViewById(R.id.noExercisesText);
        contentRecycler = view.findViewById(R.id.contentRecycler);

        // Mesma navegação da aba "Exercícios": abre o detalhe → câmera + MediaPipe.
        exerciseAdapter = new ExerciseCompactAdapter(assignment -> {
            Intent intent = new Intent(requireContext(), ExerciseDetailActivity.class);
            intent.putExtra(ExercisesFragment.EXTRA_ASSIGNMENT_ID, assignment.id);
            if (assignment.exercise != null) {
                intent.putExtra(ExercisesFragment.EXTRA_EXERCISE_TITLE, assignment.exercise.title);
            }
            startActivity(intent);
        });
        exercisesRecycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        exercisesRecycler.setAdapter(exerciseAdapter);

        contentAdapter = new ContentCardAdapter(content -> {
            Intent intent = new Intent(requireContext(), ContentDetailActivity.class);
            intent.putExtra(ContentFragment.EXTRA_CONTENT_ID, content.id);
            intent.putExtra(ContentFragment.EXTRA_CONTENT_TITLE, content.title);
            intent.putExtra(ContentFragment.EXTRA_CONTENT_BODY, content.body);
            startActivity(intent);
        });
        contentRecycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        contentRecycler.setAdapter(contentAdapter);

        renderGreeting();
        loadData();

        swipeRefresh.setOnRefreshListener(this::loadData);
    }

    private void renderGreeting() {
        SessionManager session = MayaApp.get().getSessionManager();
        String name = session.getUserName();
        if (name == null || name.isEmpty()) {
            greetingText.setText(R.string.home_greeting_generic);
        } else {
            String firstName = name.contains(" ") ? name.substring(0, name.indexOf(' ')) : name;
            greetingText.setText(getString(R.string.home_greeting, firstName));
        }
        dateText.setText(DateUtils.formatTodayLong());
    }

    private void loadData() {
        swipeRefresh.setRefreshing(true);

        // Próxima sessão
        MayaApp.get().getAppointmentRepository().getNextAppointment()
                .observe(getViewLifecycleOwner(), resource -> {
                    if (resource.isSuccess()) {
                        Appointment a = resource.getData();
                        if (a != null) {
                            nextSessionCard.setVisibility(View.VISIBLE);
                            noSessionText.setVisibility(View.GONE);
                            nextSessionTime.setText(DateUtils.formatDateTimeFriendly(a.startsAt));
                            nextSessionTherapist.setText(a.therapistName != null
                                    ? "com " + a.therapistName : "");
                        } else {
                            nextSessionCard.setVisibility(View.GONE);
                            noSessionText.setVisibility(View.VISIBLE);
                        }
                    } else if (resource.isError()) {
                        nextSessionCard.setVisibility(View.GONE);
                        noSessionText.setVisibility(View.VISIBLE);
                        noSessionText.setText(resource.getMessage());
                    }
                    swipeRefresh.setRefreshing(false);
                });

        // Exercícios atribuídos
        MayaApp.get().getExerciseRepository().getMyAssignments()
                .observe(getViewLifecycleOwner(), resource -> {
                    if (resource.isSuccess()) {
                        if (resource.getData() != null && !resource.getData().isEmpty()) {
                            exerciseAdapter.submit(resource.getData());
                            exercisesRecycler.setVisibility(View.VISIBLE);
                            noExercisesText.setVisibility(View.GONE);
                        } else {
                            exercisesRecycler.setVisibility(View.GONE);
                            noExercisesText.setVisibility(View.VISIBLE);
                        }
                    }
                });

        // Conteúdo educativo
        MayaApp.get().getContentRepository().getPublishedContent()
                .observe(getViewLifecycleOwner(), resource -> {
                    if (resource.isSuccess()) {
                        contentAdapter.submit(resource.getData());
                    }
                });
    }
}
