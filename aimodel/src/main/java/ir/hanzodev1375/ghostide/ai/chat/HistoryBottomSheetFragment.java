package ir.hanzodev1375.ghostide.ai.chat;

import android.content.res.ColorStateList;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import com.google.android.material.color.MaterialColors;
import com.google.android.material.listitem.ListItemCardView;
import com.google.android.material.listitem.ListItemViewHolder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ir.hanzodev1375.ghostide.ai.R;
import ir.hanzodev1375.ghostide.ai.database.ChatRepository;

public class HistoryBottomSheetFragment extends BottomSheetDialogFragment {

  private ChatRepository repository;
  private HistoryAdapter adapter;
  private OnChatsSelectedListener listener;
  private ProgressBar progressBar;
  private RecyclerView recyclerView;
  private View emptyView;
  private ExecutorService executor = Executors.newSingleThreadExecutor();
  private Handler mainHandler = new Handler(Looper.getMainLooper());

  public interface OnChatsSelectedListener {
    void onLoadChat(long chatId);

    void onDeleteChats(List<Long> chatIds);
  }

  public void setOnChatsSelectedListener(OnChatsSelectedListener listener) {
    this.listener = listener;
  }

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setRetainInstance(true);
    if (getContext() != null) {
      repository = new ChatRepository(getContext());
    }
  }

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    return inflater.inflate(R.layout.bottom_sheet_history, container, false);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    recyclerView = view.findViewById(R.id.rv_history);
    progressBar = view.findViewById(R.id.progress_history);
    emptyView = view.findViewById(R.id.emptyview);
    Button btnDelete = view.findViewById(R.id.btn_delete_selected);
    Button btnCancel = view.findViewById(R.id.btn_cancel);

    recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    adapter = new HistoryAdapter();
    recyclerView.setAdapter(adapter);
    recyclerView.addItemDecoration(new MarginItemDecoration());

    loadChatsAsync();

    btnDelete.setOnClickListener(
        v -> {
          List<Long> selectedIds = adapter.getSelectedIds();
          if (!selectedIds.isEmpty() && listener != null) {
            listener.onDeleteChats(selectedIds);
            loadChatsAsync();
            Toast.makeText(
                    getContext(), "Deleted " + selectedIds.size() + " chats", Toast.LENGTH_SHORT)
                .show();
          } else {
            Toast.makeText(getContext(), "No chat selected", Toast.LENGTH_SHORT).show();
          }
        });

    btnCancel.setOnClickListener(v -> dismiss());
  }

  private void loadChatsAsync() {
    if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
    executor.execute(
        () -> {
          try {
            List<ChatRepository.ChatItem> loaded = repository.getAllChats();
            emptyView.setVisibility(!loaded.isEmpty() ? View.GONE : View.VISIBLE);
            mainHandler.post(
                () -> {
                  adapter.setData(loaded);
                  if (progressBar != null) progressBar.setVisibility(View.GONE);
                });
          } catch (Exception e) {
            mainHandler.post(
                () -> {
                  if (progressBar != null) progressBar.setVisibility(View.GONE);
                  Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT)
                      .show();
                });
          }
        });
  }

  private class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    private List<ChatRepository.ChatItem> items = new ArrayList<>();
    private List<Boolean> selected = new ArrayList<>();

    @Override
    public long getItemId(int position) {
      return items.get(position).id;
    }

    public void setData(List<ChatRepository.ChatItem> newItems) {
      items.clear();
      items.addAll(newItems);
      selected.clear();
      for (int i = 0; i < items.size(); i++) {
        selected.add(false);
      }
      notifyDataSetChanged();
    }

    List<Long> getSelectedIds() {
      List<Long> ids = new ArrayList<>();
      for (int i = 0; i < items.size(); i++) {
        if (selected.get(i)) ids.add(items.get(i).id);
      }
      return ids;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
      View v =
          LayoutInflater.from(parent.getContext())
              .inflate(R.layout.item_history_chat, parent, false);
      return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
      ChatRepository.ChatItem chat = items.get(position);
      SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault());
      String date = sdf.format(new Date(chat.createdAt));
      String title = (chat.title == null || chat.title.isEmpty()) ? "Chat " + chat.id : chat.title;
      holder.tvTitle.setText(title);
      holder.tvDate.setText(date);
      holder.bind(position, getItemCount());
      holder.checkBox.setOnCheckedChangeListener(null);
      holder.checkBox.setChecked(selected.get(position));
      holder.checkBox.setOnCheckedChangeListener(
          (buttonView, isChecked) -> {
            selected.set(position, isChecked);
          });

      holder.itemView.setOnClickListener(
          v -> {
            if (listener != null && !selected.get(position)) {
              listener.onLoadChat(chat.id);
              dismiss();
            }
          });
    }

    @Override
    public int getItemCount() {
      return items.size();
    }

    class ViewHolder extends ListItemViewHolder {
      TextView tvTitle, tvDate;
      CheckBox checkBox;
      ListItemCardView card;

      ViewHolder(@NonNull View itemView) {
        super(itemView);
        tvTitle = itemView.findViewById(R.id.tv_chat_title);
        tvDate = itemView.findViewById(R.id.tv_chat_date);
        checkBox = itemView.findViewById(R.id.checkbox_select);
        card = itemView.findViewById(R.id.card);
        card.setCardBackgroundColor(
            ColorStateList.valueOf(
                MaterialColors.getColor(
                    card, com.google.android.material.R.attr.colorSurfaceContainerHighest)));
      }
    }
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    executor.shutdownNow();
  }

  class MarginItemDecoration extends RecyclerView.ItemDecoration {
    private final int itemMargin;

    public MarginItemDecoration() {
      itemMargin = 2;
    }

    @Override
    public void getItemOffsets(
        @NonNull Rect outRect,
        @NonNull View view,
        @NonNull RecyclerView parent,
        @NonNull RecyclerView.State state) {
      int position = parent.getChildAdapterPosition(view);
      if (position != state.getItemCount() - 1) {
        outRect.bottom = itemMargin;
      }
    }
  }
}
