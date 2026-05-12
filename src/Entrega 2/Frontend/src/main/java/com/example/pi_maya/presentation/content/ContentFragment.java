package com.example.pi_maya.presentation.content;

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
import com.example.pi_maya.presentation.common.ContentCardAdapter;

public class ContentFragment extends Fragment {

    public static final String EXTRA_CONTENT_ID = "extra_content_id";
    public static final String EXTRA_CONTENT_TITLE = "extra_content_title";
    public static final String EXTRA_CONTENT_BODY = "extra_content_body";

    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView recycler;
    private TextView emptyText;
    private ContentCardAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_content, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        swipeRefresh = view.findViewById(R.id.swipeRefresh);
        recycler = view.findViewById(R.id.contentRecycler);
        emptyText = view.findViewById(R.id.emptyText);

        adapter = new ContentCardAdapter(content -> {
            Intent intent = new Intent(requireContext(), ContentDetailActivity.class);
            intent.putExtra(EXTRA_CONTENT_ID, content.id);
            intent.putExtra(EXTRA_CONTENT_TITLE, content.title);
            intent.putExtra(EXTRA_CONTENT_BODY, content.body);
            startActivity(intent);
        });
        recycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        recycler.setAdapter(adapter);

        swipeRefresh.setOnRefreshListener(this::loadData);
        loadData();
    }

    private void loadData() {
        swipeRefresh.setRefreshing(true);
        MayaApp.get().getContentRepository().getPublishedContent()
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
