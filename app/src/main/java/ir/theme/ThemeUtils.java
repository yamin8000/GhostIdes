package ir.theme;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import io.github.rosemoe.sora.widget.schemes.EditorColorScheme;
import ir.hanzodev1375.ghostide.codeeditors.IdeEditor;
import ir.hanzodev1375.ghostide.codeeditors.colorscheme.GhostColorScheme;
import java.lang.reflect.Field;

public class ThemeUtils {

  private final ThemeManager manager;

  public ThemeUtils(ThemeManager manager) {
    this.manager = manager;
  }

  public GhostTheme getTheme() {
    return manager.getTheme();
  }

  public void applyActivity(AppCompatActivity activity) {

    GhostTheme theme = getTheme();
    if (theme == null) {
      return;
    }

    if (theme.getActivity() == null) {
      return;
    }

    ActivityTheme colors = theme.getActivity();

    Window window = activity.getWindow();

    if (colors.getStatusBar() != null) {

      window.setStatusBarColor(parseColor(colors.getStatusBar()));
    }

    if (colors.getNavigationBar() != null) {

      window.setNavigationBarColor(parseColor(colors.getNavigationBar()));
    }

    if (colors.getBackground() != null) {

      View decor = window.getDecorView();

      decor.setBackgroundColor(parseColor(colors.getBackground()));
    }
  }

  public void applyEditor(IdeEditor editor) {

    GhostTheme theme = getTheme();

    if (theme == null) {
      return;
    }

    if (theme.getEditor() == null) {
      return;
    }

    EditorTheme t = theme.getEditor();

    var scheme = editor.getColorScheme();
    scheme.setColor(GhostColorScheme.KEYWORD, Color.parseColor(t.getKeyword()));
    scheme.setColor(GhostColorScheme.WHOLE_BACKGROUND, Color.parseColor(t.getWholeBackground()));
    scheme.setColor(GhostColorScheme.LINE_DIVIDER, Color.parseColor(t.getLineDivider()));
    scheme.setColor(GhostColorScheme.LINE_NUMBER, Color.parseColor(t.getLineNumber()));
    scheme.setColor(
        GhostColorScheme.LINE_NUMBER_BACKGROUND, Color.parseColor(t.getLineNumberBackground()));
    scheme.setColor(GhostColorScheme.WHOLE_BACKGROUND, Color.parseColor(t.getWholeBackground()));
    scheme.setColor(GhostColorScheme.TEXT_NORMAL, Color.parseColor(t.getTextNormal()));
    scheme.setColor(
        GhostColorScheme.SELECTED_TEXT_BACKGROUND, Color.parseColor(t.getSelectedTextBackground()));
    scheme.setColor(GhostColorScheme.SELECTION_INSERT, Color.parseColor(t.getSelectionInsert()));
    scheme.setColor(GhostColorScheme.SELECTION_HANDLE, Color.parseColor(t.getSelectionHandle()));
    scheme.setColor(GhostColorScheme.CURRENT_LINE, Color.parseColor(t.getCurrentLine()));
    scheme.setColor(GhostColorScheme.UNDERLINE, Color.parseColor(t.getUnderline()));
    scheme.setColor(GhostColorScheme.SCROLL_BAR_THUMB, Color.parseColor(t.getScrollBarThumb()));
    scheme.setColor(
        GhostColorScheme.SCROLL_BAR_THUMB_PRESSED, Color.parseColor(t.getScrollBarThumbPressed()));
    scheme.setColor(GhostColorScheme.SCROLL_BAR_TRACK, Color.parseColor(t.getScrollBarTrack()));
    scheme.setColor(GhostColorScheme.BLOCK_LINE, Color.parseColor(t.getBlockLine()));
    scheme.setColor(GhostColorScheme.BLOCK_LINE_CURRENT, Color.parseColor(t.getBlockLineCurrent()));
    scheme.setColor(GhostColorScheme.LINE_NUMBER_PANEL, Color.parseColor(t.getLineNumberPanel()));
    scheme.setColor(
        GhostColorScheme.LINE_NUMBER_PANEL_TEXT, Color.parseColor(t.getLineNumberPanelText()));
    scheme.setColor(
        GhostColorScheme.COMPLETION_WND_BACKGROUND,
        Color.parseColor(t.getCompletionWndBackground()));
    scheme.setColor(
        GhostColorScheme.COMPLETION_WND_CORNER, Color.parseColor(t.getCompletionWndCorner()));
    scheme.setColor(GhostColorScheme.KEYWORD, Color.parseColor(t.getKeyword()));
    scheme.setColor(GhostColorScheme.COMMENT, Color.parseColor(t.getComment()));
    scheme.setColor(GhostColorScheme.OPERATOR, Color.parseColor(t.getOperator()));
    scheme.setColor(GhostColorScheme.LITERAL, Color.parseColor(t.getLiteral()));
    scheme.setColor(GhostColorScheme.IDENTIFIER_VAR, Color.parseColor(t.getIdentifierVar()));
    scheme.setColor(GhostColorScheme.IDENTIFIER_NAME, Color.parseColor(t.getIdentifierName()));
    scheme.setColor(GhostColorScheme.FUNCTION_NAME, Color.parseColor(t.getFunctionName()));
    scheme.setColor(GhostColorScheme.ANNOTATION, Color.parseColor(t.getAnnotation()));
    scheme.setColor(
        GhostColorScheme.MATCHED_TEXT_BACKGROUND, Color.parseColor(t.getMatchedTextBackground()));
    scheme.setColor(GhostColorScheme.TEXT_SELECTED, Color.parseColor(t.getTextSelected()));
    scheme.setColor(GhostColorScheme.NON_PRINTABLE_CHAR, Color.parseColor(t.getNonPrintableChar()));
    scheme.setColor(GhostColorScheme.HTML_TAG, Color.parseColor(t.getHtmlTag()));
    scheme.setColor(GhostColorScheme.ATTRIBUTE_NAME, Color.parseColor(t.getAttributeName()));
    scheme.setColor(GhostColorScheme.ATTRIBUTE_VALUE, Color.parseColor(t.getAttributeValue()));
    scheme.setColor(GhostColorScheme.PROBLEM_ERROR, Color.parseColor(t.getProblemError()));
    scheme.setColor(GhostColorScheme.PROBLEM_WARNING, Color.parseColor(t.getProblemWarning()));
    scheme.setColor(GhostColorScheme.PROBLEM_TYPO, Color.parseColor(t.getProblemTypo()));
    scheme.setColor(GhostColorScheme.COLORNEXTDOT, Color.parseColor(t.getColornextdot()));
    scheme.setColor(GhostColorScheme.COLORNEXTBRAK, Color.parseColor(t.getColornextbrak()));
    scheme.setColor(GhostColorScheme.COLORNEXTCHAR, Color.parseColor(t.getColornextchar()));
    scheme.setColor(GhostColorScheme.COLORUPPERCASE, Color.parseColor(t.getColoruppercase()));
    scheme.setColor(GhostColorScheme.COLORNEXTLESS, Color.parseColor(t.getColornextless()));
  }

  public void applyTextView(TextView textView) {

    GhostTheme theme = getTheme();

    if (theme == null) {
      return;
    }

    if (theme.getWidget() == null) {
      return;
    }

    WidgetTheme widget = theme.getWidget();

    if (widget.getText() != null) {

      textView.setTextColor(parseColor(widget.getText()));
    }

    if (widget.getHint() != null) {

      textView.setHintTextColor(parseColor(widget.getHint()));
    }
  }

  public void applyImageView(ImageView imageView) {

    GhostTheme theme = getTheme();

    if (theme == null) {
      return;
    }

    if (theme.getWidget() == null) {
      return;
    }

    WidgetTheme widget = theme.getWidget();

    if (widget.getImageTint() == null) {
      return;
    }

    imageView.setColorFilter(parseColor(widget.getImageTint()));
  }

  public void applyFab(FloatingActionButton fab) {

    GhostTheme theme = getTheme();

    if (theme == null) {
      return;
    }

    if (theme.getWidget() == null) {
      return;
    }

    WidgetTheme widget = theme.getWidget();

    if (widget.getFabBackground() != null) {

      fab.setBackgroundTintList(ColorStateList.valueOf(parseColor(widget.getFabBackground())));
    }

    if (widget.getFabIcon() != null) {

      fab.setColorFilter(parseColor(widget.getFabIcon()));
    }
  }

  public void applyTabLayout(TabLayout layout) {

    GhostTheme theme = getTheme();

    if (theme == null) {
      return;
    }

    if (theme.getWidget() == null) {
      return;
    }

    WidgetTheme widget = theme.getWidget();

    if (widget.getBackground() != null) {

      layout.setBackgroundColor(parseColor(widget.getBackground()));
    }

    if (widget.getAccent() != null) {

      layout.setSelectedTabIndicatorColor(parseColor(widget.getAccent()));
    }

    if (widget.getTabSelected() != null && widget.getTabUnselected() != null) {

      layout.setTabTextColors(
          parseColor(widget.getTabUnselected()), parseColor(widget.getTabSelected()));
    }
  }

  private int parseColor(String color) {

    try {

      return Color.parseColor(color);

    } catch (Exception e) {

      return Color.WHITE;
    }
  }

  private String toConstant(String camelCase) {

    StringBuilder builder = new StringBuilder();

    for (char c : camelCase.toCharArray()) {

      if (Character.isUpperCase(c)) {

        builder.append("_");
      }

      builder.append(Character.toUpperCase(c));
    }

    return builder.toString();
  }
}
