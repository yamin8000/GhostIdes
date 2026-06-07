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
import ir.hanzodev1375.ghostide.jgit.adapter.RemoteAdapter;
import ir.hanzodev1375.ghostide.jgit.dialogs.AddRemoteDialog;
import ir.hanzodev1375.ghostide.jgit.jgitandroid.datamanager.GitViewModel;
import ir.hanzodev1375.ghostide.jgit.jgitandroid.model.RemoteInfo;

public class RemotesFragment extends Fragment {
  private GitViewModel viewModel;
  private RemoteAdapter adapter;

  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_remotes, container, false);
  }

  @Override
  public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
    viewModel = new ViewModelProvider(requireActivity()).get(GitViewModel.class);
    RecyclerView recyclerView = view.findViewById(R.id.recyclerViewRemotes);
    recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    adapter = new RemoteAdapter();
    recyclerView.setAdapter(adapter);

    viewModel.remotes.observe(getViewLifecycleOwner(), adapter::submitList);

    adapter.setOnRemoteActionListener(
        new RemoteAdapter.OnRemoteActionListener() {
          @Override
          public void onRemove(RemoteInfo remote) {
            viewModel.removeRemote(remote.getName());
          }

          @Override
          public void onPushPull(RemoteInfo remote) {
            /* می‌توان دیالوگ Push/Pull باز کرد */
          }
        });

    view.findViewById(R.id.btnAddRemote).setOnClickListener(v -> showAddRemoteDialog());

    viewModel.operationResult.observe(
        getViewLifecycleOwner(),
        result -> {
          if (result != null)
            Toast.makeText(getContext(), result.getMessage(), Toast.LENGTH_SHORT).show();
        });
  }

  private void showAddRemoteDialog() {
    AddRemoteDialog dialog = new AddRemoteDialog();
    dialog.setOnRemoteAddListener((name, url) -> viewModel.addRemote(name, url));
    dialog.show(getChildFragmentManager(), "addRemote");
  }
}
