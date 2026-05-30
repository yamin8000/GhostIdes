package ir.hanzodev1375.ghostide.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import ir.hanzodev1375.ghostide.adapters.FileManagerAdapter;
import ir.hanzodev1375.ghostide.databinding.ActivityFilemanagerBinding;
import ir.hanzodev1375.ghostide.mvvm.viewmodel.FileViewModel;
import ir.hanzodev1375.ghostide.utils.ShapeUtil;

public class FileManagerActivity extends BaseCompat {

  private ActivityFilemanagerBinding bind;
  private FileViewModel viewModel;
  private FileManagerAdapter adapter;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    EdgeToEdge.enable(this);

    bind = ActivityFilemanagerBinding.inflate(getLayoutInflater());

    setContentView(bind.getRoot());

    setupInsets();

    bind.headline.setBackground(ShapeUtil.shape(40f, this));
    viewModel = new ViewModelProvider(this).get(FileViewModel.class);
    adapter = new FileManagerAdapter(this);
    bind.rvfiles.setLayoutManager(new LinearLayoutManager(this));
    bind.rvfiles.setAdapter(adapter);
    viewModel.getFiles().observe(this, files -> adapter.submitList(files));
    viewModel
        .getIsLoading()
        .observe(
            this, loading -> bind.loadingprogass.setVisibility(loading ? View.VISIBLE : View.GONE));

    viewModel.savePath(true);

    adapter.setOnItemClickListener(
        (item, pos) -> {
          if (item.isDirectory()) {

            viewModel.navigateTo(item.getPath());

          } else {

            Intent intent = new Intent(FileManagerActivity.this, EditorActivity.class);

            intent.putExtra("file_path", item.getPath());

            intent.putExtra("file_name", item.getName());

            startActivity(intent);
          }
        });

    bind.fab.setOnClickListener(v -> {});

    setOnBackPress();
  }

  private void setupInsets() {

    ViewCompat.setOnApplyWindowInsetsListener(
        bind.coordinator,
        (view, insets) -> {
          Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());

          bind.headtop.setPadding(0, systemBars.top, 0, 0);

          bind.fab.post(
              () -> {
                int fabSpace = bind.fab.getHeight() + 48;

                bind.rvfiles.setPadding(
                    bind.rvfiles.getPaddingLeft(),
                    bind.rvfiles.getPaddingTop(),
                    bind.rvfiles.getPaddingRight(),
                    systemBars.bottom + fabSpace);
              });

          return insets;
        });
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    bind = null;
  }

  private void setOnBackPress() {

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
