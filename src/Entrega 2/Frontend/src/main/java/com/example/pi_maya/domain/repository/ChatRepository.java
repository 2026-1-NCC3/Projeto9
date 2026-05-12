package com.example.pi_maya.domain.repository;

import androidx.lifecycle.LiveData;

import com.example.pi_maya.core.result.Resource;
import com.example.pi_maya.domain.model.ChatMessage;
import com.example.pi_maya.domain.model.ChatRoom;

import java.util.List;

public interface ChatRepository {
    LiveData<Resource<ChatRoom>> getMyChatRoom();
    LiveData<Resource<List<ChatMessage>>> getMessages(String roomId);
    LiveData<Resource<ChatMessage>> sendMessage(String roomId, String content);
}
