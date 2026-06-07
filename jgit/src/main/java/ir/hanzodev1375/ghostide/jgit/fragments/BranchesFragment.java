package ir.hanzodev1375.ghostide.jgit.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import ir.hanzodev1375.ghostide.jgit.R;
import ir.hanzodev1375.ghostide.jgit.adapter.BranchAdapter;
import ir.hanzodev1375.ghostide.jgit.jgitandroid.datamanager.GitViewModel;

public class BranchesFragment extends Fragment {
  private GitViewModel viewModel;
  private BranchAdapter adapter;

  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_branches, container, false);
  }

  @Override
  public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
    viewModel = new ViewModelProvider(requireActivity()).get(GitViewModel.class);
    RecyclerView recyclerView = view.findViewById(R.id.recyclerViewBranches);
    recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    adapter = new BranchAdapter();
    recyclerView.setAdapter(adapter);

    viewModel.branches.observe(
        getViewLifecycleOwner(),
        list -> {
          String current = viewModel.currentBranch.getValue();
          adapter.submitList(list, current);
        });

    adapter.setOnBranchActionListener(
        new BranchAdapter.OnBranchActionListener() {
          @Override
          public void onCheckout(String branchName) {
            viewModel.checkoutBranch(branchName);
          }

          @Override
          public void onDelete(String branchName) {
            viewModel.deleteBranch(branchName);
          }
        });

    EditText etNewBranch = view.findViewById(R.id.editBranchName);
    view.findViewById(R.id.btnCreateBranch)
        .setOnClickListener(
            v -> {
              String name = etNewBranch.getText().toString().trim();
              if (!name.isEmpty()) viewModel.createBranch(name);
              else Toast.makeText(getContext(), "Enter branch name", Toast.LENGTH_SHORT).show();
            });

    viewModel.operationResult.observe(
        getViewLifecycleOwner(),
        result -> {
          if (result != null)
            Toast.makeText(getContext(), result.getMessage(), Toast.LENGTH_SHORT).show();
        });
  }
}
