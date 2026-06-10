package ir.hanzodev1375.ghostide.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import io.github.rosemoe.sora.event.ContentChangeEvent;
import ir.hanzodev1375.ghostide.R;
import ir.hanzodev1375.ghostide.codeeditors.IdeEditor;
import ir.hanzodev1375.ghostide.codeeditors.langs.cpp.CppLanguage;
import ir.hanzodev1375.ghostide.codeeditors.langs.css.CssLanguage;
import ir.hanzodev1375.ghostide.codeeditors.langs.html.HtmlLanguage;
import ir.hanzodev1375.ghostide.codeeditors.langs.java.JavaLanguage;
import ir.hanzodev1375.ghostide.codeeditors.langs.js.JsLanguage;
import ir.hanzodev1375.ghostide.codeeditors.langs.json.JsonLanguage;
import ir.hanzodev1375.ghostide.codeeditors.langs.python3.Python3Language;
import ir.hanzodev1375.ghostide.databinding.EditorFragmentBinding;
import ir.hanzodev1375.ghostide.mvvm.viewmodel.EditorViewModel;
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
    applyImeInsets(binding.getRoot());
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
              var b = new Bundle();
              b.putString("path", filePath);
              if (content != null) editor.setText(content, b);
            });

    if (filePath != null) viewModel.loadFile(filePath);
    if (filePath.endsWith(".java")) {
      editor.setEditorLanguage(new JavaLanguage());
    } else if (filePath.endsWith(".cpp")) {
      editor.setEditorLanguage(new CppLanguage());
    } else if (filePath.endsWith(".html")) {
      editor.setEditorLanguage(new HtmlLanguage());
    } else if (filePath.endsWith(".css")) {
      editor.setEditorLanguage(new CssLanguage());
    } else if (filePath.endsWith(".js")) {
      editor.setEditorLanguage(new JsLanguage());
    } else if (filePath.endsWith(".py")) {
      editor.setEditorLanguage(new Python3Language());
    } else if (filePath.endsWith(".json")) {
      editor.setEditorLanguage(new JsonLanguage(getContext(), filePath));
    }
    try {
      editor.subscribeEvent(
          ContentChangeEvent.class,
          (e, v) -> {
            editor.updateHintWelcom();
          });

    } catch (Exception err) {
      err.printStackTrace();
    }
  }

  public void saveCurrentFile() {
    if (filePath != null && viewModel != null && editor != null) {
      String content = editor.getText().toString();
      if (content != null) {
        viewModel.saveFile(content);
      } else {
        Log.e("EditorFragment", "محتوای ادیتور نال است");
      }
    }
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    binding = null;
  }

  public IdeEditor getEditor() {
    return editor;
  }

  /**
   * Applies dynamic bottom padding or margin adjustment so that the given view stays above the soft
   * keyboard when it appears.
   *
   * @param target The view that should remain visible (e.g., bottom sheet, header, etc.)
   */
  void applyImeInsets(@NonNull final View target) {
    ViewCompat.setOnApplyWindowInsetsListener(
        target,
        (v, insets) -> {
          Insets imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime());
          Insets navInsets = insets.getInsets(WindowInsetsCompat.Type.navigationBars());
          int bottomInset = Math.max(imeInsets.bottom, navInsets.bottom);

          v.setPadding(v.getPaddingLeft(), v.getPaddingTop(), v.getPaddingRight(), bottomInset);
          return insets;
        });
  }
}
