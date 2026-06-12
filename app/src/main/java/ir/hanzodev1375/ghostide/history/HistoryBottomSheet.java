package ir.hanzodev1375.ghostide.history;

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

public class HistoryBottomSheet extends BottomSheetDialogFragment {

  public static final String TAG = "HistoryBottomSheet";

  public interface OnHistoryItemSelectedListener {
    void onHistoryItemSelected(HistoryEntity item);
  }

  private OnHistoryItemSelectedListener listener;
  private HistoryViewModel historyViewModel;

  public static HistoryBottomSheet newInstance() {
    return new HistoryBottomSheet();
  }

  public void setOnHistoryItemSelectedListener(OnHistoryItemSelectedListener listener) {
    this.listener = listener;
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

    historyViewModel = new ViewModelProvider(requireActivity()).get(HistoryViewModel.class);

    RecyclerView rvHistory = view.findViewById(R.id.rvHistory);
    View tvEmpty = view.findViewById(R.id.tvHistoryEmpty);
    View btnClear = view.findViewById(R.id.btnClearHistory);
    HistoryAdapter adapter =
        new HistoryAdapter(
            item -> {
              if (listener != null) listener.onHistoryItemSelected(item);
              dismiss();
            });

    rvHistory.setLayoutManager(new LinearLayoutManager(requireContext()));
    rvHistory.setAdapter(adapter);
    rvHistory.addItemDecoration(new MarginItemDecoration(requireContext()));

    historyViewModel
        .getHistory()
        .observe(
            getViewLifecycleOwner(),
            list -> {
              adapter.submitList(list);
              boolean isEmpty = list == null || list.isEmpty();
              rvHistory.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
              tvEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
            });

    btnClear.setOnClickListener(v -> historyViewModel.clearHistory());
  }
}
