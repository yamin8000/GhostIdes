package ir.hanzodev1375.ghostide.jgit.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import ir.hanzodev1375.ghostide.jgit.R;

public class CommitDialog extends DialogFragment {
  private EditText etMessage, etAuthor, etEmail;
  private OnCommitListener listener;

  public interface OnCommitListener {
    void onCommit(String message, String author, String email);
  }

  public void setOnCommitListener(OnCommitListener listener) {
    this.listener = listener;
  }

  @NonNull
  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    View view = requireActivity().getLayoutInflater().inflate(R.layout.dialog_commit, null);
    etMessage = view.findViewById(R.id.etCommitMessage);
    etAuthor = view.findViewById(R.id.etAuthor);
    etEmail = view.findViewById(R.id.etEmail);

    return new MaterialAlertDialogBuilder(requireActivity())
        .setTitle("Commit Changes")
        .setView(view)
        .setPositiveButton(
            "Commit",
            (dialog, which) -> {
              if (listener != null) {
                listener.onCommit(
                    etMessage.getText().toString(),
                    etAuthor.getText().toString(),
                    etEmail.getText().toString());
              }
            })
        .setNegativeButton("Cancel", null)
        .create();
  }
}
