package com.example.pi_maya.presentation.onboarding;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.pi_maya.MayaApp;
import com.example.pi_maya.R;
import com.example.pi_maya.presentation.auth.LoginActivity;
import com.google.android.material.button.MaterialButton;

import java.util.Arrays;
import java.util.List;

public class OnboardingActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private LinearLayout dotsIndicator;
    private MaterialButton continueButton;

    private final List<Page> pages = Arrays.asList(
            new Page(R.string.onboarding_welcome_title, R.string.onboarding_welcome_body, R.drawable.ic_home),
            new Page(R.string.onboarding_exercises_title, R.string.onboarding_exercises_body, R.drawable.ic_exercise),
            new Page(R.string.onboarding_privacy_title, R.string.onboarding_privacy_body, R.drawable.ic_lock)
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        viewPager = findViewById(R.id.viewPager);
        dotsIndicator = findViewById(R.id.dotsIndicator);
        continueButton = findViewById(R.id.continueButton);
        MaterialButton skipButton = findViewById(R.id.skipButton);

        viewPager.setAdapter(new OnboardingAdapter(pages));
        renderDots(0);

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                renderDots(position);
                continueButton.setText(position == pages.size() - 1
                        ? R.string.action_start
                        : R.string.action_continue);
            }
        });

        continueButton.setOnClickListener(v -> {
            int next = viewPager.getCurrentItem() + 1;
            if (next < pages.size()) {
                viewPager.setCurrentItem(next, true);
            } else {
                finishOnboarding();
            }
        });

        skipButton.setOnClickListener(v -> finishOnboarding());
    }

    private void finishOnboarding() {
        MayaApp.get().getSessionManager().setOnboardingDone();
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    private void renderDots(int activeIndex) {
        dotsIndicator.removeAllViews();
        int dotSize = (int) (10 * getResources().getDisplayMetrics().density);
        int margin = (int) (4 * getResources().getDisplayMetrics().density);
        for (int i = 0; i < pages.size(); i++) {
            View dot = new View(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dotSize, dotSize);
            params.setMargins(margin, 0, margin, 0);
            dot.setLayoutParams(params);
            dot.setBackgroundResource(R.drawable.bg_circle_avatar);
            dot.setBackgroundTintList(getColorStateList(
                    i == activeIndex ? R.color.primary_pastel_dark : R.color.outline));
            dotsIndicator.addView(dot);
        }
    }

    static class Page {
        final int titleRes;
        final int bodyRes;
        final int iconRes;
        Page(int titleRes, int bodyRes, int iconRes) {
            this.titleRes = titleRes;
            this.bodyRes = bodyRes;
            this.iconRes = iconRes;
        }
    }

    static class OnboardingAdapter extends RecyclerView.Adapter<OnboardingAdapter.PageVH> {
        private final List<Page> pages;
        OnboardingAdapter(List<Page> pages) { this.pages = pages; }

        @NonNull @Override
        public PageVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_onboarding_page, parent, false);
            return new PageVH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull PageVH h, int position) {
            Page p = pages.get(position);
            h.title.setText(p.titleRes);
            h.body.setText(p.bodyRes);
            h.icon.setImageResource(p.iconRes);
        }

        @Override public int getItemCount() { return pages.size(); }

        static class PageVH extends RecyclerView.ViewHolder {
            final TextView title;
            final TextView body;
            final ImageView icon;
            PageVH(View v) {
                super(v);
                title = v.findViewById(R.id.title);
                body = v.findViewById(R.id.body);
                icon = v.findViewById(R.id.illustrationIcon);
            }
        }
    }
}
