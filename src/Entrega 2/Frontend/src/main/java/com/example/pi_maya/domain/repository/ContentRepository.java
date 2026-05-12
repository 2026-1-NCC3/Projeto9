package com.example.pi_maya.domain.repository;

import androidx.lifecycle.LiveData;

import com.example.pi_maya.core.result.Resource;
import com.example.pi_maya.domain.model.EducationalContent;

import java.util.List;

public interface ContentRepository {
    LiveData<Resource<List<EducationalContent>>> getPublishedContent();
}
