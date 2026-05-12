package com.example.pi_maya.presentation.content;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.example.pi_maya.MayaApp;
import com.example.pi_maya.R;
import com.example.pi_maya.domain.model.EducationalContent;

import java.util.List;

public class ContentDetailActivity extends AppCompatActivity {

    private TextView titleView;
    private TextView bodyView;
    private TextView categoryChip;
    private ImageView coverImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_content_detail);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        titleView = findViewById(R.id.contentTitle);
        bodyView = findViewById(R.id.contentBody);
        categoryChip = findViewById(R.id.categoryChip);
        coverImage = findViewById(R.id.coverImage);

        String contentId = getIntent().getStringExtra(ContentFragment.EXTRA_CONTENT_ID);
        String title = getIntent().getStringExtra(ContentFragment.EXTRA_CONTENT_TITLE);
        String body = getIntent().getStringExtra(ContentFragment.EXTRA_CONTENT_BODY);

        titleView.setText(title != null ? title : "");
        bodyView.setText(body != null ? body : "");
        categoryChip.setVisibility(View.GONE);

        // Busca os dados completos para pegar cover_url + categoria
        if (contentId != null) {
            MayaApp.get().getContentRepository().getPublishedContent()
                    .observe(this, resource -> {
                        if (!resource.isSuccess() || resource.getData() == null) return;
                        List<EducationalContent> all = resource.getData();
                        for (EducationalContent c : all) {
                            if (contentId.equals(c.id)) {
                                renderContent(c);
                                break;
                            }
                        }
                    });
        }
    }

    private void renderContent(EducationalContent c) {
        if (!TextUtils.isEmpty(c.title)) titleView.setText(c.title);
        if (!TextUtils.isEmpty(c.body)) bodyView.setText(c.body);

        if (!TextUtils.isEmpty(c.category)) {
            categoryChip.setVisibility(View.VISIBLE);
            categoryChip.setText(c.category);
        }

        if (!TextUtils.isEmpty(c.coverUrl)) {
            Glide.with(this)
                    .load(c.coverUrl)
                    .centerCrop()
                    .into(coverImage);
        }
    }
}
