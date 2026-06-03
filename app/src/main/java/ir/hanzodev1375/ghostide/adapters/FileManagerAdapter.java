package ir.hanzodev1375.ghostide.adapters;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.selection.ItemDetailsLookup;
import androidx.recyclerview.selection.ItemKeyProvider;
import androidx.recyclerview.selection.SelectionTracker;
import androidx.recyclerview.selection.StorageStrategy;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;
import com.bluewhaleyt.materialfileicon.core.FileIconHelper;
import com.bumptech.glide.Glide;
import com.google.android.material.color.MaterialColors;
import com.google.android.material.listitem.ListItemCardView;
import com.google.android.material.listitem.ListItemViewHolder;
import ir.hanzodev1375.ghostide.R;
import ir.hanzodev1375.ghostide.models.FileManagerModel;
import ir.hanzodev1375.ghostide.utils.ShapeUtil;
import java.util.ArrayList;
import java.util.List;

public class FileManagerAdapter extends RecyclerView.Adapter<FileManagerAdapter.ViewHolder> {

  private List<FileManagerModel> items = new ArrayList<>();
  private List<FileManagerModel> itemsFull = new ArrayList<>();
  private String searchQuery = "";
  private int highlightColor;
  private Context context;
  private SelectionTracker<Long> selectionTracker;
  private OnItemClickListener itemClickListener;
  private OnMoreClickListener moreClickListener;
  private SelectionStateListener selectionStateListener;

  public interface OnItemClickListener {
    void onItemClick(FileManagerModel item, int position);
  }

  public interface OnMoreClickListener {
    void onMoreClick(FileManagerModel item, View moreView, int position);
  }

  public interface SelectionStateListener {
    void onSelectionChanged(int count);

    void onSelectionModeStarted();

    void onSelectionModeEnded();
  }

  public FileManagerAdapter(Context context) {
    this.context = context;
    setHasStableIds(true);
    highlightColor = Color.parseColor("#200180");
  }

  public void setOnItemClickListener(OnItemClickListener listener) {
    this.itemClickListener = listener;
  }

  public void setOnMoreClickListener(OnMoreClickListener listener) {
    this.moreClickListener = listener;
  }

  public void setSelectionStateListener(SelectionStateListener listener) {
    this.selectionStateListener = listener;
  }

  public void setupSelectionTracker(RecyclerView recyclerView) {
    if (selectionTracker != null) return;
    ItemKeyProvider<Long> keyProvider =
        new ItemKeyProvider<Long>(ItemKeyProvider.SCOPE_CACHED) {
          @Nullable
          @Override
          public Long getKey(int position) {
            if (position < 0 || position >= getItemCount()) return null;
            return getItemId(position);
          }

          @Override
          public int getPosition(@NonNull Long key) {
            for (int i = 0; i < getItemCount(); i++) {
              if (getItemId(i) == key) return i;
            }
            return RecyclerView.NO_POSITION;
          }
        };
    ItemDetailsLookup<Long> detailsLookup =
        new ItemDetailsLookup<Long>() {
          @Nullable
          @Override
          public ItemDetails<Long> getItemDetails(@NonNull MotionEvent e) {
            View view = recyclerView.findChildViewUnder(e.getX(), e.getY());
            if (view != null) {
              RecyclerView.ViewHolder vh = recyclerView.getChildViewHolder(view);
              if (vh instanceof ViewHolder) {
                int pos = vh.getBindingAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                  final int position = pos;
                  return new ItemDetails<Long>() {
                    @Override
                    public int getPosition() {
                      return position;
                    }

                    @Nullable
                    @Override
                    public Long getSelectionKey() {
                      return getItemId(position);
                    }
                  };
                }
              }
            }
            return null;
          }
        };
    selectionTracker =
        new SelectionTracker.Builder<>(
                "file_selection_id",
                recyclerView,
                keyProvider,
                detailsLookup,
                StorageStrategy.createLongStorage())
            .build();
    selectionTracker.addObserver(
        new SelectionTracker.SelectionObserver<Long>() {
          @Override
          public void onSelectionChanged() {
            int count = selectionTracker.getSelection().size();
            if (selectionStateListener != null) {
              selectionStateListener.onSelectionChanged(count);
              if (count > 0) selectionStateListener.onSelectionModeStarted();
              else selectionStateListener.onSelectionModeEnded();
            }
            // notifyDataSetChanged();
          }
        });
  }

  public SelectionTracker<Long> getSelectionTracker() {
    return selectionTracker;
  }

  public void selectAll() {
    if (selectionTracker == null) return;
    for (int i = 0; i < items.size(); i++) {
      long id = getItemId(i);
      if (!selectionTracker.isSelected(id)) {
        selectionTracker.select(id);
      }
    }
    if (selectionStateListener != null) {
      selectionStateListener.onSelectionChanged(getSelectedItems().size());
      if (getSelectedItems().size() > 0) {
        selectionStateListener.onSelectionModeStarted();
      }
    }
  }

  public List<FileManagerModel> getSelectedItems() {
    List<FileManagerModel> selected = new ArrayList<>();
    if (selectionTracker == null) return selected;
    for (Long id : selectionTracker.getSelection()) {
      int pos = -1;
      for (int i = 0; i < items.size(); i++) {
        if (getItemId(i) == id) {
          pos = i;
          break;
        }
      }
      if (pos != -1) selected.add(items.get(pos));
    }
    return selected;
  }

  public void clearSelection() {
    if (selectionTracker != null) selectionTracker.clearSelection();
  }

  public boolean isInSelectionMode() {
    return selectionTracker != null && selectionTracker.hasSelection();
  }

  public void submitList(List<FileManagerModel> newList) {
    DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new FileDiffCallback(items, newList));
    items = newList != null ? newList : new ArrayList<>();
    itemsFull = new ArrayList<>(items);
    diffResult.dispatchUpdatesTo(this);
    if (selectionTracker != null) selectionTracker.clearSelection();
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
    if (selectionTracker != null) selectionTracker.clearSelection();
  }

  @Override
  public long getItemId(int position) {
    return items.get(position).getPath().hashCode();
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
    holder.bindItem(item, position);
    holder.bind(position, getItemCount());
    boolean isSelected =
        selectionTracker != null && selectionTracker.isSelected(getItemId(position));
    holder.card.setCardBackgroundColor(
        ColorStateList.valueOf(
            isSelected
                ? ShapeUtil.getcolorPrimaryContainer(holder.card)
                : ShapeUtil.getcolorSurfaceContainer(holder.card)));
  }

  @Override
  public int getItemCount() {
    return items.size();
  }

  class ViewHolder extends ListItemViewHolder {
    ImageView ivIcon;
    TextView tvName;
    TextView tvDate;
    ImageView ivMore;
    ListItemCardView card;

    ViewHolder(@NonNull View itemView) {
      super(itemView);
      ivIcon = itemView.findViewById(R.id.ivIcon);
      tvName = itemView.findViewById(R.id.tvName);
      tvDate = itemView.findViewById(R.id.tvDate);
      ivMore = itemView.findViewById(R.id.ivMore);
      card = itemView.findViewById(R.id.listcard);
      itemView.setOnClickListener(
          v -> {
            int pos = getBindingAdapterPosition();
            if (pos == RecyclerView.NO_POSITION) return;
            if (selectionTracker != null && selectionTracker.hasSelection()) {
              // Toggle selection instead of always selecting
              long id = FileManagerAdapter.this.getItemId(pos);
              if (selectionTracker.isSelected(id)) {
                selectionTracker.deselect(id);
              } else {
                selectionTracker.select(id);
              }
            } else {
              if (itemClickListener != null) {
                itemClickListener.onItemClick(items.get(pos), pos);
              }
            }
          });

      itemView.setOnLongClickListener(
          v -> {
            if (selectionTracker != null && !selectionTracker.hasSelection()) {
              int pos = getBindingAdapterPosition();
              if (pos != RecyclerView.NO_POSITION) {
                selectionTracker.select(FileManagerAdapter.this.getItemId(pos));
                return true;
              }
            }
            return false;
          });
      ivMore.setOnClickListener(
          v -> {
            int pos = getBindingAdapterPosition();
            if (pos != RecyclerView.NO_POSITION
                && moreClickListener != null
                && (selectionTracker == null || !selectionTracker.hasSelection())) {
              moreClickListener.onMoreClick(items.get(pos), ivMore, pos);
            }
          });
    }

    void bindItem(FileManagerModel item, int position) {
      var iconHelper = new FileIconHelper(item.getPath());
      iconHelper.setDynamicFolderEnabled(true);
      iconHelper.setEnvironmentEnabled(true);
      Glide.with(ivIcon.getContext())
          .load(iconHelper.getFileIcon())
          .error(R.drawable.ic_close)
          .into(ivIcon);
      if (searchQuery.isEmpty()) {
        tvName.setText(item.getName());
      } else {
        tvName.setText(getHighlightedText(item.getName(), searchQuery));
      }
      tvDate.setText(item.getLastModifiedFormatted());
      var gd = new GradientDrawable();
      gd.setColor(MaterialColors.getColor(ivIcon, com.google.android.material.R.attr.colorSurface));
      gd.setStroke(
          1, MaterialColors.getColor(ivIcon, com.google.android.material.R.attr.colorOutline));
      gd.setCornerRadius(8);
      ivIcon.setPadding(5, 5, 5, 5);
      ivIcon.setBackground(gd);
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
