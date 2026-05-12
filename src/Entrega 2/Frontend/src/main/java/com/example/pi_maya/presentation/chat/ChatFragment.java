package com.example.pi_maya.presentation.chat;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.pi_maya.MayaApp;
import com.example.pi_maya.R;
import com.example.pi_maya.domain.model.ChatRoom;
import com.google.android.material.card.MaterialCardView;

public class ChatFragment extends Fragment {

    public static final String EXTRA_ROOM_ID = "extra_room_id";
    public static final String EXTRA_THERAPIST_NAME = "extra_therapist_name";

    private SwipeRefreshLayout swipeRefresh;
    private MaterialCardView therapistCard;
    private TextView therapistName;
    private TextView emptyText;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_chat, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        swipeRefresh = view.findViewById(R.id.swipeRefresh);
        therapistCard = view.findViewById(R.id.therapistCard);
        therapistName = view.findViewById(R.id.therapistName);
        emptyText = view.findViewById(R.id.emptyText);

        swipeRefresh.setOnRefreshListener(this::loadData);
        loadData();
    }

    private void loadData() {
        swipeRefresh.setRefreshing(true);
        MayaApp.get().getChatRepository().getMyChatRoom()
                .observe(getViewLifecycleOwner(), resource -> {
                    swipeRefresh.setRefreshing(false);
                    if (resource.isSuccess()) {
                        ChatRoom room = resource.getData();
                        if (room != null) {
                            therapistCard.setVisibility(View.VISIBLE);
                            emptyText.setVisibility(View.GONE);
                            therapistName.setText(room.therapistName != null
                                    ? room.therapistName : "Sua fisioterapeuta");
                            therapistCard.setOnClickListener(v -> openRoom(room));
                        } else {
                            therapistCard.setVisibility(View.GONE);
                            emptyText.setVisibility(View.VISIBLE);
                        }
                    } else if (resource.isError()) {
                        therapistCard.setVisibility(View.GONE);
                        emptyText.setVisibility(View.VISIBLE);
                        emptyText.setText(resource.getMessage());
                    }
                });
    }

    private void openRoom(ChatRoom room) {
        Intent intent = new Intent(requireContext(), ChatRoomActivity.class);
        intent.putExtra(EXTRA_ROOM_ID, room.id);
        intent.putExtra(EXTRA_THERAPIST_NAME, room.therapistName);
        startActivity(intent);
    }
}
