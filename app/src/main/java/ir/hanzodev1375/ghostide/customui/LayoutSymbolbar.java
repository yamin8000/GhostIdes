package ir.hanzodev1375.ghostide.customui;

import android.content.Context;
import android.util.AttributeSet;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.transition.Transition;
import com.google.android.material.transition.MaterialSharedAxis;
import androidx.transition.TransitionManager;
import android.view.View;
import ir.hanzodev1375.ghostide.adapters.SysmbolbarAdapter;
import ir.hanzodev1375.ghostide.codeeditors.IdeEditor;
import ir.theme.ThemeManager;
import ir.theme.ThemeUtils;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.util.ArrayList;
import java.util.HashMap;

public class LayoutSymbolbar extends LinearLayoutCompat {
  private IdeEditor editor;
  private boolean isShowing = false;
  private ArrayList<HashMap<String, Object>> staticSymbiolPiare = new ArrayList<>();

  public LayoutSymbolbar(Context c) {
    super(c);
    init();
  }

  public LayoutSymbolbar(Context c, AttributeSet s) {
    super(c, s);
    init();
  }

  public void bindEditor(IdeEditor editor) {
    this.editor = editor;
  }

  void init() {
    var rv = new RecyclerView(getContext());
    rv.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
    removeAllViews();
    if (rv != null) {
      addView(rv);
    }
    try {
      InputStream inputstream5 = getContext().getAssets().open("data/symbol.json");
      staticSymbiolPiare =
          new Gson()
              .fromJson(
                  copyFromInputStream(inputstream5),
                  new TypeToken<ArrayList<HashMap<String, Object>>>() {}.getType());

    } catch (Exception err) {

    }

    SysmbolbarAdapter syspiarAdapter =
        new SysmbolbarAdapter(
            staticSymbiolPiare,
            new SysmbolbarAdapter.OnTabView() {
              @Override
              public void TAB(String tab) {
                editor.commitText("\t");
              }

              @Override
              public void POST(String post) {
                editor.insertText(post, post.length());
              }
            },
            null);
    rv.setAdapter(syspiarAdapter);
    rv.setLayoutManager(
        new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

    var manager = new ThemeManager(getContext());
    var themeUtils = new ThemeUtils(manager);
    themeUtils.applySymbolBarLayout(this);
    setVisibility(View.GONE);
    isShowing = false;
  }

  protected String copyFromInputStream(InputStream inputStream) {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    byte[] buf = new byte[1024];
    int i;
    try {
      while ((i = inputStream.read(buf)) != -1) {
        outputStream.write(buf, 0, i);
      }
      outputStream.close();
      inputStream.close();
    } catch (IOException e) {
    }

    return outputStream.toString();
  }

  public void show() {
    if (!isShowing) {
      Transition sharedAxis = new MaterialSharedAxis(MaterialSharedAxis.Z, true);
      TransitionManager.beginDelayedTransition(this, sharedAxis);
      setVisibility(View.VISIBLE);
      isShowing = true;
    }
  }

  public void hide() {
    if (isShowing) {
      Transition sharedAxis = new MaterialSharedAxis(MaterialSharedAxis.Z, true);
      TransitionManager.beginDelayedTransition(this, sharedAxis);
      setVisibility(View.GONE);
      isShowing = false;
    }
  }

  public boolean isShowing() {
    return isShowing;
  }
}
