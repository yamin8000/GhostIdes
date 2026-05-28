package ir.hanzodev1375.ghostide.activity;

import android.os.Bundle;
import android.view.View;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import ir.hanzodev1375.ghostide.R;
import ir.hanzodev1375.ghostide.adapters.FileManagerAdapter;
import ir.hanzodev1375.ghostide.databinding.ActivityFilemanagerBinding;
import ir.hanzodev1375.ghostide.mvvm.viewmodel.FileViewModel;

public class FileManagerActivity extends AppCompatActivity {
  private ActivityFilemanagerBinding bind;
  private FileViewModel viewModel;
  private FileManagerAdapter adapter;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    bind = ActivityFilemanagerBinding.inflate(getLayoutInflater());
    setContentView(bind.getRoot());

    viewModel = new ViewModelProvider(this).get(FileViewModel.class);
    adapter = new FileManagerAdapter(this);

    bind.rvfiles.setLayoutManager(new LinearLayoutManager(this));
    bind.rvfiles.setAdapter(adapter);

    viewModel.getFiles().observe(this, files -> adapter.submitList(files));
    viewModel
        .getIsLoading()
        .observe(
            this,
            loading -> {
              bind.loadingprogass.setVisibility(loading ? View.VISIBLE : View.GONE);
            });
    viewModel.savePath(true);
    adapter.setOnItemClickListener(
        (item, pos) -> {
          if (item.isDirectory()) {
            viewModel.navigateTo(item.getPath());
          }
        });

    bind.fab.setOnClickListener(
        v -> {
          // TODO: create file/folder
        });

    setOnBackPress();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    bind = null;
  }

  void setOnBackPress() {
    getOnBackPressedDispatcher()
        .addCallback(
            this,
            new OnBackPressedCallback(true) {
              @Override
              public void handleOnBackPressed() {
                if (viewModel.getCurrentPath().getValue() != null
                    && !viewModel.getCurrentPath().getValue().equals("/storage/emulated/0")) {
                  viewModel.navigateUp();
                } else {
                  new MaterialAlertDialogBuilder(FileManagerActivity.this)
                      .setTitle("Exit")
                      .setMessage("Exit in Ghost ide")
                      .setNegativeButton("yes", (c, f) -> finishAffinity())
                      .setPositiveButton("no", null)
                      .show();
                }
              }
            });
  }
}
