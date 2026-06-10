// CodeAnalyzer.java
package ir.hanzodev1375.ghostide.codeeditors.langs.antlr4base;

import androidx.annotation.NonNull;
import com.blankj.utilcode.util.ArrayUtils;
import com.google.common.base.Preconditions;
import com.google.common.collect.EvictingQueue;
import io.github.rosemoe.sora.lang.analysis.AsyncIncrementalAnalyzeManager;
import io.github.rosemoe.sora.lang.completion.IdentifierAutoComplete;
import io.github.rosemoe.sora.lang.styling.CodeBlock;
import io.github.rosemoe.sora.lang.styling.Span;
import io.github.rosemoe.sora.lang.styling.TextStyle;
import io.github.rosemoe.sora.text.Content;
import io.github.rosemoe.sora.util.IntPair;
import io.github.rosemoe.sora.widget.schemes.EditorColorScheme;
import ir.hanzodev1375.ghostide.codeeditors.colorscheme.GhostColorScheme;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Stack;
import java.util.stream.Collectors;
import kotlin.Pair;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.Token;

public abstract class CodeAnalyzer
    extends AsyncIncrementalAnalyzeManager<LineState, IncrementalToken> {

  protected final Lexer lexer;
  private final int[] multilineStartTypes;
  private final int[] multilineEndTypes;
  private final int[] blockTokens;
  protected final IdentifierAutoComplete.SyncIdentifiers syncIdentifiers;

  public static final int[] RAINBOW = {
    GhostColorScheme.KEYWORD,
    GhostColorScheme.OPERATOR,
    GhostColorScheme.ATTRIBUTE_NAME,
    GhostColorScheme.ATTRIBUTE_VALUE,
    GhostColorScheme.HTML_TAG,
    GhostColorScheme.ANNOTATION
  };

  public CodeAnalyzer(final Class<? extends Lexer> lexer) {
    Objects.requireNonNull(lexer, "Cannot create analyzer manager for null lexer");
    this.lexer = createLexerInstance(lexer);
    this.syncIdentifiers = new IdentifierAutoComplete.SyncIdentifiers();

    var multilineTokenTypes = getMultilineTokenStartEndTypes();
    verifyMultilineTypes(multilineTokenTypes);

    this.multilineStartTypes = multilineTokenTypes[0];
    this.multilineEndTypes = multilineTokenTypes[1];

    var tokens = getCodeBlockTokens();
    if (tokens == null) {
      tokens = new int[0];
    }
    this.blockTokens = tokens;
  }

  @NonNull
  private Lexer createLexerInstance(final Class<? extends Lexer> lexer) {
    try {
      final var constructor = lexer.getConstructor(CharStream.class);
      if (!constructor.isAccessible()) {
        constructor.setAccessible(true);
      }
      return constructor.newInstance(createStream(""));
    } catch (Throwable err) {
      throw new RuntimeException("Unable to create Lexer instance", err);
    }
  }

  @NonNull
  protected CharStream createStream(@NonNull CharSequence source) {
    Objects.requireNonNull(source);
    try {
      return CharStreams.fromReader(new CharSequenceReader(source));
    } catch (IOException e) {
      throw new RuntimeException("Cannot create CharStream for source", e);
    }
  }

  private void verifyMultilineTypes(@NonNull final int[][] types) {
    Preconditions.checkState(
        types.length == 2, "There must be exact two inner int[] in multiline token types");

    final var start = types[0];
    final var end = types[1];
    Preconditions.checkState(start.length > 0, "Invalid start token types");
    Preconditions.checkState(end.length > 0, "Invalid end token types");
  }

  protected abstract int[][] getMultilineTokenStartEndTypes();

  protected abstract int[] getCodeBlockTokens();

  protected abstract boolean isIdentifierToken(int tokenType);

  @Override
  public LineState getInitialState() {
    return new LineState();
  }

  @Override
  public boolean stateEquals(final LineState state, final LineState another) {
    return state.equals(another);
  }

  @Override
  public void onAddState(LineState state) {
    if (state.identifiers != null) {
      for (String identifier : state.identifiers) {
        syncIdentifiers.identifierIncrease(identifier);
      }
    }
  }

  @Override
  public void onAbandonState(LineState state) {
    if (state.identifiers != null) {
      for (String identifier : state.identifiers) {
        syncIdentifiers.identifierDecrease(identifier);
      }
    }
  }

  @Override
  public void reset(
      @NonNull io.github.rosemoe.sora.text.ContentReference content,
      @NonNull android.os.Bundle extraArguments) {
    super.reset(content, extraArguments);
    syncIdentifiers.clear();
  }

  @Override
  public LineTokenizeResult<LineState, IncrementalToken> tokenizeLine(
      final CharSequence lineText, final LineState state, final int line) {
    final var tokens = new ArrayList<IncrementalToken>();
    var newState = 0;
    var stateObj = new LineState();
    if (state.state == LineState.NORMAL) {
      newState = tokenizeNormal(lineText, 0, tokens, stateObj, state.lexerMode);
    } else if (state.state == LineState.INCOMPLETE) {
      final var result = fillIncomplete(lineText, tokens, state.lexerMode);
      newState = IntPair.getFirst(result);
      if (newState == LineState.NORMAL) {
        newState =
            tokenizeNormal(lineText, IntPair.getSecond(result), tokens, stateObj, state.lexerMode);
      } else {
        newState = LineState.INCOMPLETE;
      }
    }
    stateObj.state = newState;
    stateObj.lexerMode = lexer._mode;
    // new Api
    stateObj.lineNumber = line;
    return new LineTokenizeResult<>(stateObj, tokens);
  }

  @Override
  public List<Span> generateSpansForLine(
      final LineTokenizeResult<LineState, IncrementalToken> tokens) {
    var result = generateSpans(tokens);
    Objects.requireNonNull(result);
    if (result.isEmpty()) {
      result.add(Span.obtain(0, TextStyle.makeStyle(EditorColorScheme.TEXT_NORMAL)));
    }
    return result;
  }

  protected abstract List<Span> generateSpans(
      final LineTokenizeResult<LineState, IncrementalToken> tokens);

  @Override
  public List<CodeBlock> computeBlocks(
      final Content text,
      final AsyncIncrementalAnalyzeManager<LineState, IncrementalToken>.CodeBlockAnalyzeDelegate
          delegate) {
    final var stack = new Stack<CodeBlock>();
    final var blocks = new ArrayList<CodeBlock>();
    var line = 0;
    var maxSwitch = 0;
    var currSwitch = 0;
    while (delegate.isNotCancelled() && line < text.getLineCount()) {
      final var tokens = getState(line);
      final var checkForIdentifiers =
          tokens.state.state == LineState.NORMAL
              || (tokens.state.state == LineState.INCOMPLETE && tokens.tokens.size() > 1);
      if (!tokens.state.hasBraces && !checkForIdentifiers) {
        line++;
        continue;
      }

      for (int i = 0; i < tokens.tokens.size(); i++) {
        final var token = tokens.tokens.get(i);
        var offset = token.getStartIndex();
        if (isCodeBlockStart(token)) {
          if (stack.isEmpty()) {
            if (currSwitch > maxSwitch) {
              maxSwitch = currSwitch;
            }
            currSwitch = 0;
          }
          currSwitch++;
          CodeBlock block = new CodeBlock();
          block.startLine = line;
          block.startColumn = offset;
          stack.push(block);
        } else if (isCodeBlockEnd(token)) {
          if (!stack.isEmpty()) {
            CodeBlock block = stack.pop();
            block.endLine = line;
            block.endColumn = offset;
            if (block.startLine != block.endLine) {
              blocks.add(block);
            }
          }
        }
      }

      line++;
    }
    blocks.sort(CodeBlock.COMPARATOR_END);
    return blocks;
  }

  protected boolean isCodeBlockStart(IncrementalToken token) {
    return blockTokens.length == 2 && token.getType() == blockTokens[0];
  }

  protected boolean isCodeBlockEnd(IncrementalToken token) {
    return blockTokens.length == 2 && token.getType() == blockTokens[1];
  }

  @SuppressWarnings("UnstableApiUsage")
  protected boolean isIncompleteTokenStart(EvictingQueue<IncrementalToken> q) {
    return matchTokenTypes(this.multilineStartTypes, q);
  }

  @SuppressWarnings("UnstableApiUsage")
  protected boolean isIncompleteTokenEnd(EvictingQueue<IncrementalToken> q) {
    return matchTokenTypes(this.multilineEndTypes, q);
  }

  protected abstract void handleIncompleteToken(IncrementalToken token);

  protected IncrementalToken nextToken() {
    return new IncrementalToken(lexer.nextToken());
  }

  protected void popTokensAfterIncomplete(
      @NonNull IncrementalToken incompleteToken, @NonNull List<IncrementalToken> tokens) {
    tokens.remove(tokens.size() - 1);
  }

  @SuppressWarnings("UnstableApiUsage")
  protected int tokenizeNormal(
      final CharSequence line,
      final int column,
      final List<IncrementalToken> tokens,
      final LineState st,
      final int lexerMode) {
    lexer.setInputStream(createStream(line));
    if (lexer._mode != lexerMode) {
      lexer.pushMode(lexerMode);
    }
    final var queues = createEvictingQueueForTokens();
    final var start = queues.getFirst();
    final var end = queues.getSecond();
    var isInIncompleteToken = false;
    var state = LineState.NORMAL;
    IncrementalToken token;
    IncrementalToken incompleteToken = null;

    while ((token = nextToken()) != null) {
      if (token.getType() == IncrementalToken.EOF) {
        break;
      }

      if (token.getStartIndex() < column) {
        continue;
      }

      if (!isInIncompleteToken) {
        if (token.getStartIndex() == column && !tokens.isEmpty()) {
          token.type = tokens.get(tokens.size() - 1).getType();
        }

        tokens.add(token);
      }
      start.add(token);
      end.add(token);
      final var type = token.getType();
      if (ArrayUtils.contains(getCodeBlockTokens(), type)) {
        st.hasBraces = true;
      }

      if (isIdentifierToken(type)) {
        st.addIdentifier(token.getText());
      }

      if (start.remainingCapacity() == 0 && isIncompleteTokenStart(start)) {
        isInIncompleteToken = true;
        incompleteToken = start.poll();
        popTokensAfterIncomplete(Objects.requireNonNull(incompleteToken), tokens);
      } else if (end.remainingCapacity() == 0 && isIncompleteTokenEnd(end)) {
        isInIncompleteToken = false;
        incompleteToken = null;
      }

      if (isInIncompleteToken) {
        state = LineState.INCOMPLETE;
      }
    }

    if (incompleteToken != null) {
      incompleteToken.incomplete = true;
      handleIncompleteToken(incompleteToken);
    }

    return state;
  }

  @SuppressWarnings("UnstableApiUsage")
  protected long fillIncomplete(
      CharSequence line, final List<IncrementalToken> tokens, final int lexerMode) {
    lexer.setInputStream(createStream(line));
    if (lexer._mode != lexerMode) {
      lexer.pushMode(lexerMode);
    }
    final var queue = createEvictingQueueForTokens();
    final var end = queue.getSecond();
    final var allTokens =
        lexer.getAllTokens().stream().map(IncrementalToken::new).collect(Collectors.toList());
    if (allTokens.isEmpty()) {
      return IntPair.pack(LineState.INCOMPLETE, 0);
    }
    var completed = false;
    var index = 0;
    for (index = 0; index < allTokens.size(); index++) {
      final IncrementalToken token = allTokens.get(index);
      if (token.getType() == Token.EOF) {
        break;
      }

      end.add(token);
      if (end.remainingCapacity() == 0 && isIncompleteTokenEnd(end)) {
        completed = true;
        break;
      }
    }

    final var first = allTokens.get(0);
    final int offset = allTokens.get(completed ? index : index - 1).getStartIndex();
    first.startIndex = 0;
    handleIncompleteToken(first);
    tokens.add(first);
    if (completed) {
      return IntPair.pack(LineState.NORMAL, offset);
    } else {
      return IntPair.pack(LineState.INCOMPLETE, offset);
    }
  }

  @SuppressWarnings("UnstableApiUsage")
  @NonNull
  private Pair<EvictingQueue<IncrementalToken>, EvictingQueue<IncrementalToken>>
      createEvictingQueueForTokens() {
    return new Pair<>(
        EvictingQueue.create(multilineStartTypes.length),
        EvictingQueue.create(multilineEndTypes.length));
  }

  @SuppressWarnings("UnstableApiUsage")
  private boolean matchTokenTypes(
      @NonNull int[] types, @NonNull EvictingQueue<IncrementalToken> tokens) {
    final var arr = tokens.toArray(new IncrementalToken[0]);
    for (int i = 0; i < types.length; i++) {
      if (types[i] != arr[i].getType()) {
        return false;
      }
    }
    return true;
  }
}
