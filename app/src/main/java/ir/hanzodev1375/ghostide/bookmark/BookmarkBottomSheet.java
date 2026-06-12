package ir.hanzodev1375.ghostide.bookmark;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import ir.hanzodev1375.ghostide.R;
import ir.hanzodev1375.ghostide.utils.MarginItemDecoration;

public class BookmarkBottomSheet extends BottomSheetDialogFragment {

  public static final String TAG = "BookmarkBottomSheet";

  public interface OnBookmarkSelectedListener {
    void onBookmarkSelected(BookmarkEntity item);
  }

  private OnBookmarkSelectedListener listener;
  private BookmarkViewModel bookmarkViewModel;

  public static BookmarkBottomSheet newInstance() {
    return new BookmarkBottomSheet();
  }

  public void setOnBookmarkSelectedListener(OnBookmarkSelectedListener listener) {
    this.listener = listener;
  }

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    return inflater.inflate(R.layout.bottom_sheet_bookmarks, container, false);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    bookmarkViewModel = new ViewModelProvider(requireActivity()).get(BookmarkViewModel.class);

    RecyclerView rvBookmarks = view.findViewById(R.id.rvBookmarks);
    View tvEmpty = view.findViewById(R.id.tvBookmarkEmpty);
    View btnClear = view.findViewById(R.id.btnClearBookmarks);

    BookmarkAdapter adapter =
        new BookmarkAdapter(
            item -> {
              if (listener != null) listener.onBookmarkSelected(item);
              dismiss();
            },
            item -> bookmarkViewModel.removeBookmark(item.path));

    rvBookmarks.setLayoutManager(new LinearLayoutManager(requireContext()));
    rvBookmarks.setAdapter(adapter);
    rvBookmarks.addItemDecoration(new MarginItemDecoration(requireContext()));

    bookmarkViewModel
        .getBookmarks()
        .observe(
            getViewLifecycleOwner(),
            list -> {
              adapter.submitList(list);
              boolean isEmpty = list == null || list.isEmpty();
              rvBookmarks.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
              tvEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
            });

    btnClear.setOnClickListener(v -> bookmarkViewModel.clearAll());
  }
}
