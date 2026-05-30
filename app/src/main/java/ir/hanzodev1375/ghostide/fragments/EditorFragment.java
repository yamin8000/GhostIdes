package ir.hanzodev1375.ghostide.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import ir.hanzodev1375.ghostide.codeeditors.IdeEditor;
import ir.hanzodev1375.ghostide.codeeditors.langs.cpp.CppLanguage;
import ir.hanzodev1375.ghostide.codeeditors.langs.java.JavaLanguage;
import ir.hanzodev1375.ghostide.databinding.EditorFragmentBinding;
import ir.hanzodev1375.ghostide.mvvm.viewmodel.EditorViewModel;
import io.github.rosemoe.sora.widget.schemes.SchemeDarcula;
import ir.theme.ThemeManager;
import ir.theme.ThemeUtils;

public class EditorFragment extends Fragment {
  private EditorFragmentBinding binding;
  private EditorViewModel viewModel;
  private IdeEditor editor;
  private String filePath;
  private ThemeUtils theme;

  public static EditorFragment newInstance(String path) {
    EditorFragment f = new EditorFragment();
    Bundle args = new Bundle();
    args.putString("file_path", path);
    f.setArguments(args);
    return f;
  }

  @Override
  public View onCreateView(
      LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    binding = EditorFragmentBinding.inflate(inflater, container, false);
    return binding.getRoot();
  }

  @Override
  public void onViewCreated(View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    filePath = getArguments().getString("file_path");
    viewModel = new ViewModelProvider(this).get(EditorViewModel.class);
    editor = binding.editor;
    var manager = new ThemeManager(requireActivity());
    theme = new ThemeUtils(manager);
    theme.applyEditor(editor);
    viewModel
        .getLoading()
        .observe(
            getViewLifecycleOwner(),
            loading -> {
              binding.prograssLoading.setVisibility(loading ? View.VISIBLE : View.GONE);
            });

    viewModel
        .getText()
        .observe(
            getViewLifecycleOwner(),
            content -> {
              if (content != null) editor.setText(content);
            });

    if (filePath != null) viewModel.loadFile(filePath);
    if (filePath.endsWith(".java")) {
      editor.setEditorLanguage(new JavaLanguage());
    }else if(filePath.endsWith(".cpp")) {
    	editor.setEditorLanguage(new CppLanguage());
    }
  //  editor.setColorScheme(new SchemeDarcula());
  }

  @Override
  public void onDestroyView() {
    if (viewModel != null && editor != null) viewModel.saveFile(editor.getText());
    super.onDestroyView();
    binding = null;
  }
  public IdeEditor getEditor(){
    return editor;
  }
}