package com.example.pi_maya.presentation.chat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pi_maya.R;
import com.example.pi_maya.domain.model.ChatMessage;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_IN = 0;
    private static final int TYPE_OUT = 1;
    private static final DateTimeFormatter HM = DateTimeFormatter.ofPattern("HH:mm");

    private final List<ChatMessage> items = new ArrayList<>();
    private final String myProfileId;

    public MessageAdapter(String myProfileId) {
        this.myProfileId = myProfileId;
    }

    public void submit(List<ChatMessage> data) {
        items.clear();
        if (data != null) items.addAll(data);
        notifyDataSetChanged();
    }

    public void append(ChatMessage msg) {
        items.add(msg);
        notifyItemInserted(items.size() - 1);
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position).isMine(myProfileId) ? TYPE_OUT : TYPE_IN;
    }

    @NonNull @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layoutId = viewType == TYPE_OUT
                ? R.layout.item_message_out
                : R.layout.item_message_in;
        View v = LayoutInflater.from(parent.getContext()).inflate(layoutId, parent, false);
        return new MsgVH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage m = items.get(position);
        MsgVH h = (MsgVH) holder;
        h.text.setText(m.content != null ? m.content : "");
        if (m.createdAt != null) {
            h.time.setText(m.createdAt.atZoneSameInstant(ZoneId.systemDefault()).toLocalTime().format(HM));
        } else {
            h.time.setText("");
        }
    }

    @Override public int getItemCount() { return items.size(); }

    static class MsgVH extends RecyclerView.ViewHolder {
        final TextView text;
        final TextView time;
        MsgVH(View v) {
            super(v);
            text = v.findViewById(R.id.messageText);
            time = v.findViewById(R.id.messageTime);
        }
    }
}
