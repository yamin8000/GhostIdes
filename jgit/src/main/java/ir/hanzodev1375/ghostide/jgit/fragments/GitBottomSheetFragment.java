package ir.hanzodev1375.ghostide.jgit.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import ir.hanzodev1375.ghostide.codeeditors.setting.PreferencesUtils;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import android.widget.EditText;
import ir.hanzodev1375.ghostide.jgit.R;
import ir.hanzodev1375.ghostide.jgit.jgitandroid.datamanager.GitViewModel;
import ir.hanzodev1375.ghostide.jgit.jgitandroid.RepositoryStatus;
import ir.hanzodev1375.ghostide.jgit.adapter.ViewPagerAdapter;
import ir.hanzodev1375.ghostide.jgit.model.GitTab;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class GitBottomSheetFragment extends BottomSheetDialogFragment {

  private GitViewModel viewModel;
  private ProgressBar progressBar;
  private String repoPath;
  private boolean isInitialized = false;

  public void setRepoPath(String repoPath) {
    this.repoPath = repoPath;
  }

  public GitBottomSheetFragment() {}

  public static GitBottomSheetFragment newInstance(String repoPath) {
    GitBottomSheetFragment fragment = new GitBottomSheetFragment();
    Bundle args = new Bundle();
    args.putString("repo_path", repoPath);
    fragment.setArguments(args);
    return fragment;
  }

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if (getArguments() != null) {
      repoPath = getArguments().getString("repo_path");
    }
  }

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {

    return inflater.inflate(R.layout.bottom_sheet_git, container, false);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    viewModel = new ViewModelProvider(requireActivity()).get(GitViewModel.class);

    progressBar = view.findViewById(R.id.progressBar);

    viewModel.progressMessage.observe(
        getViewLifecycleOwner(),
        msg -> {
          progressBar.setVisibility(msg != null ? View.VISIBLE : View.GONE);
          if (msg != null) {
            Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
          }
        });

    viewModel.repositoryStatus.observe(
        getViewLifecycleOwner(),
        status -> {
          if (status == RepositoryStatus.OPENED || status == RepositoryStatus.INITIALIZED) {
            PreferencesUtils prefsUtils = new PreferencesUtils(requireContext());
            if (prefsUtils.hasGitLocalUserConfig()) {
              viewModel.setUserConfig(
                  prefsUtils.getGitLocalUserName(), prefsUtils.getGitLocalUserEmail());
            } else {
              showUserConfigDialog();
            }
          }
        });

    setupViewPager(view);

    if (!isInitialized && repoPath != null && !repoPath.isEmpty()) {
      File gitDir = new File(repoPath, ".git");
      if (gitDir.exists() && gitDir.isDirectory()) {
        viewModel.openExistingRepository(repoPath);
      } else {
        viewModel.initializeRepository(repoPath);
      }
      isInitialized = true;
    } else if (repoPath == null || repoPath.isEmpty()) {
      Toast.makeText(getContext(), "مسیر مخزن تنظیم نشده است!", Toast.LENGTH_SHORT).show();
      dismiss();
    }
  }

  private void setupViewPager(View root) {
    List<GitTab> tabs = new ArrayList<>();
    tabs.add(new GitTab("Changes", new ChangedFilesFragment()));
    tabs.add(new GitTab("History", new CommitHistoryFragment()));
    tabs.add(new GitTab("Branches", new BranchesFragment()));
    tabs.add(new GitTab("Remotes", new RemotesFragment()));
    tabs.add(new GitTab("Stash", new StashFragment()));
    tabs.add(new GitTab("Conflicts", new ConflictResolverFragment()));
    tabs.add(new GitTab("Diff", new DiffViewerFragment()));

    ViewPager2 viewPager = root.findViewById(R.id.viewPager);
    ViewPagerAdapter adapter = new ViewPagerAdapter(requireActivity(), tabs);
    viewPager.setAdapter(adapter);

    TabLayout tabLayout = root.findViewById(R.id.tabLayout);
    new TabLayoutMediator(
            tabLayout, viewPager, (tab, position) -> tab.setText(tabs.get(position).getTitle()))
        .attach();
  }

  private void showUserConfigDialog() {
    PreferencesUtils prefsUtils = new PreferencesUtils(requireContext());

    MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
    builder.setTitle("Git User Configuration");

    View view = LayoutInflater.from(requireContext()).inflate(R.layout.git_local_config, null);
    EditText etName = view.findViewById(R.id.etGitUserName);
    EditText etEmail = view.findViewById(R.id.etGitUserEmail);

    etName.setText(prefsUtils.getGitLocalUserName());
    etEmail.setText(prefsUtils.getGitLocalUserEmail());

    builder.setView(view);
    builder.setPositiveButton(
        "Save",
        (d, w) -> {
          String name = etName.getText().toString().trim();
          String email = etEmail.getText().toString().trim();
          if (name.isEmpty() || email.isEmpty()) {
            Toast.makeText(getContext(), "Name and email required", Toast.LENGTH_SHORT).show();
            return;
          }
          prefsUtils.setGitLocalUserName(name);
          prefsUtils.setGitLocalUserEmail(email);
          viewModel.setUserConfig(name, email);
          Toast.makeText(getContext(), "Saved", Toast.LENGTH_SHORT).show();
        });
    builder.setNegativeButton("Cancel", null);
    builder.show();
  }
}
