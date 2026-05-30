package ir.hanzodev1375.ghostide.codeeditors.setting;

public class Constants {

  public static class SharedPreferenceKeys {
    // Editor Preferences
    public static final String KEY_CODE_EDITOR_FONT_LIAGTURES = "pref_code_editor_font_liagtures";
    public static final String KEY_CODE_EDITOR_WORD_WRAP = "pref_code_editor_word_wrap";
    public static final String KEY_CODE_EDITOR_TAB_INDENT = "pref_code_editor_tab_indent";
    public static final String KEY_CODE_EDITOR_ICU = "pref_code_editor_icu";
    public static final String KEY_CODE_EDITOR_AUTO_SAVE = "pref_code_editor_auto_save";
    public static final String KEY_CODE_EDITOR_RELATIVE_CLOSE_DEPTH =
        "pref_code_editor_relative_close_depth";
    public static final String KEY_CODE_EDITOR_PIN_LINE_NUM = "pref_code_editor_pin_line_numbers";
    public static final String KEY_CODE_EDITOR_MAGNIFIER = "pref_code_editor_use_magnifier";
    public static final String KEY_CODE_EDITOR_STICKY_SCROLL = "pref_code_editor_sticky_scroll";
    public static final String KEY_CODE_EDITOR_AUTO_CLOSE_BRACKET =
        "pref_code_editor_auto_close_bracket";
    public static final String KEY_CODE_EDITOR_SCROLL_BAR = "pref_code_editor_scroll_bar";
    public static final String KEY_CODE_EDITOR_HARDWARE_ACCELERATION =
        "pref_code_editor_hardware_acceleration";
    public static final String KEY_CODE_EDITOR_LINE_NUMBERS = "pref_code_editor_line_numbers";
    public static final String KEY_CODE_EDITOR_DELETE_EMPTY_LINE =
        "pref_code_editor_delete_empty_line_bck_key_event";
    public static final String KEY_CODE_EDITOR_DELETE_TAB =
        "pref_code_editor_delete_tab_bck_key_event";
    public static final String KEY_CODE_EDITOR_ANIMATE_AUTO_COMP_WINDOW =
        "pref_code_editor_animate_auto_complt_window";
    public static final String KEY_CODE_EDITOR_HIGHLIGHT_BRACKET =
        "pref_code_editor_highlight_brckt";
    public static final String KEY_CODE_EDITOR_AUTO_COMPLETE = "pref_code_editor_auto_complete";
    public static final String KEY_CODE_EDITOR_CURSOR_BLINK_PERIOD =
        "pref_code_editor_cursor_blnk_period";
    public static final String KEY_CODE_EDITOR_TAB_SIZE = "pref_code_editor_tab_size";
    public static final String KEY_CODE_EDITOR_LINE_HEIGHT = "pref_code_editor_line_height";
    public static final String KEY_CODE_EDITOR_NP_PAINT_FLAGS = "pref_code_editor_npc";

    // General Configuration Preferences
    public static final String KEY_BUFFER_SIZE = "pref_general_config_buffer_size";

    private SharedPreferenceKeys() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
  }
}