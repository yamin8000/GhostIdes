package ir.hanzodev1375.ghostide.codeeditors;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import androidx.annotation.Nullable;
import com.eup.codeopsstudio.editor.langs.widget.component.CustomEditorTextActionWindow;
import io.github.rosemoe.sora.graphics.inlayHint.TextInlayHintRenderer;
import io.github.rosemoe.sora.lang.styling.inlayHint.InlayHintsContainer;
import io.github.rosemoe.sora.lang.styling.inlayHint.TextInlayHint;
import io.github.rosemoe.sora.widget.component.EditorAutoCompletion;
import io.github.rosemoe.sora.widget.component.EditorTextActionWindow;
import io.github.rosemoe.sora.widget.component.Magnifier;
import io.github.rosemoe.sora.widget.CodeEditor;
import ir.hanzodev1375.ghostide.codeeditors.colorrender.WebColorIde;
import ir.hanzodev1375.ghostide.codeeditors.setting.Constants;
import ir.hanzodev1375.ghostide.codeeditors.setting.PreferencesUtils;
import ir.hanzodev1375.ghostide.codeeditors.ui.CustomEditorAutoCompletion;
import ir.hanzodev1375.ghostide.codeeditors.ui.CustomEditorCompletionAdapter;
import java.util.Objects;

public class IdeEditor extends CodeEditor
    implements SharedPreferences.OnSharedPreferenceChangeListener {

  private PreferencesUtils setting;
  private WebColorIde webColorIde;

  public IdeEditor(Context context) {
    super(context);
    init();
  }

  public IdeEditor(Context context, AttributeSet attrs) {
    super(context, attrs);
    init();
  }

  private void init() {
    setting = new PreferencesUtils(getContext());
    // test
    setWebIdeColor(true);
    var editorAutoCompletion = new CustomEditorAutoCompletion(this);
    editorAutoCompletion.setAdapter(new CustomEditorCompletionAdapter());
    replaceComponent(EditorAutoCompletion.class, editorAutoCompletion);
    replaceComponent(EditorTextActionWindow.class, new CustomEditorTextActionWindow(this));
    getComponent(EditorAutoCompletion.class)
        .setEnabledAnimation(setting.enableAutoCompleteWindowAnimation());

    updateEditorTabSize();
    updateEditorStickyScroll();
    updateEditorHardWareAcceleration();
    updateEditorScrollBar();
    updateEditorMagnifier();
    updateEditorWordWrap();
    updateEditorLineNumber();
    updateEditorAutoCompletePanelAnimation();
    updateEditorDeleteEmptyLineFast();
    updateEditorDeleteTabs();
    updateEditorHighlightBracketPair();
    updateEditorLineSpacing();
    updateEditorCursorBlinkPeriod();
    updateEditorNonPrintablePaintingFlags();
    updateEditorFontLigatures();
    updateEditorPinLineNumber();
    updateEditorMiniMap();
  }

  private void updateEditorPinLineNumber() {
    setPinLineNumber(setting.pinLineNumber());
  }

  private void updateEditorMiniMap() {
    var enabled = setting.enableMiniMap();
    getProps().showMinimap = enabled;
  }

  public void setInlay(int line, int col, String text, boolean removed) {
    var hint = new InlayHintsContainer();
    if (removed) {
      hint.add(new TextInlayHint(line, col, text));
    } else hint.remove(new TextInlayHint(line, col, text));
    setInlayHints(hint);
    registerInlayHintRenderer(TextInlayHintRenderer.Companion.getDefaultInstance());
  }

  public void setInlayDrawable(int line, int col, Drawable text, boolean removed) {
    var hint = new InlayHintsContainer();
    if (removed) {
      hint.add(new DrawableInlayHint(line, col, text));
    } else hint.remove(new DrawableInlayHint(line, col, text));
    setInlayHints(hint);
    registerInlayHintRenderer(new DrawableInlayHintRenderer());
  }

  public void updateHintWelcom() {
    var hint = new InlayHintsContainer();
    String text = "Welcome to Ghost Ide write something....";
    if (getText().isEmpty()) {
      hint.add(new TextInlayHint(0, 0, text));
    } else {
      hint.remove(new TextInlayHint(0, 0, text));
    }
    setInlayHints(hint);
    registerInlayHintRenderer(TextInlayHintRenderer.Companion.getDefaultInstance());
  }

  public void setWebIdeColor(boolean mod) {
    if (mod) {
      webColorIde = new WebColorIde(this);
      webColorIde.attach();
    }
  }

  private void updateEditorFontLigatures() {
    setLigatureEnabled(setting.useFontLigatures());
  }

  private void updateEditorStickyScroll() {
    var enabled = setting.enableStickyScroll();
    getProps().stickyScroll = enabled;
    setStickyScroll(enabled);
    setStickyScrollMaxLines(4);
  }

  public void setStickyScroll(boolean enabled) {
    getProps().stickyScroll = enabled;
  }

  public void setStickyScrollMaxLines(int maxLines) {
    getProps().stickyScrollMaxLines = maxLines;
  }

  private void updateEditorHardWareAcceleration() {
    setHardwareAcceleratedDrawAllowed(setting.enableHardWareAcceleration());
  }

  private void updateEditorScrollBar() {
    setScrollBarEnabled(setting.enableScrollBar());
  }

  private void updateEditorTabSize() {
    setTabWidth(setting.getCodeEditorTabSize());
  }

  private void updateEditorMagnifier() {
    enableMagnifier(setting.enableMagnifier());
  }

  public void enableMagnifier(boolean enabled) {
    getComponent(Magnifier.class).setEnabled(enabled);
  }

  private void updateEditorWordWrap() {
    setWordwrap(setting.useWordWrap());
  }

  private void updateEditorLineNumber() {
    setLineNumberEnabled(setting.enableLineNumbers());
  }

  private void updateEditorAutoCompletePanelAnimation() {
    animateAutoCompletionPanel(setting.enableAutoCompleteWindowAnimation());
  }

  public void animateAutoCompletionPanel(boolean enabled) {
    getComponent(EditorAutoCompletion.class).setEnabledAnimation(enabled);
  }

  private void updateEditorDeleteEmptyLineFast() {
    deleteEmptyLineFast(setting.enableDeleteEmptyLine());
  }

  public void deleteEmptyLineFast(boolean deleteEmptyLinesFast) {
    getProps().deleteEmptyLineFast = deleteEmptyLinesFast;
  }

  private void updateEditorDeleteTabs() {
    deleteTabs(setting.enableDeleteTab());
  }

  public void deleteTabs(boolean deleteTabs) {
    getProps().deleteMultiSpaces = deleteTabs ? -1 : 1;
  }

  private void updateEditorHighlightBracketPair() {
    setHighlightBracketPair(setting.enableBracketHighlight());
  }

  private void updateEditorLineSpacing() {
    setLineSpacing(setting.getCurrentEditorLineHeight(), 1.1f);
  }

  private void updateEditorCursorBlinkPeriod() {
    setCursorBlinkPeriod(setting.getCursorBlinkPeriod());
  }

  public void useICULibrary(boolean enabled) {
    getProps().useICULibToSelectWords = enabled;
  }

  private void updateEditorNonPrintablePaintingFlags() {
    var flags =
        applyNonPrintableFlags(
            setting.flagLeading(),
            setting.flagInner(),
            setting.flagTrailing(),
            setting.flagEmptyLine(),
            setting.flagLineBreaks(),
            true,
            false);
    setNonPrintablePaintingFlags(flags);
  }

  public int applyNonPrintableFlags(
      boolean leading,
      boolean inner,
      boolean trailing,
      boolean emptyLine,
      boolean lineSeparator,
      boolean inSelection,
      boolean tabSameAsSpace) {
    return (leading ? CodeEditor.FLAG_DRAW_WHITESPACE_LEADING : 0)
        | (inner ? CodeEditor.FLAG_DRAW_WHITESPACE_INNER : 0)
        | (trailing ? CodeEditor.FLAG_DRAW_WHITESPACE_TRAILING : 0)
        | (emptyLine ? CodeEditor.FLAG_DRAW_WHITESPACE_FOR_EMPTY_LINE : 0)
        | (lineSeparator ? CodeEditor.FLAG_DRAW_LINE_SEPARATOR : 0)
        | (inSelection ? CodeEditor.FLAG_DRAW_WHITESPACE_IN_SELECTION : 0)
        | (tabSameAsSpace ? CodeEditor.FLAG_DRAW_TAB_SAME_AS_SPACE : 0);
  }

  @Override
  public void onSharedPreferenceChanged(SharedPreferences pref, @Nullable String key) {
    Objects.requireNonNull(key);
    switch (key) {
      case Constants.SharedPreferenceKeys.KEY_CODE_EDITOR_TAB_SIZE:
        updateEditorTabSize();
        break;
      case Constants.SharedPreferenceKeys.KEY_CODE_EDITOR_STICKY_SCROLL:
        updateEditorStickyScroll();
        break;
      case Constants.SharedPreferenceKeys.KEY_CODE_EDITOR_HARDWARE_ACCELERATION:
        updateEditorHardWareAcceleration();
        break;
      case Constants.SharedPreferenceKeys.KEY_CODE_EDITOR_SCROLL_BAR:
        updateEditorScrollBar();
        break;
      case Constants.SharedPreferenceKeys.KEY_CODE_EDITOR_MAGNIFIER:
        updateEditorMagnifier();
        break;
      case Constants.SharedPreferenceKeys.KEY_CODE_EDITOR_WORD_WRAP:
        updateEditorWordWrap();
        break;
      case Constants.SharedPreferenceKeys.KEY_CODE_EDITOR_LINE_NUMBERS:
        updateEditorLineNumber();
        break;
      case Constants.SharedPreferenceKeys.KEY_CODE_EDITOR_ANIMATE_AUTO_COMP_WINDOW:
        updateEditorAutoCompletePanelAnimation();
        break;
      case Constants.SharedPreferenceKeys.KEY_CODE_EDITOR_DELETE_EMPTY_LINE:
        updateEditorDeleteEmptyLineFast();
        break;
      case Constants.SharedPreferenceKeys.KEY_CODE_EDITOR_DELETE_TAB:
        updateEditorDeleteTabs();
        break;
      case Constants.SharedPreferenceKeys.KEY_CODE_EDITOR_HIGHLIGHT_BRACKET:
        updateEditorHighlightBracketPair();
        break;
      case Constants.SharedPreferenceKeys.KEY_CODE_EDITOR_LINE_HEIGHT:
        updateEditorLineSpacing();
        break;
      case Constants.SharedPreferenceKeys.KEY_CODE_EDITOR_CURSOR_BLINK_PERIOD:
        updateEditorCursorBlinkPeriod();
        break;
      case Constants.SharedPreferenceKeys.KEY_CODE_EDITOR_NP_PAINT_FLAGS:
        updateEditorNonPrintablePaintingFlags();
        break;
      case Constants.SharedPreferenceKeys.KEY_CODE_EDITOR_FONT_LIAGTURES:
        updateEditorFontLigatures();
        break;
      case Constants.SharedPreferenceKeys.KEY_CODE_EDITOR_PIN_LINE_NUM:
        updateEditorPinLineNumber();
        break;
      case Constants.SharedPreferenceKeys.KEY_CODE_EDITOR_ICU:
        useICULibrary(setting.useICULibrary());
        break;
      default:
        // nothing
    }
  }
}
