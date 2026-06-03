package ir.hanzodev1375.ghostide.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.view.ActionMode;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import ir.hanzodev1375.ghostide.adapters.FileManagerAdapter;
import ir.hanzodev1375.ghostide.databinding.ActivityFilemanagerBinding;
import ir.hanzodev1375.ghostide.databinding.SelectionPanelBinding;
import ir.hanzodev1375.ghostide.models.FileManagerModel;
import ir.hanzodev1375.ghostide.mvvm.viewmodel.FileViewModel;
import ir.hanzodev1375.ghostide.plugin.PluginManager;
import ir.hanzodev1375.ghostide.utils.MarginItemDecoration;
import ir.hanzodev1375.ghostide.utils.ShapeUtil;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import ir.hanzodev1375.ghostide.R;
import java.util.Set;

public class FileManagerActivity extends BaseCompat {

  private ActivityFilemanagerBinding bind;
  private FileViewModel viewModel;
  private FileManagerAdapter adapter;
  private View selectionPanel;
  private TextView selectionCount;
  private ImageView btnCopy, btnCut, btnDelete, btnPaste, btnClose;
  private boolean isCutOperation = false;
  private List<FileManagerModel> pendingClipboard = new ArrayList<>();
  private SelectionPanelBinding selectionPanelBinding;
  private Set<String> itemname = new HashSet<>(Arrays.asList(".html", ".java", ".cpp",".css",".js"));

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    bind = ActivityFilemanagerBinding.inflate(getLayoutInflater());
    setContentView(bind.getRoot());
    setupInsets();
    new Handler(Looper.getMainLooper())
        .postDelayed(
            () -> {
              try {
                PluginManager.getInstance().setCurrentFileManagerActivity(this);
              } catch (Exception e) {
                e.printStackTrace();
              }
            },
            100);
    bind.headline.setBackground(ShapeUtil.shape(40f, this));
    viewModel = new ViewModelProvider(this).get(FileViewModel.class);
    adapter = new FileManagerAdapter(this);
    bind.rvfiles.setLayoutManager(new LinearLayoutManager(this));
    bind.rvfiles.setAdapter(adapter);
    bind.rvfiles.addItemDecoration(new MarginItemDecoration(this));

    adapter.setupSelectionTracker(bind.rvfiles);

    viewModel
        .getFiles()
        .observe(
            this,
            files -> {
              adapter.submitList(files);
              if (files == null || files.isEmpty()) {
                bind.emptystates.setVisibility(View.VISIBLE);
                bind.rvfiles.setVisibility(View.GONE);
              } else {
                bind.emptystates.setVisibility(View.GONE);
                bind.rvfiles.setVisibility(View.VISIBLE);
              }
            });
    viewModel
        .getIsLoading()
        .observe(
            this, loading -> bind.loadingprogass.setVisibility(loading ? View.VISIBLE : View.GONE));
    viewModel.savePath(true);
    viewModel
        .getCurrentPath()
        .observe(
            this,
            path -> {
              if (path != null) {
                bind.navmodel.setFile(new File(path));
              }
            });

    adapter.setOnItemClickListener(
        (item, pos) -> {
          if (item.isDirectory()) {
            viewModel.navigateTo(item.getPath());
          } else {
            setupClick(item.getPath(), item.getName());
          }
        });

    bind.fab.setOnClickListener(
        v -> startActivity(new Intent(FileManagerActivity.this, SettingActivity.class)));
    bind.navmodel
        .getAdapter()
        .setOnItemClickListener((view, nav, pos) -> viewModel.navigateTo(nav.getFilePath()));

    setupSelectionPanel();
    adapter.setSelectionStateListener(
        new FileManagerAdapter.SelectionStateListener() {
          @Override
          public void onSelectionChanged(int count) {

            if (count == 0 && pendingClipboard.isEmpty()) {
              if (selectionPanel != null) selectionPanel.setVisibility(View.GONE);
            } else if (count > 0) {
              selectionPanel.setVisibility(View.VISIBLE);
              selectionCount.setText(String.valueOf(count));
            } else if (count == 0 && !pendingClipboard.isEmpty()) {

              selectionCount.setText("0");
              selectionPanel.setVisibility(View.VISIBLE);
            }
          }

          @Override
          public void onSelectionModeStarted() {}

          @Override
          public void onSelectionModeEnded() {

            if (pendingClipboard.isEmpty() && selectionPanel != null) {
              selectionPanel.setVisibility(View.GONE);
            }
          }
        });

    setOnBackPress();
  }

  void setupClick(String path, String name) {
    int lastDot = name.lastIndexOf(".");
    String extension = (lastDot > 0) ? name.substring(lastDot).toLowerCase() : "";
    if (itemname.contains(extension)) {
      Intent intent = new Intent(FileManagerActivity.this, EditorActivity.class);
      intent.putExtra("file_path", path);
      intent.putExtra("file_name", name);
      startActivity(intent);
    } else {
      Toast.makeText(this, "فرمت فایل پشتیبانی نمی‌شود", Toast.LENGTH_SHORT).show();
    }
  }

  private void setupSelectionPanel() {
    selectionPanelBinding = bind.selectionPanel;
    selectionPanel = selectionPanelBinding.getRoot();

    selectionCount = selectionPanelBinding.txtSelectedCount;
    btnCopy = selectionPanelBinding.btnCopy;
    btnCut = selectionPanelBinding.btnCut;
    btnDelete = selectionPanelBinding.btnDelete;
    btnPaste = selectionPanelBinding.btnPaste;
    btnClose = selectionPanelBinding.btnClose;

    btnCopy.setOnClickListener(
        v -> {
          List<FileManagerModel> selected = adapter.getSelectedItems();
          if (!selected.isEmpty()) {
            pendingClipboard = new ArrayList<>(selected);
            isCutOperation = false;
            adapter.clearSelection();
            btnPaste.setColorFilter(0xff00ff00);

            selectionPanel.setVisibility(View.VISIBLE);
            selectionCount.setText("0");
          }
        });

    btnCut.setOnClickListener(
        v -> {
          List<FileManagerModel> selected = adapter.getSelectedItems();
          if (!selected.isEmpty()) {
            pendingClipboard = new ArrayList<>(selected);
            isCutOperation = true;
            adapter.clearSelection();
            btnPaste.setColorFilter(0xff00ff00);
            selectionPanel.setVisibility(View.VISIBLE);
            selectionCount.setText("0");
          }
        });
    btnDelete.setOnClickListener(
        v -> {
          List<FileManagerModel> selected = adapter.getSelectedItems();
          if (!selected.isEmpty()) {
            new MaterialAlertDialogBuilder(this)
                .setTitle("Delete")
                .setMessage("Delete " + selected.size() + " items?")
                .setPositiveButton(
                    "Delete",
                    (d, w) -> {
                      viewModel.deleteFiles(selected);
                      adapter.clearSelection();
                    })
                .setNegativeButton("Cancel", null)
                .show();
          }
        });

    btnPaste.setOnClickListener(
        v -> {
          if (pendingClipboard.isEmpty()) return;
          String currentDir = viewModel.getCurrentPath().getValue();
          if (currentDir != null) {
            viewModel.pasteFiles(
                pendingClipboard,
                currentDir,
                isCutOperation,
                success -> {
                  if (success) {
                    pendingClipboard.clear();
                    btnPaste.clearColorFilter();
                    adapter.clearSelection();
                    selectionPanel.setVisibility(View.GONE);
                    adapter.notifyDataSetChanged();

                  } else {
                    Toast.makeText(this, "Paste failed", Toast.LENGTH_SHORT).show();
                  }
                });
          }
        });

    btnPaste.setOnLongClickListener(
        v -> {
          adapter.selectAll();
          selectionCount.setText(String.valueOf(adapter.getSelectedItems().size()));
          if (selectionPanel.getVisibility() != View.VISIBLE) {
            selectionPanel.setVisibility(View.VISIBLE);
          }
          return true;
        });
    btnClose.setOnClickListener(
        v -> {
          pendingClipboard.clear();
          adapter.clearSelection();
          btnPaste.clearColorFilter();
          selectionPanel.setVisibility(View.GONE);
        });

    selectionPanel.setVisibility(View.GONE);
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
                      .setMessage("Exit Ghost IDE?")
                      .setNegativeButton("Yes", (c, f) -> finishAffinity())
                      .setPositiveButton("No", null)
                      .show();
                }
              }
            });
  }
}
