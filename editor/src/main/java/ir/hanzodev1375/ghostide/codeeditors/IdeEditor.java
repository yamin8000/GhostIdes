package ir.hanzodev1375.ghostide.codeeditors;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.AttributeSet;

import androidx.annotation.Nullable;
import com.eup.codeopsstudio.editor.langs.widget.component.CustomEditorTextActionWindow;
import io.github.rosemoe.sora.graphics.inlayHint.ColorInlayHintRenderer;
import io.github.rosemoe.sora.text.CharPosition;
import io.github.rosemoe.sora.widget.component.EditorAutoCompletion;
import io.github.rosemoe.sora.widget.component.EditorTextActionWindow;
import io.github.rosemoe.sora.widget.component.Magnifier;
import ir.hanzodev1375.ghostide.codeeditors.setting.Constants;
import ir.hanzodev1375.ghostide.codeeditors.setting.PreferencesUtils;
import ir.hanzodev1375.ghostide.codeeditors.ui.CustomEditorAutoCompletion;
import ir.hanzodev1375.ghostide.codeeditors.ui.CustomEditorCompletionAdapter;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.github.rosemoe.sora.event.ContentChangeEvent;
import io.github.rosemoe.sora.lang.styling.color.ConstColor;
import io.github.rosemoe.sora.lang.styling.inlayHint.ColorInlayHint;
import io.github.rosemoe.sora.lang.styling.inlayHint.InlayHintsContainer;
import io.github.rosemoe.sora.widget.CodeEditor;

public class IdeEditor extends CodeEditor
    implements SharedPreferences.OnSharedPreferenceChangeListener {
  private PreferencesUtils setting;
  private static final Pattern COLOR_PATTERN =
      Pattern.compile(
          "(?:(#(?:[0-9a-fA-F]{3}|[0-9a-fA-F]{6}|[0-9a-fA-F]{8}))\\b"
              + "|rgb\\((\\d{1,3})\\s*,\\s*(\\d{1,3})\\s*,\\s*(\\d{1,3})\\s*\\)"
              + "|rgba\\((\\d{1,3})\\s*,\\s*(\\d{1,3})\\s*,\\s*(\\d{1,3})\\s*,\\s*([\\d.]+)\\s*\\))");

  public IdeEditor(Context context) {
    super(context);
    init();
  }

  public IdeEditor(Context context, AttributeSet attrs) {
    super(context, attrs);
    init();
  }

  private void init() {
    setWebColor(true);
    setting = new PreferencesUtils(getContext());
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
  }

  private void updateEditorPinLineNumber() {
    setPinLineNumber(setting.pinLineNumber());
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

  public void setWebColor(boolean colorMod) {
    if (colorMod) {
      subscribeEvent(ContentChangeEvent.class, (event, unsubscribe) -> updateColorHints());
      post(this::updateColorHints);
    }
  }

  private void updateColorHints() {
    String text = getText().toString();
    Matcher matcher = COLOR_PATTERN.matcher(text);
    InlayHintsContainer container = new InlayHintsContainer();

    while (matcher.find()) {
      String hexGroup = matcher.group(1);
      if (hexGroup != null) {
        String hex = hexGroup;
        if (hex.length() == 4) {
          hex = expandShortHex(hex);
        }
        container.add(
            new ColorInlayHint(
                textPosition(matcher.start()).line,
                textPosition(matcher.start()).column,
                new ConstColor(hex)));
      } else if (matcher.group(2) != null) {

        int r = clamp(parseIntSafe(matcher.group(2)), 0, 255);
        int g = clamp(parseIntSafe(matcher.group(3)), 0, 255);
        int b = clamp(parseIntSafe(matcher.group(4)), 0, 255);
        int color = 0xFF000000 | (r << 16) | (g << 8) | b;
        container.add(
            new ColorInlayHint(
                textPosition(matcher.start()).line,
                textPosition(matcher.start()).column,
                new ConstColor(color)));
      } else if (matcher.group(5) != null) {

        int r = clamp(parseIntSafe(matcher.group(5)), 0, 255);
        int g = clamp(parseIntSafe(matcher.group(6)), 0, 255);
        int b = clamp(parseIntSafe(matcher.group(7)), 0, 255);
        float aFloat = parseFloatSafe(matcher.group(8));
        int a = clamp(Math.round(aFloat * 255), 0, 255);
        int color = (a << 24) | (r << 16) | (g << 8) | b;
        container.add(
            new ColorInlayHint(
                textPosition(matcher.start()).line,
                textPosition(matcher.start()).column,
                new ConstColor(color)));
      }
    }

    setInlayHints(container);
    registerInlayHintRenderer(ColorInlayHintRenderer.Companion.getDefaultInstance());
  }

  private String expandShortHex(String hex) {
    StringBuilder sb = new StringBuilder("#");
    for (int i = 1; i < hex.length(); i++) {
      char c = hex.charAt(i);
      sb.append(c).append(c);
    }
    return sb.toString();
  }

  private int clamp(int value, int min, int max) {
    return Math.max(min, Math.min(max, value));
  }

  private int parseIntSafe(String s) {
    try {
      return Integer.parseInt(s);
    } catch (NumberFormatException e) {
      return 0;
    }
  }

  private float parseFloatSafe(String s) {
    try {
      return Float.parseFloat(s);
    } catch (NumberFormatException e) {
      return 1f;
    }
  }

  private CharPosition textPosition(int offset) {
    return getText().getIndexer().getCharPosition(offset);
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

  /*
   * Applies a set of non-printable painting flags.
   * This should set the flags dynamically if the flags are enabled from the preferences
   * the flags would be added otherwise #flag would return 0 at that particular flag to disable it
   */
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
      default: // Nothing
    }
  }
}
