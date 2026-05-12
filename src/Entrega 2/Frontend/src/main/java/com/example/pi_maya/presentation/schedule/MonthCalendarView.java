package com.example.pi_maya.presentation.schedule;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.example.pi_maya.R;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * Calendário mensal custom em estilo brasileiro (semana começa no domingo).
 *
 *  - Cabeçalho com mês/ano + botões prev/next
 *  - Linha de iniciais dos dias (D S T Q Q S S)
 *  - Grid 6×7 de células com número do dia
 *  - Dias com eventos recebem um pontinho coral abaixo
 *  - Hoje fica com borda ciano
 *  - Dia selecionado fica com fundo petróleo + texto branco
 */
public class MonthCalendarView extends LinearLayout {

    public interface OnDateSelectedListener {
        void onDateSelected(LocalDate date);
    }

    private static final Locale PT_BR = new Locale("pt", "BR");

    private TextView monthYearLabel;
    private GridLayout daysGrid;

    private YearMonth currentMonth = YearMonth.now();
    private LocalDate selectedDate;
    private final Set<LocalDate> daysWithEvents = new HashSet<>();

    private OnDateSelectedListener listener;

    public MonthCalendarView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setOrientation(VERTICAL);
        buildHeader(context);
        buildWeekdayRow(context);
        buildDaysGrid(context);
        rebuild();
    }

    private void buildHeader(Context ctx) {
        LinearLayout header = new LinearLayout(ctx);
        header.setOrientation(HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setPadding(0, 0, 0, dp(8));

        ImageView prev = new ImageView(ctx);
        prev.setImageResource(R.drawable.ic_back);
        prev.setBackgroundResource(android.R.color.transparent);
        prev.setPadding(dp(12), dp(12), dp(12), dp(12));
        prev.setContentDescription("Mês anterior");
        LayoutParams prevLp = new LayoutParams(dp(48), dp(48));
        prev.setLayoutParams(prevLp);
        prev.setOnClickListener(v -> {
            currentMonth = currentMonth.minusMonths(1);
            rebuild();
        });

        monthYearLabel = new TextView(ctx);
        monthYearLabel.setTextSize(20);
        monthYearLabel.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        monthYearLabel.setTextColor(getResources().getColor(R.color.text_primary, ctx.getTheme()));
        monthYearLabel.setGravity(Gravity.CENTER);
        LayoutParams labelLp = new LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f);
        monthYearLabel.setLayoutParams(labelLp);

        ImageView next = new ImageView(ctx);
        next.setImageResource(R.drawable.ic_back);
        next.setRotation(180f);
        next.setBackgroundResource(android.R.color.transparent);
        next.setPadding(dp(12), dp(12), dp(12), dp(12));
        next.setContentDescription("Próximo mês");
        next.setLayoutParams(new LayoutParams(dp(48), dp(48)));
        next.setOnClickListener(v -> {
            currentMonth = currentMonth.plusMonths(1);
            rebuild();
        });

        header.addView(prev);
        header.addView(monthYearLabel);
        header.addView(next);
        addView(header);
    }

    private void buildWeekdayRow(Context ctx) {
        GridLayout row = new GridLayout(ctx);
        row.setColumnCount(7);
        row.setRowCount(1);
        LayoutParams rowLp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        rowLp.bottomMargin = dp(4);
        row.setLayoutParams(rowLp);

        // Domingo a Sábado
        String[] labels = {"D", "S", "T", "Q", "Q", "S", "S"};
        for (int i = 0; i < 7; i++) {
            TextView t = new TextView(ctx);
            t.setText(labels[i]);
            t.setGravity(Gravity.CENTER);
            t.setTextSize(13);
            t.setTextColor(getResources().getColor(R.color.text_secondary, ctx.getTheme()));
            GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
            lp.width = 0;
            lp.height = dp(28);
            lp.columnSpec = GridLayout.spec(i, 1, 1f);
            lp.rowSpec = GridLayout.spec(0);
            t.setLayoutParams(lp);
            row.addView(t);
        }
        addView(row);
    }

    private void buildDaysGrid(Context ctx) {
        daysGrid = new GridLayout(ctx);
        daysGrid.setColumnCount(7);
        daysGrid.setRowCount(6);
        daysGrid.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        addView(daysGrid);
    }

    public void setEventDates(Set<LocalDate> dates) {
        daysWithEvents.clear();
        if (dates != null) daysWithEvents.addAll(dates);
        rebuild();
    }

    public void setOnDateSelectedListener(OnDateSelectedListener l) {
        this.listener = l;
    }

    public LocalDate getSelectedDate() {
        return selectedDate;
    }

    public void setSelectedDate(LocalDate date) {
        this.selectedDate = date;
        if (date != null) {
            currentMonth = YearMonth.from(date);
        }
        rebuild();
    }

    private void rebuild() {
        if (daysGrid == null) return;

        monthYearLabel.setText(formatMonthYear(currentMonth));

        daysGrid.removeAllViews();
        LocalDate firstOfMonth = currentMonth.atDay(1);
        // getDayOfWeek: MONDAY=1 ... SUNDAY=7. Queremos Domingo no índice 0.
        int firstDayCol = firstOfMonth.getDayOfWeek().getValue() % 7;
        int daysInMonth = currentMonth.lengthOfMonth();

        // Total de células = 6×7 = 42
        for (int i = 0; i < 42; i++) {
            int row = i / 7;
            int col = i % 7;

            View cell;
            if (i < firstDayCol || i >= firstDayCol + daysInMonth) {
                cell = new View(getContext());
            } else {
                int dayNumber = i - firstDayCol + 1;
                LocalDate date = currentMonth.atDay(dayNumber);
                cell = createDayCell(date);
            }

            GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
            lp.width = 0;
            lp.height = dp(48);
            lp.columnSpec = GridLayout.spec(col, 1, 1f);
            lp.rowSpec = GridLayout.spec(row, 1, 1f);
            cell.setLayoutParams(lp);
            daysGrid.addView(cell);
        }
    }

    private View createDayCell(LocalDate date) {
        boolean isToday = date.equals(LocalDate.now());
        boolean isSelected = date.equals(selectedDate);
        boolean hasEvent = daysWithEvents.contains(date);

        FrameLayoutCompat container = new FrameLayoutCompat(getContext());
        container.setOnClickListener(v -> {
            selectedDate = date;
            rebuild();
            if (listener != null) listener.onDateSelected(date);
        });

        TextView dayText = new TextView(getContext());
        dayText.setText(String.valueOf(date.getDayOfMonth()));
        dayText.setGravity(Gravity.CENTER);
        dayText.setTextSize(15);
        dayText.setTypeface(Typeface.create("sans-serif", Typeface.NORMAL));

        // Background do dia
        if (isSelected) {
            dayText.setBackgroundResource(R.drawable.bg_calendar_day_selected);
            dayText.setTextColor(getResources().getColor(R.color.white, getContext().getTheme()));
            dayText.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        } else if (isToday) {
            dayText.setBackgroundResource(R.drawable.bg_calendar_day_today);
            dayText.setTextColor(getResources().getColor(R.color.petroleo, getContext().getTheme()));
            dayText.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        } else {
            dayText.setBackgroundResource(android.R.color.transparent);
            dayText.setTextColor(getResources().getColor(R.color.text_primary, getContext().getTheme()));
        }

        FrameLayoutCompat.LayoutParams dayLp = new FrameLayoutCompat.LayoutParams(
                dp(36), dp(36));
        dayLp.gravity = Gravity.CENTER;
        dayText.setLayoutParams(dayLp);
        container.addView(dayText);

        // Dot abaixo do número se tiver evento
        if (hasEvent) {
            View dot = new View(getContext());
            dot.setBackgroundResource(R.drawable.bg_calendar_event_dot);
            FrameLayoutCompat.LayoutParams dotLp = new FrameLayoutCompat.LayoutParams(dp(6), dp(6));
            dotLp.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
            dotLp.bottomMargin = dp(2);
            dot.setLayoutParams(dotLp);
            container.addView(dot);
        }

        return container;
    }

    private static String formatMonthYear(YearMonth m) {
        String monthName = m.getMonth().getDisplayName(TextStyle.FULL, PT_BR);
        return Character.toUpperCase(monthName.charAt(0))
                + monthName.substring(1) + " de " + m.getYear();
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }

    /** Wrapper interno para evitar import do FrameLayout direto e manter um único arquivo. */
    private static class FrameLayoutCompat extends android.widget.FrameLayout {
        FrameLayoutCompat(Context context) {
            super(context);
        }
    }
}
