/*
 * This file is part of CodeOps Studio.
 * CodeOps Studio - Code anywhere anytime
 * https://github.com/euptron/CodeOps-Studio
 * Copyright (C) 2024-2026 Etido Peter
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/
 */

package com.eup.codeopsstudio.editor.langs.widget.component;

import android.annotation.SuppressLint;
import android.graphics.RectF;
import android.graphics.drawable.GradientDrawable;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.TooltipCompat;

import io.github.rosemoe.sora.event.ColorSchemeUpdateEvent;
import io.github.rosemoe.sora.event.HandleStateChangeEvent;
import io.github.rosemoe.sora.event.InterceptTarget;
import io.github.rosemoe.sora.event.LongPressEvent;
import io.github.rosemoe.sora.event.ScrollEvent;
import io.github.rosemoe.sora.event.SelectionChangeEvent;
import io.github.rosemoe.sora.event.Unsubscribe;
import io.github.rosemoe.sora.text.Cursor;
import io.github.rosemoe.sora.widget.EditorTouchEventHandler;
import io.github.rosemoe.sora.widget.component.EditorAutoCompletion;
import io.github.rosemoe.sora.widget.component.EditorTextActionWindow;
import io.github.rosemoe.sora.widget.schemes.EditorColorScheme;

import ir.hanzodev1375.ghostide.codeeditors.IdeEditor;
import ir.hanzodev1375.ghostide.codeeditors.R;
import ir.hanzodev1375.ghostide.codeeditors.setting.PreferencesUtils;
import ir.hanzodev1375.ghostide.codeeditors.util.TranslateLanguages;

import ir.hanzodev1375.ghostide.codeeditors.util.TranslateTask;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import org.json.JSONArray;

/**
 * This window will show when selecting text to present text actions.
 *
 * @author Ghost
 */
public class CustomEditorTextActionWindow extends EditorTextActionWindow {

  private static final long DELAY = 200;
  private final IdeEditor editor;
  private final ImageButton pasteBtn;
  private final ImageButton copyBtn;
  private final ImageButton cutBtn;
  private final ImageButton selectAllBtn;
  private final ImageButton longSelectBtn;
  private final ImageButton expandSelectionBtn;
  private final ImageButton formatBtn;
  private final ImageButton translateBtn;
  private final View rootView;
  private final EditorTouchEventHandler handler;
  private long lastScroll;
  private int lastPosition;
  private int lastCause;

  private boolean enabled = true;
  private int windowCornerRadius = 8;

  public CustomEditorTextActionWindow(IdeEditor editor) {
    super(editor);
    this.editor = editor;
    handler = editor.getEventHandler();

    @SuppressLint("InflateParams")
    View root =
        LayoutInflater.from(editor.getContext())
            .inflate(R.layout.contextual_text_compose_panel, null);

    pasteBtn = root.findViewById(R.id.panel_btn_paste);
    copyBtn = root.findViewById(R.id.panel_btn_copy);
    cutBtn = root.findViewById(R.id.panel_btn_cut);
    selectAllBtn = root.findViewById(R.id.panel_btn_select_all);
    longSelectBtn = root.findViewById(R.id.panel_btn_long_select);
    expandSelectionBtn = root.findViewById(R.id.panel_btn_expand_selection);
    formatBtn = root.findViewById(R.id.panel_btn_format);
    translateBtn = root.findViewById(R.id.panel_btn_translate); // ← جدید

    pasteBtn.setOnClickListener(this);
    copyBtn.setOnClickListener(this);
    cutBtn.setOnClickListener(this);
    selectAllBtn.setOnClickListener(this);
    longSelectBtn.setOnClickListener(this);
    expandSelectionBtn.setOnClickListener(this);
    formatBtn.setOnClickListener(this);
    translateBtn.setOnClickListener(this);
    applyColorScheme(root, editor.getColorScheme());
    editor.subscribeEvent(
        ColorSchemeUpdateEvent.class,
        (event, unsubscribe) -> applyColorScheme(root, event.getColorScheme()));

    setContentView(root);
    setSize(0, (int) (this.editor.getDpUnit() * 48));
    rootView = root;

    editor.subscribeEvent(
        ScrollEvent.class,
        ((event, unsubscribe) -> {
          long last = lastScroll;
          lastScroll = System.currentTimeMillis();
          if (lastScroll - last < DELAY && lastCause != SelectionChangeEvent.CAUSE_SEARCH) {
            runPostDisplay();
          }
        }));
    editor.subscribeEvent(
        HandleStateChangeEvent.class,
        ((event, unsubscribe) -> {
          if (event.isHeld()) {
            runPostDisplay();
          }
        }));
    editor.subscribeEvent(
        LongPressEvent.class,
        ((event, unsubscribe) -> {
          if (editor.getCursor().isSelected() && lastCause == SelectionChangeEvent.CAUSE_SEARCH) {
            int idx = event.getIndex();
            if (idx >= editor.getCursor().getLeft() && idx <= editor.getCursor().getRight()) {
              lastCause = 0;
              displayWindow();
            }
            event.intercept(InterceptTarget.TARGET_EDITOR);
          }
        }));
    editor.subscribeEvent(
        HandleStateChangeEvent.class,
        ((event, unsubscribe) -> {
          if (!event.getEditor().getCursor().isSelected()
              && event.getHandleType() == HandleStateChangeEvent.HANDLE_TYPE_INSERT
              && !event.isHeld()) {
            displayWindow();
            editor.postDelayedInLifecycle(
                new Runnable() {
                  @Override
                  public void run() {
                    if (!editor.getEventHandler().shouldDrawInsertHandle()
                        && !editor.getCursor().isSelected()) {
                      dismiss();
                    } else if (!editor.getCursor().isSelected()) {
                      editor.postDelayedInLifecycle(this, 100);
                    }
                  }
                },
                100);
          }
        }));

    getPopup().setAnimationStyle(io.github.rosemoe.sora.R.style.text_action_popup_animation);
  }

  private void applyColorScheme(View root, EditorColorScheme scheme) {
    GradientDrawable gd = new GradientDrawable();
    gd.setCornerRadius(windowCornerRadius * editor.getDpUnit());
    gd.setColor(scheme.getColor(EditorColorScheme.COMPLETION_WND_BACKGROUND));
    gd.setStroke(1, scheme.getColor(EditorColorScheme.COMPLETION_WND_CORNER));
    root.setBackground(gd);

    int textColor = scheme.getColor(EditorColorScheme.COMPLETION_WND_TEXT_SECONDARY);
    setColorFilterById(textColor, pasteBtn);
    setColorFilterById(textColor, copyBtn);
    setColorFilterById(textColor, cutBtn);
    setColorFilterById(textColor, expandSelectionBtn);
    setColorFilterById(textColor, longSelectBtn);
    setColorFilterById(textColor, formatBtn);
    setColorFilterById(textColor, selectAllBtn);
    setColorFilterById(textColor, translateBtn);
  }

  @Override
  public boolean isEnabled() {
    return enabled;
  }

  @Override
  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
    if (!enabled) dismiss();
  }

  @Override
  public ViewGroup getView() {
    return (ViewGroup) getPopup().getContentView();
  }

  public void onReceive(@NonNull SelectionChangeEvent event, @NonNull Unsubscribe unsubscribe) {
    if (handler.hasAnyHeldHandle()) return;
    lastCause = event.getCause();

    if (event.isSelected()) {
      if (event.getCause() != SelectionChangeEvent.CAUSE_SEARCH) {
        editor.postInLifecycle(this::displayWindow);
      } else {
        dismiss();
      }
      lastPosition = -1;
    } else {
      boolean show = false;
      if (event.getCause() == SelectionChangeEvent.CAUSE_TAP
          && event.getLeft().index == lastPosition
          && !isShowing()
          && !editor.getText().isInBatchEdit()
          && editor.isEditable()) {
        editor.postInLifecycle(this::displayWindow);
        show = true;
      } else {
        dismiss();
      }
      if (event.getCause() == SelectionChangeEvent.CAUSE_TAP && !show) {
        lastPosition = event.getLeft().index;
      } else {
        lastPosition = -1;
      }
    }
  }

  @Override
  public void displayWindow() {
    updateButtonState();
    int top;
    Cursor cursor = editor.getCursor();
    if (cursor.isSelected()) {
      RectF leftRect = editor.getLeftHandleDescriptor().position;
      RectF rightRect = editor.getRightHandleDescriptor().position;
      top = Math.min(selectTopRect(leftRect), selectTopRect(rightRect));
    } else {
      top = selectTopRect(editor.getInsertHandleDescriptor().position);
    }
    top = Math.max(0, Math.min(top, editor.getHeight() - getHeight() - 5));
    float handleLeftX =
        editor.getOffset(editor.getCursor().getLeftLine(), editor.getCursor().getLeftColumn());
    float handleRightX =
        editor.getOffset(editor.getCursor().getRightLine(), editor.getCursor().getRightColumn());
    int panelX = (int) ((handleLeftX + handleRightX) / 2f - rootView.getMeasuredWidth() / 2f);
    setLocationAbsolutely(panelX, top);
    show();
  }

  private int selectTopRect(@NonNull RectF rect) {
    int rowHeight = editor.getRowHeight();
    if (rect.top - rowHeight * 3 / 2F > getHeight()) {
      return (int) (rect.top - (float) (rowHeight * 3) / 2 - getHeight());
    } else {
      return (int) (rect.bottom + (float) rowHeight / 2);
    }
  }

  private void updateButtonState() {
    pasteBtn.setEnabled(editor.hasClip());
    boolean isSelected = editor.getCursor().isSelected();
    boolean isEditable = editor.isEditable();

    copyBtn.setVisibility(isSelected ? View.VISIBLE : View.GONE);
    formatBtn.setVisibility(View.VISIBLE);
    cutBtn.setVisibility((isSelected && isEditable) ? View.VISIBLE : View.GONE);
    pasteBtn.setVisibility(isEditable ? View.VISIBLE : View.GONE);
    longSelectBtn.setVisibility((!isSelected && isEditable) ? View.VISIBLE : View.GONE);
    expandSelectionBtn.setVisibility(View.GONE);
    translateBtn.setVisibility(isSelected ? View.VISIBLE : View.GONE);

    rootView.measure(
        View.MeasureSpec.makeMeasureSpec(1000000, View.MeasureSpec.AT_MOST),
        View.MeasureSpec.makeMeasureSpec(100000, View.MeasureSpec.AT_MOST));
    setSize(Math.min(rootView.getMeasuredWidth(), (int) (editor.getDpUnit() * 230)), getHeight());
  }

  @Override
  public void show() {
    if (!enabled || editor.getSnippetController().isInSnippet()) return;
    super.show();
  }

  @Override
  public void onClick(@NonNull View view) {
    int id = view.getId();
    if (id == R.id.panel_btn_select_all) {
      attachTooltip(selectAllBtn, "Select all");
      editor.selectAll();
      return;
    } else if (id == R.id.panel_btn_cut) {
      attachTooltip(cutBtn, "Cut");
      if (editor.getCursor().isSelected()) editor.cutText();
    } else if (id == R.id.panel_btn_paste) {
      attachTooltip(pasteBtn, "Paste");
      editor.pasteText();
      editor.setSelection(editor.getCursor().getRightLine(), editor.getCursor().getRightColumn());
    } else if (id == R.id.panel_btn_copy) {
      attachTooltip(copyBtn, "Copy");
      editor.copyText();
      editor.setSelection(editor.getCursor().getRightLine(), editor.getCursor().getRightColumn());
    } else if (id == R.id.panel_btn_long_select) {
      attachTooltip(longSelectBtn, "Long select");
      editor.beginLongSelect();
    } else if (id == R.id.panel_btn_format) {
      attachTooltip(formatBtn, "Format");
      Cursor cursor = editor.getText().getCursor();
      if (cursor.isSelected()) {
        editor.formatCodeAsync(cursor.left(), cursor.right());
      } else {
        editor.formatCodeAsync();
      }
      return;
    } else if (id == R.id.panel_btn_expand_selection) {
      attachTooltip(expandSelectionBtn, "Expand selection");
      if (editor.getEditable()) {
        // TODO: Handle
      }
    } else if (id == R.id.panel_btn_translate) {
      attachTooltip(translateBtn, "Translate");
      handleTranslate();
      return;
    }
    dismiss();
  }

  public String getSelectedText() {
    Cursor cursor = editor.getCursor();
    return editor
        .getText()
        .subContent(
            cursor.getLeftLine(),
            cursor.getLeftColumn(),
            cursor.getRightLine(),
            cursor.getRightColumn())
        .toString();
  }

  private void handleTranslate() {
    String selectedText = getSelectedText();
    if (selectedText == null || selectedText.isEmpty()) {
      Toast.makeText(editor.getContext(), "متنی انتخاب نشده است", Toast.LENGTH_SHORT).show();
      dismiss();
      return;
    }

    PreferencesUtils prefs = new PreferencesUtils(editor.getContext());
    String targetLang = prefs.getTranslateTargetLang();

    dismiss();
    new TranslateTask(
            selectedText,
            targetLang,
            new TranslateTask.Callback() {
              @Override
              public void onSuccess(String translatedText) {
                editor.post(
                    () -> {
                      if (editor.getCursor().isSelected()) {
                        editor.deleteText();
                      }
                      editor.insertText(translatedText, translatedText.length());
                    });
              }

              @Override
              public void onFailure(String error) {
                editor.post(
                    () ->
                        Toast.makeText(editor.getContext(), "error: " + error, Toast.LENGTH_SHORT)
                            .show());
              }
            })
        .execute();
  }

  private void attachTooltip(View anchor, String text) {
    TooltipCompat.setTooltipText(anchor, text);
  }

  public void setWindowCornerRadius(final int radius) {
    windowCornerRadius = radius;
  }

  private void runPostDisplay() {
    if (!isShowing()) return;
    dismiss();
    if (!editor.getCursor().isSelected()) return;
    editor.postDelayedInLifecycle(
        new Runnable() {
          @Override
          public void run() {
            if (!handler.hasAnyHeldHandle()
                && !editor.getSnippetController().isInSnippet()
                && System.currentTimeMillis() - lastScroll > DELAY
                && editor.getScroller().isFinished()) {
              displayWindow();
            } else {
              editor.postDelayedInLifecycle(this, DELAY);
            }
          }
        },
        DELAY);
  }

  void setColorFilterById(int color, ImageButton btn) {
    btn.setColorFilter(color);
  }
}
