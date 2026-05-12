package com.example.pi_maya.presentation.schedule;

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
import com.example.pi_maya.domain.model.Appointment;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ScheduleFragment extends Fragment {

    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView recycler;
    private TextView emptyText;
    private TextView listHeader;
    private TextView toggleList;
    private TextView toggleCalendar;
    private MonthCalendarView calendarView;

    private AppointmentAdapter adapter;

    private List<Appointment> allAppointments = new ArrayList<>();
    private boolean calendarMode = false;
    private LocalDate selectedDay;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_schedule, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        swipeRefresh = view.findViewById(R.id.swipeRefresh);
        recycler = view.findViewById(R.id.appointmentsRecycler);
        emptyText = view.findViewById(R.id.emptyText);
        listHeader = view.findViewById(R.id.listHeader);
        toggleList = view.findViewById(R.id.toggleList);
        toggleCalendar = view.findViewById(R.id.toggleCalendar);
        calendarView = view.findViewById(R.id.calendarView);

        adapter = new AppointmentAdapter();
        recycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        recycler.setAdapter(adapter);

        // Estado inicial: lista selecionada
        toggleList.setSelected(true);
        toggleCalendar.setSelected(false);

        toggleList.setOnClickListener(v -> setMode(false));
        toggleCalendar.setOnClickListener(v -> setMode(true));

        calendarView.setOnDateSelectedListener(date -> {
            selectedDay = date;
            renderListForSelectedDay();
        });

        swipeRefresh.setOnRefreshListener(this::loadData);
        loadData();
    }

    private void setMode(boolean calendar) {
        if (calendarMode == calendar) return;
        calendarMode = calendar;

        toggleList.setSelected(!calendar);
        toggleCalendar.setSelected(calendar);
        toggleList.setTextColor(getResources().getColor(
                !calendar ? R.color.white : R.color.text_primary, requireContext().getTheme()));
        toggleCalendar.setTextColor(getResources().getColor(
                calendar ? R.color.white : R.color.text_primary, requireContext().getTheme()));

        calendarView.setVisibility(calendar ? View.VISIBLE : View.GONE);

        if (calendar) {
            // Quando entra no modo calendário, seleciona hoje por padrão
            selectedDay = LocalDate.now();
            calendarView.setSelectedDate(selectedDay);
            listHeader.setText(formatHeaderForDay(selectedDay));
            renderListForSelectedDay();
        } else {
            selectedDay = null;
            listHeader.setText(R.string.schedule_upcoming);
            renderUpcoming();
        }
    }

    private void loadData() {
        swipeRefresh.setRefreshing(true);
        MayaApp.get().getAppointmentRepository().getAllMyAppointments()
                .observe(getViewLifecycleOwner(), resource -> {
                    swipeRefresh.setRefreshing(false);
                    if (resource.isSuccess()) {
                        allAppointments = resource.getData() != null ? resource.getData() : new ArrayList<>();

                        Set<LocalDate> daysWithEvents = new HashSet<>();
                        for (Appointment a : allAppointments) {
                            if (a.startsAt == null) continue;
                            daysWithEvents.add(a.startsAt
                                    .atZoneSameInstant(ZoneId.systemDefault())
                                    .toLocalDate());
                        }
                        calendarView.setEventDates(daysWithEvents);

                        if (calendarMode) {
                            renderListForSelectedDay();
                        } else {
                            renderUpcoming();
                        }
                    } else if (resource.isError()) {
                        emptyText.setText(resource.getMessage());
                        emptyText.setVisibility(View.VISIBLE);
                        recycler.setVisibility(View.GONE);
                    }
                });
    }

    private void renderUpcoming() {
        LocalDate today = LocalDate.now();
        List<Appointment> upcoming = new ArrayList<>();
        for (Appointment a : allAppointments) {
            if (a.startsAt == null) continue;
            LocalDate d = a.startsAt.atZoneSameInstant(ZoneId.systemDefault()).toLocalDate();
            if (!d.isBefore(today)) upcoming.add(a);
        }
        showList(upcoming);
    }

    private void renderListForSelectedDay() {
        List<Appointment> ofDay = new ArrayList<>();
        if (selectedDay != null) {
            for (Appointment a : allAppointments) {
                if (a.startsAt == null) continue;
                LocalDate d = a.startsAt.atZoneSameInstant(ZoneId.systemDefault()).toLocalDate();
                if (d.equals(selectedDay)) ofDay.add(a);
            }
            listHeader.setText(formatHeaderForDay(selectedDay));
        }
        showList(ofDay);
    }

    private void showList(List<Appointment> data) {
        if (data == null || data.isEmpty()) {
            recycler.setVisibility(View.GONE);
            emptyText.setVisibility(View.VISIBLE);
            emptyText.setText(calendarMode
                    ? "Nenhuma sessão neste dia."
                    : getString(R.string.schedule_empty));
        } else {
            adapter.submit(data);
            recycler.setVisibility(View.VISIBLE);
            emptyText.setVisibility(View.GONE);
        }
    }

    private String formatHeaderForDay(LocalDate day) {
        if (day == null) return "";
        if (day.equals(LocalDate.now())) return "Hoje";
        if (day.equals(LocalDate.now().plusDays(1))) return "Amanhã";
        String monthName = day.getMonth().getDisplayName(
                java.time.format.TextStyle.FULL, new java.util.Locale("pt", "BR"));
        return day.getDayOfMonth() + " de " + monthName;
    }
}
