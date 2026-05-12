package com.example.pi_maya.presentation.common;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pi_maya.R;
import com.example.pi_maya.domain.model.EducationalContent;

import java.util.ArrayList;
import java.util.List;

public class ContentCardAdapter extends RecyclerView.Adapter<ContentCardAdapter.VH> {

    public interface OnContentClick {
        void onClick(EducationalContent content);
    }

    private final List<EducationalContent> items = new ArrayList<>();
    private final OnContentClick listener;

    public ContentCardAdapter(OnContentClick listener) {
        this.listener = listener;
    }

    public void submit(List<EducationalContent> data) {
        items.clear();
        if (data != null) items.addAll(data);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_content_card, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        EducationalContent c = items.get(position);
        h.title.setText(c.title != null ? c.title : "");
        if (c.body != null) {
            String snippet = c.body.length() > 140 ? c.body.substring(0, 140) + "…" : c.body;
            h.snippet.setText(snippet);
        } else {
            h.snippet.setText("");
        }
        h.category.setText(c.category != null ? c.category : "RPG");
        h.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onClick(c);
        });
    }

    @Override public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        final TextView title;
        final TextView snippet;
        final TextView category;
        VH(View v) {
            super(v);
            title = v.findViewById(R.id.contentTitle);
            snippet = v.findViewById(R.id.contentSnippet);
            category = v.findViewById(R.id.contentCategory);
        }
    }
}
