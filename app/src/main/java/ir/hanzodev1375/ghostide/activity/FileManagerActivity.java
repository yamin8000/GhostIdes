package ir.hanzodev1375.ghostide.activity;

import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.OnBackPressedCallback;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.bumptech.glide.Glide;
import com.google.android.material.color.MaterialColors;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.skydoves.powermenu.MenuAnimation;
import com.skydoves.powermenu.PowerMenu;
import com.skydoves.powermenu.PowerMenuItem;
import ir.ghostide.logcat.BottomSheetLogView;
import ir.hanzodev1375.components.RenameDialogFragment;
import ir.hanzodev1375.components.TextInputDialogFragment;
import ir.hanzodev1375.components.ui.ProfileView;
import ir.hanzodev1375.ghostide.adapters.FileManagerAdapter;
import ir.hanzodev1375.ghostide.adapters.ToolbarAdapter;
import ir.hanzodev1375.ghostide.adapters.ZipBrowserAdapter;
import ir.hanzodev1375.ghostide.ai.chat.AiChatActivity;
import ir.hanzodev1375.ghostide.databinding.ActivityFilemanagerBinding;
import ir.hanzodev1375.ghostide.databinding.SelectionPanelBinding;
import ir.hanzodev1375.ghostide.dialogs.CopyProgressDialog;
import ir.hanzodev1375.ghostide.dialogs.DeleteProgressDialog;
import ir.hanzodev1375.ghostide.jgit.GitHubClient;
import ir.hanzodev1375.ghostide.jgit.GitHubProfileSheet;
import ir.hanzodev1375.ghostide.jgit.fragments.GitBottomSheetFragment;
import ir.hanzodev1375.ghostide.models.FileManagerModel;
import ir.hanzodev1375.ghostide.models.ZipEntryModel;
import ir.hanzodev1375.ghostide.mvvm.viewmodel.FileViewModel;
import ir.hanzodev1375.ghostide.plugin.PluginManager;
import ir.hanzodev1375.ghostide.utils.MarginItemDecoration;
import ir.hanzodev1375.ghostide.utils.NetworkChangeReceiver;
import ir.hanzodev1375.ghostide.utils.ShapeUtil;
import ir.theme.themeeditor.ThemeEditorActivity;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import ir.hanzodev1375.ghostide.R;
import java.util.Set;
import ninja.coder.appuploader.main.appupdate.UpadteAppView;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;

public class FileManagerActivity extends BaseCompat
    implements NetworkChangeReceiver.CallBackNetWork {

  private ActivityFilemanagerBinding bind;
  private FileViewModel viewModel;
  private FileManagerAdapter adapter;
  private ZipBrowserAdapter zipAdapter;
  private View selectionPanel;
  private TextView selectionCount;
  private ImageView btnCopy, btnCut, btnDelete, btnPaste, btnClose, btnSelectall;
  private boolean isCutOperation = false;
  private List<FileManagerModel> pendingClipboard = new ArrayList<>();
  private SelectionPanelBinding selectionPanelBinding;
  private FileManagerModel fileModels;
  private UpadteAppView app;
  private ProfileView profileview;
  private NetworkChangeReceiver networkChangeReceiver;
  private Set<String> itemname =
      new HashSet<>(Arrays.asList(".html", ".java", ".cpp", ".css", ".js", ".py", ".json"));
  private CopyProgressDialog copyProgressDialog;
  private DeleteProgressDialog deleteProgressDialog;
  private boolean isZipMode = false;
  private String currentZipFilePath = null;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    bind = ActivityFilemanagerBinding.inflate(getLayoutInflater());
    setContentView(bind.getRoot());
    setupInsets();
    setupSearchLayoutInsets();

    networkChangeReceiver = new NetworkChangeReceiver(this);
    IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
    this.registerReceiver(networkChangeReceiver, filter);
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
    app = new UpadteAppView(this, bind.downloader, () -> {});
    stepSearch();
    adapter.setupSelectionTracker(bind.rvfiles);

    viewModel
        .getFiles()
        .observe(
            this,
            files -> {
              if (files != null && !files.isEmpty()) fileModels = files.get(0);
              adapter.submitList(new ArrayList<>(files));
              bind.rvfiles.post(() -> adapter.notifyDataSetChanged());
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
              if (path != null) bind.navmodel.setFile(new File(path));
            });

    copyProgressDialog = new CopyProgressDialog(this);
    viewModel
        .getCopyProgress()
        .observe(
            this,
            progress -> {
              if (progress == null) return;
              if (progress.isRunning) {
                if (!copyProgressDialog.isShowing()) copyProgressDialog.show();
                copyProgressDialog.update(progress);
              } else {
                copyProgressDialog.dismiss();
              }
            });

    deleteProgressDialog = new DeleteProgressDialog(this);
    viewModel
        .getDeleteProgress()
        .observe(
            this,
            progress -> {
              if (progress == null) return;
              if (progress.isRunning) {
                if (!deleteProgressDialog.isShowing()) deleteProgressDialog.show();
                deleteProgressDialog.update(progress);
              } else {
                deleteProgressDialog.dismiss();
              }
            });

    adapter.setOnItemClickListener(
        (item, pos) -> {
          if (item.isDirectory()) {
            viewModel.navigateTo(item.getPath());
          } else if (item.getPath().toLowerCase().endsWith(".zip")) {
            enterZipMode(item.getPath());
          } else {
            setupClick(item.getPath(), item.getName());
          }
          String currentPath = viewModel.getCurrentPath().getValue();
          if (currentPath != null) {
            bind.gitActionButton.setVisibility(
                isGitRepository(currentPath) ? View.VISIBLE : View.GONE);
          }
          if (bind.ser.isShow()) {
            bind.ser.hide();
            bind.fab.setVisibility(View.VISIBLE);
            bind.ser.setQuery("");
          }
        });

    List<Integer> listIcon = new ArrayList<>();
    listIcon.add(R.drawable.folder);
    listIcon.add(R.drawable.ic_fileicon);
    bind.fab
        .getRecyclerView()
        .setAdapter(
            new ToolbarAdapter(
                listIcon,
                (view2, mypos) -> {
                  switch (mypos) {
                    case 0 -> creatorFolder(fileModels);
                    case 1 -> creatorFile(fileModels);
                  }
                }));

    bind.fab
        .getFab()
        .setOnClickListener(
            v -> {
              if (!bind.fab.isExpanded()) bind.fab.expand();
              else bind.fab.collapse();
            });

    bind.navmodel
        .getAdapter()
        .setOnItemClickListener((view, nav, pos) -> viewModel.navigateTo(nav.getFilePath()));

    stepMoreAdapter();
    setupSelectionPanel();

    adapter.setSelectionStateListener(
        new FileManagerAdapter.SelectionStateListener() {
          @Override
          public void onSelectionChanged(int count) {
            if (count == 0 && pendingClipboard.isEmpty()) {
              if (selectionPanel != null) selectionPanel.setVisibility(View.GONE);
            } else if (count > 0) {
              selectionPanel.setVisibility(View.VISIBLE);
              selectionCount.setText(getString(R.string.selected_items_count, count));
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

    bind.buttonAi.setOnClickListener(
        v -> startActivity(new Intent(getApplicationContext(), AiChatActivity.class)));

    setOnBackPress();
    setupGitButton();
    observePathForGit();
    initZipBrowserAdapter();
  }

  private void initZipBrowserAdapter() {
    zipAdapter = new ZipBrowserAdapter(this);
    zipAdapter.setZipLoadListener(
        new ZipBrowserAdapter.ZipLoadListener() {
          @Override
          public void onLoadStarted() {
            bind.loadingprogass.setVisibility(View.VISIBLE);
          }

          @Override
          public void onLoadFinished(String internalPath, boolean hasParent) {
            bind.loadingprogass.setVisibility(View.GONE);
          }

          @Override
          public void onLoadError(String message) {
            bind.loadingprogass.setVisibility(View.GONE);
            Toast.makeText(FileManagerActivity.this, "خطا: " + message, Toast.LENGTH_SHORT).show();
            exitZipMode();
          }
        });
    zipAdapter.setOnItemClickListener(
        (item, position) -> {
          if (item.isDirectory()) {
            zipAdapter.loadZip(currentZipFilePath, item.getEntryPath());
          } else {
            extractAndOpenZipEntry(item);
          }
        });
    zipAdapter.setOnMoreClickListener(
        (item, anchor, pos) -> {
          PowerMenu menu = new PowerMenu.Builder(anchor.getContext()).setIsMaterial(true).build();
          menu.addItem(new PowerMenuItem(getString(R.string.removed)));
          menu.addItem(new PowerMenuItem(getString(R.string.rename)));
          menu.setMenuColor(
              MaterialColors.getColor(
                  anchor.getContext(), com.google.android.material.R.attr.colorSurface, 0));
          menu.setTextColor(
              MaterialColors.getColor(
                  anchor.getContext(), com.google.android.material.R.attr.colorOnSurface, 0));
          menu.setShowBackground(false);
          menu.setAutoDismiss(true);
          menu.setMenuRadius(30f);
          menu.setAnimation(MenuAnimation.FADE);
          menu.setOnMenuItemClickListener(
              (index, menuItem) -> {
                if (index == 0) {
                  new MaterialAlertDialogBuilder(FileManagerActivity.this)
                      .setTitle(getString(R.string.removed))
                      .setMessage(getString(R.string.removedmassges, item.getName() + "?"))
                      .setPositiveButton(getString(R.string.ok), (d, w) -> {})
                      .setNegativeButton(getString(R.string.cancel), null)
                      .show();
                } else if (index == 1) {
                  Toast.makeText(
                          FileManagerActivity.this,
                          "Rename in ZIP not supported",
                          Toast.LENGTH_SHORT)
                      .show();
                }
              });
          int[] location = new int[2];
          anchor.getLocationOnScreen(location);
          int x = location[0];
          int y = location[1];
          var dm = anchor.getResources().getDisplayMetrics();
          int screenHeight = dm.heightPixels;
          int menuHeight = menu.getContentViewHeight();
          if (menuHeight <= 0) menuHeight = 200;
          int spaceBelow = screenHeight - (y + anchor.getHeight());
          int spaceAbove = y;
          if (spaceBelow < menuHeight && spaceAbove > spaceBelow) y -= menuHeight;
          else y += anchor.getHeight();
          menu.showAtLocation(anchor, Gravity.TOP | Gravity.START, x, y);
        });
    zipAdapter.setSelectionStateListener(
        new ZipBrowserAdapter.SelectionStateListener() {
          @Override
          public void onSelectionChanged(int count) {
            if (count == 0 && pendingClipboard.isEmpty()) {
              if (selectionPanel != null) selectionPanel.setVisibility(View.GONE);
            } else if (count > 0) {
              selectionPanel.setVisibility(View.VISIBLE);
              selectionCount.setText(getString(R.string.selected_items_count, count));
            } else if (count == 0 && !pendingClipboard.isEmpty()) {
              selectionCount.setText("0");
              selectionPanel.setVisibility(View.VISIBLE);
            }
          }

          @Override
          public void onSelectionModeStarted() {}

          @Override
          public void onSelectionModeEnded() {}
        });
  }

  private void enterZipMode(String zipFilePath) {
    isZipMode = true;
    currentZipFilePath = zipFilePath;
    bind.rvfiles.setAdapter(zipAdapter);
    zipAdapter.setupSelectionTracker(bind.rvfiles);
    zipAdapter.loadZip(zipFilePath, "");
    bind.fab.setVisibility(View.GONE);
    bind.gitActionButton.setVisibility(View.GONE);
    bind.navmodel.setVisibility(View.GONE);
  }

  private void exitZipMode() {
    isZipMode = false;
    currentZipFilePath = null;
    bind.rvfiles.setAdapter(adapter);
    adapter.setupSelectionTracker(bind.rvfiles);
    viewModel.loadFiles(viewModel.getCurrentPath().getValue());
    bind.fab.setVisibility(View.VISIBLE);
    bind.navmodel.setVisibility(View.VISIBLE);
    String currentPath = viewModel.getCurrentPath().getValue();
    if (currentPath != null) {
      bind.gitActionButton.setVisibility(isGitRepository(currentPath) ? View.VISIBLE : View.GONE);
    }
  }

  private void extractAndOpenZipEntry(ZipEntryModel entry) {
    File cacheDir = new File(getCacheDir(), "zip_extract");
    if (!cacheDir.exists()) cacheDir.mkdirs();
    File outFile = new File(cacheDir, entry.getName());
    new Thread(
            () -> {
              try (ZipFile zipFile = new ZipFile(entry.getParentZipPath())) {
                zipFile.extractFile(
                    entry.getEntryPath(), cacheDir.getAbsolutePath(), entry.getName());
                runOnUiThread(
                    () -> {
                      Intent intent = new Intent(FileManagerActivity.this, EditorActivity.class);
                      intent.putExtra("file_path", outFile.getAbsolutePath());
                      intent.putExtra("file_name", entry.getName());
                      startActivity(intent);
                    });
              } catch (Exception e) {
                runOnUiThread(
                    () ->
                        Toast.makeText(
                                FileManagerActivity.this, "خطا در استخراج فایل", Toast.LENGTH_SHORT)
                            .show());
              }
            })
        .start();
  }

  private void setupSearchLayoutInsets() {
    bind.fab.post(
        () -> {
          ViewCompat.setOnApplyWindowInsetsListener(
              bind.ser,
              (view, insets) -> {
                int imeHeight = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom;
                int fabBottomMargin = getFabBottomMargin();
                int targetBottomMargin;
                if (imeHeight > 0) {
                  targetBottomMargin = imeHeight;
                } else {
                  targetBottomMargin =
                      fabBottomMargin + (int) (8 * getResources().getDisplayMetrics().density);
                }
                ViewGroup.MarginLayoutParams params =
                    (ViewGroup.MarginLayoutParams) view.getLayoutParams();
                params.bottomMargin = targetBottomMargin;
                view.setLayoutParams(params);
                return insets;
              });
          ViewCompat.requestApplyInsets(bind.ser);
        });
  }

  private int getFabBottomMargin() {
    int fabBottom = bind.fab.getBottom();
    int screenHeight = bind.fab.getRootView().getHeight();
    return screenHeight - fabBottom;
  }

  void setupClick(String path, String name) {
    int lastDot = name.lastIndexOf(".");
    String extension = (lastDot > 0) ? name.substring(lastDot).toLowerCase() : "";
    if (itemname.contains(extension)) {
      Intent intent = new Intent(FileManagerActivity.this, EditorActivity.class);
      intent.putExtra("file_path", path);
      intent.putExtra("file_name", name);
      startActivity(intent);
    } else if (path.endsWith(".gth")) {
      Intent i = new Intent(FileManagerActivity.this, ThemeEditorActivity.class);
      i.putExtra(ThemeEditorActivity.EXTRA_THEME_PATH, path);
      startActivity(i);
    } else {
      Toast.makeText(this, getString(R.string.error_file_format_not_supported), Toast.LENGTH_SHORT)
          .show();
    }
  }

  private void setupGitButton() {
    bind.gitActionButton.setOnClickListener(
        v -> {
          String repoPath = findGitRepositoryPath();
          if (repoPath == null) {
            Toast.makeText(this, "Git dir not found", Toast.LENGTH_LONG).show();
            return;
          }
          GitBottomSheetFragment.newInstance(repoPath)
              .show(getSupportFragmentManager(), "git_bottom_sheet");
        });
  }

  private String findGitRepositoryPath() {
    String currentDir = viewModel.getCurrentPath().getValue();
    if (currentDir == null) return null;
    File dir = new File(currentDir);
    while (dir != null) {
      File gitDir = new File(dir, ".git");
      if (gitDir.exists() && gitDir.isDirectory()) return dir.getAbsolutePath();
      dir = dir.getParentFile();
    }
    return null;
  }

  private void observePathForGit() {
    viewModel
        .getCurrentPath()
        .observe(
            this,
            path -> {
              if (path != null && isGitRepository(path)) {
                bind.gitActionButton.setVisibility(View.VISIBLE);
              } else {
                bind.gitActionButton.setVisibility(View.GONE);
              }
            });
  }

  private boolean isGitRepository(String path) {
    File gitDir = new File(path, ".git");
    return gitDir.exists() && gitDir.isDirectory();
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
    btnSelectall = selectionPanelBinding.btnSelectall;
    selectionPanelBinding.getRoot().setBackground(ShapeUtil.shapeCustomView(this));

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
            copyProgressDialog.setMoveMode(isCutOperation);
            viewModel.pasteFiles(
                pendingClipboard,
                currentDir,
                isCutOperation,
                success -> {
                  pendingClipboard.clear();
                  btnPaste.clearColorFilter();
                  adapter.clearSelection();
                  selectionPanel.setVisibility(View.GONE);
                  adapter.notifyDataSetChanged();
                  if (!success) Toast.makeText(this, "Paste failed", Toast.LENGTH_SHORT).show();
                });
          }
        });

    btnSelectall.setOnClickListener(
        v -> {
          adapter.selectAll();
          selectionCount.setText(
              getString(R.string.selected_items_count, adapter.getSelectedItems().size()));
          if (selectionPanel.getVisibility() != View.VISIBLE) {
            selectionPanel.setVisibility(View.VISIBLE);
          }
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
          int imeBottom = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom;
          bind.headtop.setPadding(0, systemBars.top, 0, 0);
          bind.fab.post(
              () -> {
                int fabSpace = bind.fab.getHeight() + 48;
                int extraBottom = (imeBottom > 0) ? imeBottom : 0;
                bind.rvfiles.setPadding(
                    bind.rvfiles.getPaddingLeft(),
                    bind.rvfiles.getPaddingTop(),
                    bind.rvfiles.getPaddingRight(),
                    systemBars.bottom + fabSpace + extraBottom);
              });
          return insets;
        });
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    bind = null;
    this.unregisterReceiver(networkChangeReceiver);
  }

  private void setOnBackPress() {
    getOnBackPressedDispatcher()
        .addCallback(
            this,
            new OnBackPressedCallback(true) {
              @Override
              public void handleOnBackPressed() {
                if (isZipMode) {
                  if (!zipAdapter.navigateUp()) {
                    exitZipMode();
                  }
                } else {
                  if (viewModel.getCurrentPath().getValue() != null
                      && !viewModel.getCurrentPath().getValue().equals("/storage/emulated/0")) {
                    viewModel.navigateUp();
                    String currentPath = viewModel.getCurrentPath().getValue();
                    if (currentPath != null) {
                      bind.gitActionButton.setVisibility(
                          isGitRepository(currentPath) ? View.VISIBLE : View.GONE);
                    }
                  } else {
                    new MaterialAlertDialogBuilder(FileManagerActivity.this)
                        .setTitle(getString(R.string.dialog_exit_title))
                        .setMessage(getString(R.string.dialog_exit_message))
                        .setNegativeButton(getString(R.string.ok), (c, f) -> finishAffinity())
                        .setPositiveButton(getString(R.string.cancel), null)
                        .show();
                  }
                }
              }
            });
  }

  void stepMoreAdapter() {
    adapter.setOnMoreClickListener(
        (filemodel, view, pos) -> {
          PowerMenu menu = new PowerMenu.Builder(view.getContext()).setIsMaterial(true).build();
          menu.addItem(new PowerMenuItem(getString(R.string.removed)));
          menu.addItem(new PowerMenuItem(getString(R.string.rename)));
          menu.setMenuColor(
              MaterialColors.getColor(
                  view.getContext(), com.google.android.material.R.attr.colorSurface, 0));
          menu.setTextColor(
              MaterialColors.getColor(
                  view.getContext(), com.google.android.material.R.attr.colorOnSurface, 0));
          menu.setShowBackground(false);
          menu.setAutoDismiss(true);
          menu.setMenuRadius(30f);
          menu.setAnimation(MenuAnimation.FADE);
          menu.setOnMenuItemClickListener(
              (index, item) -> {
                switch (index) {
                  case 0 -> removedItem(filemodel);
                  case 1 -> renameItem(filemodel);
                }
              });
          int[] location = new int[2];
          view.getLocationOnScreen(location);
          int x = location[0];
          int y = location[1];
          var dm = view.getResources().getDisplayMetrics();
          int screenHeight = dm.heightPixels;
          int menuHeight = menu.getContentViewHeight();
          if (menuHeight <= 0) menuHeight = 200;
          int spaceBelow = screenHeight - (y + view.getHeight());
          int spaceAbove = y;
          if (spaceBelow < menuHeight && spaceAbove > spaceBelow) y -= menuHeight;
          else y += view.getHeight();
          menu.showAtLocation(view, Gravity.TOP | Gravity.START, x, y);
        });
  }

  void renameItem(FileManagerModel model) {
    RenameDialogFragment dialog =
        RenameDialogFragment.getInstance(
            model.getName(),
            (prefix, extension) -> {
              String displayName =
                  !TextUtils.isEmpty(extension) ? prefix + "." + extension : prefix;
              viewModel.renameFile(model, displayName);
            });
    dialog.show(getSupportFragmentManager(), RenameDialogFragment.TAG);
  }

  void removedItem(FileManagerModel model) {
    new MaterialAlertDialogBuilder(this)
        .setTitle(getString(R.string.removed))
        .setMessage(getString(R.string.removedmassges, model.getName() + "?"))
        .setPositiveButton(getString(R.string.ok), (d, w) -> viewModel.deleteFile(model))
        .setNegativeButton(getString(R.string.cancel), null)
        .show();
  }

  void creatorFile(FileManagerModel model) {
    TextInputDialogFragment.newInstance(
            getString(R.string.dialog_create_file_title),
            getString(R.string.dialog_create_file_hint),
            null)
        .setCallback(text -> viewModel.createFile(text))
        .show(getSupportFragmentManager(), null);
  }

  void creatorFolder(FileManagerModel model) {
    TextInputDialogFragment.newInstance(
            getString(R.string.dialog_create_folder_title),
            getString(R.string.dialog_create_folder_hint),
            null)
        .setCallback(text -> viewModel.createFolder(text))
        .show(getSupportFragmentManager(), null);
  }

  private void setupHeader() {
    GitHubClient gitHub = new GitHubClient(this);
    if (gitHub.isLoggedIn()) {
      bind.userNameText.setText(gitHub.getName());
      profileview = new ProfileView(this);
      profileview.bindImageView(bind.userAvatar, gitHub.getAvatarUrl(), R.drawable.user);
      Glide.with(this)
          .load(gitHub.getAvatarUrl())
          .circleCrop()
          .placeholder(R.drawable.user)
          .into(bind.userAvatar);
    } else {
      bind.userNameText.setText(getString(R.string.github_account_not_logged_in));
      bind.userAvatar.setImageResource(R.drawable.user);
    }
    bind.userAvatar.setOnClickListener(
        v -> {
          if (gitHub.isLoggedIn()) {
            GitHubProfileSheet.newInstance().show(getSupportFragmentManager(), "github_profile");
          } else {
            new MaterialAlertDialogBuilder(v.getContext())
                .setTitle(getString(R.string.github_tokenerrortitle))
                .setMessage(getString(R.string.github_tokenerrormsg))
                .setPositiveButton(
                    getString(R.string.ok),
                    (c, e) -> {
                      Intent i = new Intent(getApplicationContext(), SettingActivity.class);
                      i.putExtra("open_section", "githublogin");
                      startActivity(i);
                    })
                .setNegativeButton(getString(R.string.cancel), null)
                .show();
          }
        });
    bind.btnSettings.setOnClickListener(v -> stepButton());
  }

  @Override
  protected void onResume() {
    super.onResume();
    setupHeader();
    if (!isZipMode) {
      String currentPath = viewModel.getCurrentPath().getValue();
      if (currentPath != null) {
        bind.gitActionButton.setVisibility(isGitRepository(currentPath) ? View.VISIBLE : View.GONE);
      }
    }
  }

  void stepButton() {
    var menu = new PowerMenu.Builder(this).build();
    menu.addItem(new PowerMenuItem(getString(R.string.settings_title)));
    menu.addItem(new PowerMenuItem(getString(R.string.search_hint)));
    menu.addItem(new PowerMenuItem(getString(R.string.openlogcat)));
    menu.setAutoDismiss(true);
    menu.setShowBackground(false);
    menu.setAnimation(MenuAnimation.FADE);
    menu.setTextColor(
        MaterialColors.getColor(this, com.google.android.material.R.attr.colorOnSurface, 0));
    menu.setMenuColor(
        MaterialColors.getColor(this, com.google.android.material.R.attr.colorSurface, 0));
    menu.setOnMenuItemClickListener(
        (c, f) -> {
          switch (c) {
            case 0 -> startActivity(new Intent(getApplicationContext(), SettingActivity.class));
            case 1 -> {
              if (!bind.ser.isShow()) {
                bind.ser.show();
                bind.fab.setVisibility(View.GONE);
              } else {
                bind.ser.hide();
                bind.fab.setVisibility(View.VISIBLE);
              }
            }
            case 2 -> {
              var log = new BottomSheetLogView();
              log.show(getSupportFragmentManager(), "log");
            }
          }
        });
    menu.showAsDropDown(bind.btnSettings);
  }

  void stepSearch() {
    bind.ser.setOnTextChangedListener(
        qer -> {
          if (qer.length() > 0) adapter.search(qer);
          else adapter.search("");
        });
    bind.ser.setIconClose(R.drawable.ic_close);
    bind.ser.setIconSearch(R.drawable.outline_search);
  }

  @Override
  public void ConnectionNOT() {}

  @Override
  public void ConnectionIS() {
    app.init();
  }
}
