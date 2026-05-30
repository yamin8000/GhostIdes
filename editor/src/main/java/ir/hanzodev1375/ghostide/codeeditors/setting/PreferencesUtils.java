package ir.hanzodev1375.ghostide.codeeditors.setting;

import android.content.SharedPreferences;
import android.util.Log;
import androidx.annotation.NonNull;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.HashSet;
import android.preference.PreferenceManager;
import androidx.appcompat.app.AppCompatDelegate;
import android.content.Context;

public class PreferencesUtils {

  private final String TAG = PreferencesUtils.class.getSimpleName();

  /**
   * Check if editor automatically saves files.
   *
   * @return true if files are automatically saved, otherwise false.
   */
  Context c;

  public PreferencesUtils(Context c) {
    this.c = c;
  }

  public boolean autoSaveFiles() {
    return getDefaultPreferences()
        .getBoolean(Constants.SharedPreferenceKeys.KEY_CODE_EDITOR_AUTO_SAVE, false);
  }

  public boolean canCloseRelativeToFirstDepth() {
    var depth =
        getDefaultPreferences()
            .getString(
                Constants.SharedPreferenceKeys.KEY_CODE_EDITOR_RELATIVE_CLOSE_DEPTH, "First");
    if (depth.equalsIgnoreCase("All")) {
      return false;
    } else if (depth.equalsIgnoreCase("First")) {
      return true;
    }
    return true; // default to first tab
  }

  public void clearPreference(@NonNull SharedPreferences pref, String key) {
    Map<String, ?> cues = pref.getAll();
    cues.forEach(
        (k, v) -> {
          if (Objects.equals(key, k)) {
            emptyEditorValue(pref.edit(), k, v);
          }
        });
  }

  public int getCursorBlinkPeriod() {
    return getDefaultPreferences()
        .getInt(Constants.SharedPreferenceKeys.KEY_CODE_EDITOR_CURSOR_BLINK_PERIOD, 500);
  }

  private void emptyEditorValue(
      SharedPreferences.Editor editor, @NonNull String key, @NonNull Object value) {
    try {
      if (value instanceof Boolean) {
        editor.putBoolean(key, false).apply();
      } else if (value instanceof Float) {
        editor.putFloat(key, 0.0f);
      } else if (value instanceof String) {
        editor.putString(key, "");
      } else if (value instanceof Integer) {
        editor.putInt(key, 0);
      } else if (value instanceof Long) {
        editor.putLong(key, 0L);
      } else if (value instanceof Set<?>) {
        editor.putStringSet(key, new HashSet<>());
      }
      Log.d(TAG, String.format(" Key-Value: %s -> %s", key, value));
      editor.apply();
    } catch (Exception e) {
      Log.e(TAG, "Failed to clear preference value", e);
    }
  }

  public boolean enableAutoComplete() {
    return getDefaultPreferences()
        .getBoolean(Constants.SharedPreferenceKeys.KEY_CODE_EDITOR_AUTO_COMPLETE, false);
  }

  /**
   * Check if auto-complete window animation is enabled.
   *
   * @return true if auto-complete window animation is enabled, otherwise false.
   */
  public boolean enableAutoCompleteWindowAnimation() {
    return getDefaultPreferences()
        .getBoolean(Constants.SharedPreferenceKeys.KEY_CODE_EDITOR_ANIMATE_AUTO_COMP_WINDOW, false);
  }

  /**
   * Check if bracket auto-closing is enabled.
   *
   * @return true if bracket auto-closing is enabled, otherwise false.
   */
  public boolean enableBracketAutoClosing() {
    return getDefaultPreferences()
        .getBoolean(Constants.SharedPreferenceKeys.KEY_CODE_EDITOR_AUTO_CLOSE_BRACKET, false);
  }

  /**
   * Check if bracket highlighting is enabled.
   *
   * @return true if bracket highlighting is enabled, otherwise false.
   */
  public boolean enableBracketHighlight() {
    return getDefaultPreferences()
        .getBoolean(Constants.SharedPreferenceKeys.KEY_CODE_EDITOR_HIGHLIGHT_BRACKET, true);
  }

  /**
   * Check if deleting empty lines is enabled.
   *
   * @return true if deleting empty lines is enabled, otherwise false.
   */
  public boolean enableDeleteEmptyLine() {
    return getDefaultPreferences()
        .getBoolean(Constants.SharedPreferenceKeys.KEY_CODE_EDITOR_DELETE_EMPTY_LINE, false);
  }

  /**
   * Check if deleting tabs is enabled.
   *
   * @return true if deleting tabs is enabled, otherwise false.
   */
  public boolean enableDeleteTab() {
    return getDefaultPreferences()
        .getBoolean(Constants.SharedPreferenceKeys.KEY_CODE_EDITOR_DELETE_TAB, false);
  }

  /**
   * Check if hardware acceleration is enabled.
   *
   * @return true if hardware acceleration is enabled, otherwise false.
   */
  public boolean enableHardWareAcceleration() {
    return getDefaultPreferences()
        .getBoolean(Constants.SharedPreferenceKeys.KEY_CODE_EDITOR_HARDWARE_ACCELERATION, false);
  }

  /**
   * Check if line numbers are enabled.
   *
   * @return true if line numbers are enabled, otherwise false.
   */
  public boolean enableLineNumbers() {
    return getDefaultPreferences()
        .getBoolean(Constants.SharedPreferenceKeys.KEY_CODE_EDITOR_LINE_NUMBERS, true);
  }

  /**
   * Check if the magnifier is enabled.
   *
   * @return true if the magnifier is enabled, otherwise false.
   */
  public boolean enableMagnifier() {
    return getDefaultPreferences()
        .getBoolean(Constants.SharedPreferenceKeys.KEY_CODE_EDITOR_MAGNIFIER, true);
  }

  /**
   * Check if the scroll bar is enabled.
   *
   * @return true if the scroll bar is enabled, otherwise false.
   */
  public boolean enableScrollBar() {
    return getDefaultPreferences()
        .getBoolean(Constants.SharedPreferenceKeys.KEY_CODE_EDITOR_SCROLL_BAR, false);
  }

  /**
   * Check if sticky scroll is enabled.
   *
   * @return true if sticky scroll is enabled, otherwise false.
   */
  public boolean enableStickyScroll() {
    return getDefaultPreferences()
        .getBoolean(Constants.SharedPreferenceKeys.KEY_CODE_EDITOR_STICKY_SCROLL, false);
  }

  /**
   * Check if the selected np-painting flag is "empty-line".
   *
   * @return true if the selected np-painting flag is "empty-line", otherwise false.
   */
  public boolean flagEmptyLine() {
    Set<String> selectedValues =
        getDefaultPreferences()
            .getStringSet(
                Constants.SharedPreferenceKeys.KEY_CODE_EDITOR_NP_PAINT_FLAGS, new HashSet<>());
    return selectedValues.contains("4");
  }

  /**
   * Check if the selected np-painting flag is "inner".
   *
   * @return true if the selected np-painting flag is "inner", otherwise false.
   */
  public boolean flagInner() {
    Set<String> selectedValues =
        getDefaultPreferences()
            .getStringSet(
                Constants.SharedPreferenceKeys.KEY_CODE_EDITOR_NP_PAINT_FLAGS, new HashSet<>());
    return selectedValues.contains("1");
  }

  /**
   * Check if the selected np-painting flag is "leading".
   *
   * @return true if the selected np-painting flag is "leading", otherwise false.
   */
  public boolean flagLeading() {
    Set<String> selectedValues =
        getDefaultPreferences()
            .getStringSet(
                Constants.SharedPreferenceKeys.KEY_CODE_EDITOR_NP_PAINT_FLAGS, new HashSet<>());
    return selectedValues.contains("2");
  }

  /**
   * Check if the selected np-painting flag is "line-breaks".
   *
   * @return true if the selected np-painting flag is "line-breaks", otherwise false.
   */
  public boolean flagLineBreaks() {
    Set<String> selectedValues =
        getDefaultPreferences()
            .getStringSet(
                Constants.SharedPreferenceKeys.KEY_CODE_EDITOR_NP_PAINT_FLAGS, new HashSet<>());
    return selectedValues.contains("5");
  }

  /**
   * Check if the selected np-painting flag is "trailing".
   *
   * @return true if the selected np-painting flag is "trailing", otherwise false.
   */
  public boolean flagTrailing() {
    Set<String> selectedValues =
        getDefaultPreferences()
            .getStringSet(
                Constants.SharedPreferenceKeys.KEY_CODE_EDITOR_NP_PAINT_FLAGS, new HashSet<>());
    return selectedValues.contains("3");
  }
  /**
   * Get the selected tab size for the code editor.
   *
   * @return The selected tab size.
   */
  public int getCodeEditorTabSize() {
    return getCodeEditorTabSize(2);
  }

  public int getCodeEditorTabSize(int size) {
    return getDefaultPreferences()
        .getInt(Constants.SharedPreferenceKeys.KEY_CODE_EDITOR_TAB_SIZE, size);
  }

  public SharedPreferences getDefaultPreferences() {
    return PreferenceManager.getDefaultSharedPreferences(c);
  }

  /**
   * Get the user selected buffer size
   *
   * <p>No set method to prevent concurrent modification, buffer size is updated on restart
   *
   * @return the buffer size in kilobytes
   */
  public int getCurrentBufferSize() {
    var selectedBufferSize =
        getDefaultPreferences().getString(Constants.SharedPreferenceKeys.KEY_BUFFER_SIZE, "5");
    return Integer.parseInt(selectedBufferSize) * 1024;
  }

  /**
   * Get the selected line height for the code editor.
   *
   * @return The selected line height.
   */
  public float getCurrentEditorLineHeight() {
    var selectedLineHeight =
        getDefaultPreferences()
            .getString(Constants.SharedPreferenceKeys.KEY_CODE_EDITOR_LINE_HEIGHT, "2");
    return getEditorLineHeight(selectedLineHeight);
  }

  /**
   * Get the line height value based on the user's choice for the code editor.
   *
   * @param lineHeightEntry The user's line height choice.
   * @return The corresponding line height value.
   */
  private float getEditorLineHeight(String lineHeightEntry) {
    return switch (lineHeightEntry) {
      case "1" -> 1;
      case "3" -> 3;
      case "4" -> 4;
      default -> 2;
    };
  }

  public int getCursorBlinkPeriod(int defaultBlinkPeriod) {
    return getDefaultPreferences()
        .getInt(
            Constants.SharedPreferenceKeys.KEY_CODE_EDITOR_CURSOR_BLINK_PERIOD, defaultBlinkPeriod);
  }

  public boolean pinLineNumber() {
    return getDefaultPreferences()
        .getBoolean(Constants.SharedPreferenceKeys.KEY_CODE_EDITOR_PIN_LINE_NUM, false);
  }

  /**
   * Check if the font ligatures is enabled
   *
   * @return true if the font ligatures is used, otherwise false.
   */
  public boolean useFontLigatures() {
    return getDefaultPreferences()
        .getBoolean(Constants.SharedPreferenceKeys.KEY_CODE_EDITOR_FONT_LIAGTURES, false);
  }

  /**
   * Check if the ICU library is used for word edge retrieval in the code editor.
   *
   * @return true if the ICU library is used, otherwise false.
   */
  public boolean useICULibrary() {
    return getDefaultPreferences()
        .getBoolean(Constants.SharedPreferenceKeys.KEY_CODE_EDITOR_ICU, false);
  }

  /**
   * Check if tabs are used instead of spaces in the code editor.
   *
   * @return true if tabs are used, otherwise false.
   */
  public boolean useTabIndentation() {
    return getDefaultPreferences()
        .getBoolean(Constants.SharedPreferenceKeys.KEY_CODE_EDITOR_TAB_INDENT, false);
  }

  /**
   * Check if word wrap is enabled for the code editor.
   *
   * @return true if word wrap is enabled, otherwise false.
   */
  public boolean useWordWrap() {
    return getDefaultPreferences()
        .getBoolean(Constants.SharedPreferenceKeys.KEY_CODE_EDITOR_WORD_WRAP, false);
  }
}
