package ir.hanzodev1375.ghostide.codeeditors.langs.html;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.github.rosemoe.sora.lang.EmptyLanguage;
import io.github.rosemoe.sora.lang.Language;
import io.github.rosemoe.sora.lang.QuickQuoteHandler;
import io.github.rosemoe.sora.lang.analysis.AnalyzeManager;
import io.github.rosemoe.sora.lang.completion.CompletionHelper;
import io.github.rosemoe.sora.lang.completion.CompletionPublisher;
import io.github.rosemoe.sora.lang.completion.IdentifierAutoComplete;
import io.github.rosemoe.sora.lang.completion.snippet.CodeSnippet;
import io.github.rosemoe.sora.lang.completion.snippet.parser.CodeSnippetParser;
import io.github.rosemoe.sora.lang.completion.SnippetDescription;
import io.github.rosemoe.sora.lang.format.Formatter;
import io.github.rosemoe.sora.lang.smartEnter.NewlineHandler;
import io.github.rosemoe.sora.text.CharPosition;
import io.github.rosemoe.sora.text.ContentReference;
import io.github.rosemoe.sora.widget.SymbolPairMatch;
import ir.hanzodev1375.ghostide.codeeditors.langs.antlr4base.CharParser;
import ir.hanzodev1375.ghostide.codeeditors.langs.antlr4base.SnippetCompletionItem;
import ir.hanzodev1375.ghostide.codeeditors.lspcustomhot.Css3Server;
import ir.hanzodev1375.ghostide.codeeditors.lspcustomhot.PathCompleter;
import java.io.StringReader;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.Token;
import java.io.IOException;
import java.util.List;
import ir.hanzodev1375.ghostide.codeeditors.lspcustomhot.CustomCompletionItem;

public class HtmlLanguage implements Language {

  private final HTMLAnalyzer analyzer;
  private final IdentifierAutoComplete autoComplete;
  private static final CodeSnippet HTML5_SNIPPET =
      CodeSnippetParser.parse(
          "<!DOCTYPE html>\n<html lang=\"en\">\n<head>\n    <meta charset=\"UTF-8\">\n    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n    <title>${1:Document}</title>\n</head>\n<body>\n    $0\n</body>\n</html>");

  private static final CodeSnippet DIV_CLASS_SNIPPET =
      CodeSnippetParser.parse("<div class=\"${1:className}\">\n    $0\n</div>");

  private static final CodeSnippet LINK_CSS_SNIPPET =
      CodeSnippetParser.parse("<link rel=\"stylesheet\" href=\"${1:style.css}\">$0");

  private static final CodeSnippet SCRIPT_SRC_SNIPPET =
      CodeSnippetParser.parse("<script src=\"${1:script.js}\"></script>$0");

  private static final CodeSnippet BUTTON_SNIPPET =
      CodeSnippetParser.parse("<button type=\"${1:button}\">${2:Click me}</button>$0");

  private static final CodeSnippet INPUT_SNIPPET =
      CodeSnippetParser.parse(
          "<input type=\"${1:text}\" name=\"${2:name}\" id=\"${3:id}\" placeholder=\"${4:Enter...}\">$0");

  public HtmlLanguage() {
    String[] htmlKeywords = {"!", "DOCTYPE"};
    autoComplete = new IdentifierAutoComplete(htmlKeywords);
    analyzer = new HTMLAnalyzer();
  }

  @NonNull
  @Override
  public AnalyzeManager getAnalyzeManager() {
    return analyzer;
  }

  @Nullable
  @Override
  public QuickQuoteHandler getQuickQuoteHandler() {
    return null;
  }

  @Override
  public void destroy() {}

  @Override
  public int getInterruptionLevel() {
    return INTERRUPTION_LEVEL_STRONG;
  }

  @Override
  public void requireAutoComplete(
      @NonNull ContentReference content,
      @NonNull CharPosition position,
      @NonNull CompletionPublisher publisher,
      @NonNull Bundle es) {
    String prefix = CompletionHelper.computePrefix(content, position, CharParser::parserHtml);
    // try {
      // var listPath = PathCompleter.getPathCompletions(es.getString("path"), prefix);
      // for (var it : listPath) {
        // publisher.addItem(it);
      // }

    // } catch (Exception err) {

    // }
    if (isInsideStyleTag(content, position)) {
      
      for (CssCompletionItem item : CssHelper.getPropertyItemsByPrefix(prefix)) {
        publisher.addItem(item);
      }

      
      Css3Server cssServer = new Css3Server();
      List<CustomCompletionItem> cssItems = cssServer.getCompletions(prefix);
      for (CustomCompletionItem item : cssItems) {
        publisher.addItem(item);
      }

      
      for (CustomCompletionItem item : HtmlHelper.getNormalTag(prefix)) {
        publisher.addItem(item);
      }
      return;
    }

    autoComplete.requireAutoComplete(
        content, position, prefix, publisher, analyzer.getSyncIdentifiers());
    if ("html5".startsWith(prefix) && prefix.length() > 0) {
      publisher.addItem(
          new SnippetCompletionItem(
              "html5",
              "Snippet - HTML5 Boilerplate",
              new SnippetDescription(prefix.length(), HTML5_SNIPPET, true)));
    }
    if ("divc".startsWith(prefix) && prefix.length() > 0) {
      publisher.addItem(
          new SnippetCompletionItem(
              "divc",
              "Snippet - Div with class",
              new SnippetDescription(prefix.length(), DIV_CLASS_SNIPPET, true)));
    }
    if ("linkcss".startsWith(prefix) && prefix.length() > 0) {
      publisher.addItem(
          new SnippetCompletionItem(
              "linkcss",
              "Snippet - Link CSS",
              new SnippetDescription(prefix.length(), LINK_CSS_SNIPPET, true)));
    }
    if ("scriptsrc".startsWith(prefix) && prefix.length() > 0) {
      publisher.addItem(
          new SnippetCompletionItem(
              "scriptsrc",
              "Snippet - Script src",
              new SnippetDescription(prefix.length(), SCRIPT_SRC_SNIPPET, true)));
    }
    if ("btn".startsWith(prefix) && prefix.length() > 0) {
      publisher.addItem(
          new SnippetCompletionItem(
              "btn",
              "Snippet - Button",
              new SnippetDescription(prefix.length(), BUTTON_SNIPPET, true)));
    }
    if ("inp".startsWith(prefix) && prefix.length() > 0) {
      publisher.addItem(
          new SnippetCompletionItem(
              "inp",
              "Snippet - Input field",
              new SnippetDescription(prefix.length(), INPUT_SNIPPET, true)));
    }
    if (prefix.length() > 0) {
      boolean insideTag = isInsideTag(content, position);
      if (insideTag) {
        for (HtmlAttributeCompletionItem item : HtmlHelper.getAttributeItemsByPrefix(prefix)) {
          publisher.addItem(item);
        }
      } else {
        for (HtmlTagCompletionItem item : HtmlHelper.getTagItemsByPrefix(prefix)) {
          publisher.addItem(item);
        }
      }
    } else {
      if (isInsideTag(content, position)) {
        for (HtmlAttributeCompletionItem item : HtmlHelper.getAllAttributeItems()) {
          publisher.addItem(item);
        }
      } else {
        for (HtmlTagCompletionItem item : HtmlHelper.getAllTagItems()) {
          publisher.addItem(item);
        }
      }
    }
  }

  private boolean isInsideStyleTag(ContentReference content, CharPosition pos) {
    try {
      int line = pos.line;
      int column = pos.column;
      boolean styleOpened = false;
      boolean styleClosed = false;
      String currentLine = content.getLine(line);
      int searchEnd = column;

      int styleStart = currentLine.lastIndexOf("<style", searchEnd);
      if (styleStart != -1) {
        int closeBracket = currentLine.indexOf('>', styleStart);
        if (closeBracket != -1 && closeBracket < searchEnd) {
          styleOpened = true;
        }
      }
      int styleEnd = currentLine.lastIndexOf("</style>", searchEnd);
      if (styleEnd != -1 && styleEnd + 8 <= searchEnd) {
        styleClosed = true;
      }

      if (styleClosed && styleOpened && styleEnd > styleStart) {
        return false;
      }
      if (styleOpened && !styleClosed) {
        return true;
      }

      for (int i = line - 1; i >= 0; i--) {
        String l = content.getLine(i);
        int startIdx = l.lastIndexOf("<style");
        if (startIdx != -1) {
          int closeIdx = l.indexOf('>', startIdx);
          if (closeIdx != -1) {
            styleOpened = true;
          }
          break;
        }
      }
      for (int i = line - 1; i >= 0; i--) {
        String l = content.getLine(i);
        int endIdx = l.lastIndexOf("</style>");
        if (endIdx != -1) {
          styleClosed = true;
          break;
        }
      }

      return styleOpened && !styleClosed;
    } catch (Exception ignored) {
      return false;
    }
  }

  private boolean isInsideTag(ContentReference content, CharPosition pos) {
    try {
      String line = content.getLine(pos.line);
      int column = pos.column;
      for (int i = column - 1; i >= 0; i--) {
        char c = line.charAt(i);
        if (c == '>') return false;
        if (c == '<') return true;
      }
    } catch (Exception ignored) {
    }
    return false;
  }

@Override
  public int getIndentAdvance(@NonNull ContentReference text, int line, int column) {

    try {
      var lexer = new HTMLLexer(CharStreams.fromReader(new StringReader(text.getLine(line))));
      Token token;
      int advance = 0;
      while (((token = lexer.nextToken()) != null && token.getType() != token.EOF)) {
        switch (token.getType()) {
          case HTMLLexer.LBRACE:
          case HTMLLexer.OPEN_SLASH:
            advance++;
            break;
          case HTMLLexer.SLASH_CLOSE:  
          case HTMLLexer.RBRACE:
            advance--;
            break;
        }
      }
      advance = Math.max(0, advance);
      return advance * 2;
    } catch (IOException e) {
      e.printStackTrace();
    }
    return 0;
  }


  @Override
  public boolean useTab() {
    return false;
  }

  @NonNull
  @Override
  public Formatter getFormatter() {
    return EmptyLanguage.EmptyFormatter.INSTANCE;
  }

  @Override
  public SymbolPairMatch getSymbolPairs() {
    return new SymbolPairMatch.DefaultSymbolPairs();
  }

  @Override
  public NewlineHandler[] getNewlineHandlers() {
    return null;
  }
}
