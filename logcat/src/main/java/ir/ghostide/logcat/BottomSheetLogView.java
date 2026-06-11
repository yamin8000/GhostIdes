package ir.ghostide.logcat;

import android.os.Bundle;
import android.app.Dialog;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class BottomSheetLogView extends BottomSheetDialogFragment {
  private MaterialLogCatView logview;
  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {

    return inflater.inflate(R.layout.fragment_logview, container, false);
  }
  @Override
  public void onViewCreated(View view, Bundle arg1) {
    super.onViewCreated(view, arg1);
    logview = view.findViewById(R.id.logview);
  }
}
