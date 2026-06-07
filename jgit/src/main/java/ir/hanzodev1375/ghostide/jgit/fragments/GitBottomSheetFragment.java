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

import ir.hanzodev1375.ghostide.jgit.R; 
import ir.hanzodev1375.ghostide.jgit.jgitandroid.datamanager.GitViewModel;
import ir.hanzodev1375.ghostide.jgit.jgitandroid.RepositoryStatus;
import ir.hanzodev1375.ghostide.jgit.adapter.ViewPagerAdapter;
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

  public GitBottomSheetFragment() {
    
  }

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
          if (status == RepositoryStatus.ERROR) {
            Toast.makeText(getContext(), "Git repository error", Toast.LENGTH_LONG).show();
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
    List<String> tabTitles = new ArrayList<>();
    tabTitles.add("Changes");
    tabTitles.add("History");
    tabTitles.add("Branches");
    tabTitles.add("Remotes"); 

    ViewPager2 viewPager = root.findViewById(R.id.viewPager);
    ViewPagerAdapter adapter = new ViewPagerAdapter(requireActivity(), tabTitles);
    viewPager.setAdapter(adapter);

    TabLayout tabLayout = root.findViewById(R.id.tabLayout);
    new TabLayoutMediator(
            tabLayout, viewPager, (tab, position) -> tab.setText(tabTitles.get(position)))
        .attach();
}
}
