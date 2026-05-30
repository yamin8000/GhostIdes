package ir.theme;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import ir.hanzodev1375.ghostide.utils.ConstKeys;

public class ThemeManager {

  private final SharedPreferences preferences;

  private final Gson gson;

  public ThemeManager(Context context) {

    preferences = context.getSharedPreferences(ConstKeys.PREFS_NAME, Context.MODE_PRIVATE);

    gson = new Gson();
  }

  public void saveTheme(GhostTheme theme) {

    preferences.edit().putString(ConstKeys.THEME, gson.toJson(theme)).apply();
  }

  public GhostTheme getTheme() {

    String json = preferences.getString(ConstKeys.THEME, null);

    if (json == null || json.isEmpty()) {

      json = getDefaultThemeJson();
    }

    try {

      GhostTheme theme = gson.fromJson(json, GhostTheme.class);

      if (theme == null) {

        return gson.fromJson(getDefaultThemeJson(), GhostTheme.class);
      }

      return theme;

    } catch (Exception e) {

      return gson.fromJson(getDefaultThemeJson(), GhostTheme.class);
    }
  }

  public String getDefaultThemeJson() {
    return """
        {
            "activity": {
                "background": "#1e1e1e",
                "statusBar": "#1e1e1e",
                "navigationBar": "#1e1e1e"
            },
            "editor": {
                "lineDivider": "#B7B7FF",
                "lineNumber": "#00FDFF",
                "lineNumberBackground": "#000000",
                "wholeBackground": "#000000",
                "textNormal": "#d4d4d4",
                "selectedTextBackground": "#264f78",
                "selectionInsert": "#aeafad",
                "selectionHandle": "#ffffff",
                "currentLine": "#2a2d2e",
                "underline": "#ffffff",
                "scrollBarThumb": "#424242",
                "scrollBarThumbPressed": "#686868",
                "scrollBarTrack": "#1e1e1e",
                "blockLine": "#404040",
                "blockLineCurrent": "#707070",
                "lineNumberPanel": "#1e1e1e",
                "lineNumberPanelText": "#ffffff",
                "completionWndBackground": "#252526",
                "completionWndCorner": "#252526",
                "keyword": "#569cd6",
                "comment": "#6a9955",
                "operator": "#d4d4d4",
                "literal": "#ce9178",
                "identifierVar": "#9cdcfe",
                "identifierName": "#4ec9b0",
                "functionName": "#F306F3",
                "annotation": "#c586c0",
                "matchedTextBackground": "#515c6a",
                "matchedTextBorder": "#ffffff",
                "textSelected": "#ffffff",
                "nonPrintableChar": "#404040",
                "htmlTag": "#569cd6",
                "attributeName": "#9cdcfe",
                "attributeValue": "#ce9178",
                "problemError": "#f44747",
                "problemWarning": "#cca700",
                "problemTypo": "#00ff00",  
                "colornextdot": "#FF00FFD4",
                "colornextbrak": "#FF00FF80",
                "colornextchar": "#FFFFF200",
                "coloruppercase": "#FFFF0073",
                "colornextless": "#FF99FF00"
            },
            "widget": {
                "text": "#cccccc",
                "hint": "#aaaaaa",
                "accent": "#007acc",
                "background": "#1e1e1e",
                "surface": "#252526",
                "stroke": "#3c3c3c",
                "fabBackground": "#007acc",
                "fabIcon": "#ffffff",
                "tabSelected": "#ffffff",
                "tabUnselected": "#858585",
                "imageTint": "#c5c5c5"
            }
        }
        """;
  }
}
