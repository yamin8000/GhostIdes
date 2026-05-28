package ir.hanzodev1375.ghostide.adapters;

import android.content.Context;
import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import ir.hanzodev1375.ghostide.R;
import ir.hanzodev1375.ghostide.models.FileManagerModel;

public class FileManagerAdapter extends RecyclerView.Adapter<FileManagerAdapter.ViewHolder> {

  private List<FileManagerModel> items = new ArrayList<>();
  private List<FileManagerModel> itemsFull = new ArrayList<>();
  private String searchQuery = "";
  private int highlightColor;
  private Context context;
  private OnItemClickListener itemClickListener;
  private OnMoreClickListener moreClickListener;

  public interface OnItemClickListener {
    void onItemClick(FileManagerModel item, int position);
  }

  public interface OnMoreClickListener {
    void onMoreClick(FileManagerModel item, View moreView, int position);
  }

  public FileManagerAdapter(Context context) {
    this.context = context;
    highlightColor = Color.parseColor("#200180");
  }

  public void setOnItemClickListener(OnItemClickListener listener) {
    this.itemClickListener = listener;
  }

  public void setOnMoreClickListener(OnMoreClickListener listener) {
    this.moreClickListener = listener;
  }

  public void submitList(List<FileManagerModel> newList) {
    DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new FileDiffCallback(items, newList));
    items = newList != null ? newList : new ArrayList<>();
    itemsFull = new ArrayList<>(items);
    diffResult.dispatchUpdatesTo(this);
  }

  public void search(String query) {
    this.searchQuery = query == null ? "" : query.trim();

    if (searchQuery.isEmpty()) {
      submitList(new ArrayList<>(itemsFull));
      return;
    }

    String lowerQuery = searchQuery.toLowerCase();
    List<FileManagerModel> filteredList = new ArrayList<>();

    for (FileManagerModel item : itemsFull) {
      if (item.getName().toLowerCase().contains(lowerQuery)) {
        filteredList.add(item);
      }
    }

    DiffUtil.DiffResult diffResult =
        DiffUtil.calculateDiff(new FileDiffCallback(items, filteredList));
    items = filteredList;
    diffResult.dispatchUpdatesTo(this);
  }

  @NonNull
  @Override
  public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View view =
        LayoutInflater.from(parent.getContext()).inflate(R.layout.item_file_manager, parent, false);
    return new ViewHolder(view);
  }

  @Override
  public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
    FileManagerModel item = items.get(position);
    holder.bind(item, position);
  }

  @Override
  public int getItemCount() {
    return items.size();
  }

  class ViewHolder extends RecyclerView.ViewHolder {
    ImageView ivIcon;
    TextView tvName;
    TextView tvDate;
    ImageView ivMore;

    ViewHolder(@NonNull View itemView) {
      super(itemView);
      ivIcon = itemView.findViewById(R.id.ivIcon);
      tvName = itemView.findViewById(R.id.tvName);
      tvDate = itemView.findViewById(R.id.tvDate);
      ivMore = itemView.findViewById(R.id.ivMore);

      itemView.setOnClickListener(
          v -> {
            int pos = getAdapterPosition();
            if (pos != RecyclerView.NO_POSITION && itemClickListener != null) {
              itemClickListener.onItemClick(items.get(pos), pos);
            }
          });

      ivMore.setOnClickListener(
          v -> {
            int pos = getAdapterPosition();
            if (pos != RecyclerView.NO_POSITION && moreClickListener != null) {
              moreClickListener.onMoreClick(items.get(pos), ivMore, pos);
            }
          });
    }

    void bind(FileManagerModel item, int position) {
      // ست کردن نام با هایلایت
      if (searchQuery.isEmpty()) {
        tvName.setText(item.getName());
      } else {
        tvName.setText(getHighlightedText(item.getName(), searchQuery));
      }

      tvDate.setText(item.getLastModifiedFormatted());
    }

    private SpannableString getHighlightedText(String text, String query) {
      SpannableString spannableString = new SpannableString(text);
      String lowerText = text.toLowerCase();
      String lowerQuery = query.toLowerCase();

      int startIndex = 0;
      while ((startIndex = lowerText.indexOf(lowerQuery, startIndex)) != -1) {
        int endIndex = startIndex + query.length();
        spannableString.setSpan(
            new ForegroundColorSpan(highlightColor),
            startIndex,
            endIndex,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        startIndex = endIndex;
      }

      return spannableString;
    }
  }

  private static class FileDiffCallback extends DiffUtil.Callback {
    private final List<FileManagerModel> oldList;
    private final List<FileManagerModel> newList;

    FileDiffCallback(List<FileManagerModel> oldList, List<FileManagerModel> newList) {
      this.oldList = oldList;
      this.newList = newList;
    }

    @Override
    public int getOldListSize() {
      return oldList.size();
    }

    @Override
    public int getNewListSize() {
      return newList.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
      return oldList.get(oldItemPosition).getPath().equals(newList.get(newItemPosition).getPath());
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
      FileManagerModel oldItem = oldList.get(oldItemPosition);
      FileManagerModel newItem = newList.get(newItemPosition);
      return oldItem.getName().equals(newItem.getName())
          && oldItem.getLastModified() == newItem.getLastModified()
          && oldItem.getState() == newItem.getState();
    }
  }
}
