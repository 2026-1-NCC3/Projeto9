package com.example.pi_maya.presentation.schedule;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pi_maya.R;
import com.example.pi_maya.domain.model.Appointment;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AppointmentAdapter extends RecyclerView.Adapter<AppointmentAdapter.VH> {

    private static final Locale PT_BR = new Locale("pt", "BR");
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm", PT_BR);

    private final List<Appointment> items = new ArrayList<>();

    public void submit(List<Appointment> data) {
        items.clear();
        if (data != null) items.addAll(data);
        notifyDataSetChanged();
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_appointment, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Appointment a = items.get(position);
        if (a.startsAt == null) return;

        ZoneId zone = ZoneId.systemDefault();
        h.dayNumber.setText(String.valueOf(a.startsAt.atZoneSameInstant(zone).getDayOfMonth()));
        h.monthName.setText(a.startsAt.atZoneSameInstant(zone)
                .getMonth()
                .getDisplayName(TextStyle.SHORT, PT_BR)
                .replace(".", "")
                .toUpperCase(PT_BR));
        h.time.setText(a.startsAt.atZoneSameInstant(zone).toLocalTime().format(TIME));
        h.therapist.setText(a.therapistName != null ? "com " + a.therapistName : "");

        switch (a.status) {
            case CONFIRMED:
                h.status.setText("Confirmado");
                break;
            case SCHEDULED:
                h.status.setText("Aguardando confirmação");
                break;
            case COMPLETED:
                h.status.setText("Concluída");
                break;
            case CANCELLED:
                h.status.setText("Cancelada");
                break;
            case NO_SHOW:
                h.status.setText("Faltou");
                break;
            default:
                h.status.setText("");
        }
    }

    @Override public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        final TextView dayNumber;
        final TextView monthName;
        final TextView time;
        final TextView therapist;
        final TextView status;
        VH(View v) {
            super(v);
            dayNumber = v.findViewById(R.id.dayNumber);
            monthName = v.findViewById(R.id.monthName);
            time = v.findViewById(R.id.timeText);
            therapist = v.findViewById(R.id.therapistText);
            status = v.findViewById(R.id.statusBadge);
        }
    }
}
