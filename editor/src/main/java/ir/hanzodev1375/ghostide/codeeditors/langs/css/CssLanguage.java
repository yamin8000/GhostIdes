package ir.hanzodev1375.ghostide.codeeditors.langs.css;

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
import ir.hanzodev1375.ghostide.codeeditors.langs.html.CssHelper;
import ir.hanzodev1375.ghostide.codeeditors.langs.html.CssCompletionItem;
import ir.hanzodev1375.ghostide.codeeditors.langs.html.HTMLAnalyzer;
import ir.hanzodev1375.ghostide.codeeditors.langs.html.HTMLLexer;
import ir.hanzodev1375.ghostide.codeeditors.langs.html.HtmlHelper;
import ir.hanzodev1375.ghostide.codeeditors.lspcustomhot.Css3Server;
import ir.hanzodev1375.ghostide.codeeditors.lspcustomhot.PathCompleter;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import ir.hanzodev1375.ghostide.codeeditors.lspcustomhot.CustomCompletionItem;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.Token;

public class CssLanguage implements Language {

  private final HTMLAnalyzer analyzer;
  private final IdentifierAutoComplete autoComplete;

  public CssLanguage() {
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
    autoComplete.requireAutoComplete(
        content, position, prefix, publisher, analyzer.getSyncIdentifiers());
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

  @Override
  public int getIndentAdvance(@NonNull ContentReference text, int line, int column) {

    try {
      var lexer = new HTMLLexer(CharStreams.fromReader(new StringReader(text.getLine(line))));
      Token token;
      int advance = 0;
      while (((token = lexer.nextToken()) != null && token.getType() != token.EOF)) {
        switch (token.getType()) {
          case HTMLLexer.LBRACE:
            advance++;
            break;
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
