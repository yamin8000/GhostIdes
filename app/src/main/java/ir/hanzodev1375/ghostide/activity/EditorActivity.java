package ir.hanzodev1375.ghostide.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.PopupMenu;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import ir.hanzodev1375.ghostide.codeeditors.IdeEditor;
import androidx.fragment.app.Fragment;
import ir.hanzodev1375.ghostide.fragments.EditorFragment;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import ir.hanzodev1375.ghostide.R;
import ir.hanzodev1375.ghostide.adapters.EditorPagerAdapter;
import ir.hanzodev1375.ghostide.databinding.ActivityEditorBinding;
import ir.hanzodev1375.ghostide.models.TabModel;
import ir.theme.ThemeManager;
import ir.theme.ThemeUtils;

public class EditorActivity extends BaseCompat {

  private ActivityEditorBinding binding;
  private EditorPagerAdapter adapter;
  private ThemeUtils theme;
  private List<TabModel> tabsList = new ArrayList<>();
  private SharedPreferences prefs;
  private Gson gson = new Gson();
  private static final String KEY_TABS = "path";
  private static final String KEY_POSITION = "positionTabs";
  private TabLayoutMediator tabMediator;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    binding = ActivityEditorBinding.inflate(getLayoutInflater());
    setContentView(binding.getRoot());

    prefs = getSharedPreferences("editor", MODE_PRIVATE);

    setupViewPager();
    setupTabLayout();
    setupFAB();
    loadSavedTabs();

    ThemeManager manager = new ThemeManager(this);
    theme = new ThemeUtils(manager);
    theme.applyActivity(this);
    theme.applyFab(binding.fabineditor);
    theme.applyTabLayout(binding.tab);

    String path = getIntent().getStringExtra("file_path");
    String name = getIntent().getStringExtra("file_name");
    if (path != null && name != null) {
      openFile(path, name);
    }
  }

  private void setupViewPager() {
    adapter = new EditorPagerAdapter(this, new ArrayList<>());
    binding.viewPager.setAdapter(adapter);
    binding.viewPager.setUserInputEnabled(false);
  }

  private void setupTabLayout() {
    if (tabMediator != null) {
      tabMediator.detach();
    }
    tabMediator =
        new TabLayoutMediator(
            binding.tab,
            binding.viewPager,
            (tab, position) -> {
              if (position < tabsList.size()) {
                tab.setText(tabsList.get(position).getFileName());
              }
            });
    tabMediator.attach();

    binding.tab.addOnTabSelectedListener(
        new TabLayout.OnTabSelectedListener() {
          @Override
          public void onTabSelected(TabLayout.Tab tab) {
            int position = tab.getPosition();
            if (binding.viewPager.getCurrentItem() != position) {
              binding.viewPager.setCurrentItem(position, false);
            }
            saveCurrentPosition(position);
          }

          @Override
          public void onTabUnselected(TabLayout.Tab tab) {}

          @Override
          public void onTabReselected(TabLayout.Tab tab) {
            showPopupMenu(tab.view, tab.getPosition());
          }
        });

    binding.viewPager.registerOnPageChangeCallback(
        new ViewPager2.OnPageChangeCallback() {
          @Override
          public void onPageSelected(int position) {
            super.onPageSelected(position);
            TabLayout.Tab tab = binding.tab.getTabAt(position);
            if (tab != null && !tab.isSelected()) {
              tab.select();
            }
            saveCurrentPosition(position);
          }
        });
  }

  private void loadSavedTabs() {
    String json = prefs.getString(KEY_TABS, "");
    if (!json.isEmpty()) {
      try {
        Type type = new TypeToken<List<TabModel>>() {}.getType();
        List<TabModel> saved = gson.fromJson(json, type);
        if (saved != null) {
          tabsList = saved;
        }
      } catch (Exception e) {
        tabsList = new ArrayList<>();
      }
    } else {
      tabsList = new ArrayList<>();
    }

    adapter.setTabs(new ArrayList<>(tabsList));

    int savedPosition = 0;
    String posStr = prefs.getString(KEY_POSITION, "0");
    try {
      savedPosition = Integer.parseInt(posStr);
    } catch (NumberFormatException e) {
      savedPosition = 0;
    }
    if (!tabsList.isEmpty() && savedPosition >= 0 && savedPosition < tabsList.size()) {
      binding.viewPager.setCurrentItem(savedPosition, false);
      binding.tab.setScrollPosition(savedPosition, 0f, true);
    }
  }

  private void saveCurrentPosition(int position) {
    prefs.edit().putString(KEY_POSITION, String.valueOf(position)).apply();
  }

  private void saveTabs() {
    String json = gson.toJson(tabsList);
    prefs.edit().putString(KEY_TABS, json).apply();
  }

  private void openFile(String path, String name) {
    // بررسی وجود تکراری
    for (int i = 0; i < tabsList.size(); i++) {
      if (tabsList.get(i).getFilePath().equals(path)) {
        binding.viewPager.setCurrentItem(i);
        return;
      }
    }
    tabsList.add(new TabModel(path, name));
    adapter.setTabs(new ArrayList<>(tabsList));
    saveTabs();
    int newPos = tabsList.size() - 1;
    binding.viewPager.setCurrentItem(newPos);
    saveCurrentPosition(newPos);
  }

  private void closeTab(int position) {
    if (position >= 0 && position < tabsList.size()) {
      if (tabsList.get(position).isPinned()) {
        // می‌توانید پیام دهید که پین شده
        return;
      }
      tabsList.remove(position);
      adapter.setTabs(new ArrayList<>(tabsList));
      saveTabs();
      if (tabsList.isEmpty()) {
        finish();
        return;
      }
      int newPos = Math.min(position, tabsList.size() - 1);
      binding.viewPager.setCurrentItem(newPos);
      saveCurrentPosition(newPos);
    }
  }

  private void closeOtherTabs(int position) {
    if (position < 0 || position >= tabsList.size()) return;
    TabModel current = tabsList.get(position);
    List<TabModel> newList = new ArrayList<>();
    newList.add(current);
    for (int i = 0; i < tabsList.size(); i++) {
      if (i != position && tabsList.get(i).isPinned()) {
        newList.add(tabsList.get(i));
      }
    }
    tabsList = newList;
    adapter.setTabs(new ArrayList<>(tabsList));
    saveTabs();
    int newPos = 0;
    binding.viewPager.setCurrentItem(newPos);
    saveCurrentPosition(newPos);
  }

  private void closeAllTabs() {
    List<TabModel> pinned = new ArrayList<>();
    for (TabModel tab : tabsList) {
      if (tab.isPinned()) pinned.add(tab);
    }
    tabsList = pinned;
    adapter.setTabs(new ArrayList<>(tabsList));
    saveTabs();
    if (tabsList.isEmpty()) {
      finish();
    } else {
      binding.viewPager.setCurrentItem(0);
      saveCurrentPosition(0);
    }
  }

  private void togglePin(int position) {
    if (position >= 0 && position < tabsList.size()) {
      TabModel tab = tabsList.get(position);
      tab.setPinned(!tab.isPinned());
      adapter.setTabs(new ArrayList<>(tabsList));
      saveTabs();
      // آپدیت ظاهر تب (اختیاری)
      binding.tab.getTabAt(position).setText(tab.getFileName());
    }
  }

  private void setupFAB() {
    binding.fabineditor.setOnClickListener(
        v -> {
          Intent intent = new Intent(this, FileManagerActivity.class);
          startActivityForResult(intent, 100);
        });
  }

  private void showPopupMenu(View anchor, int position) {
    PopupMenu popup = new PopupMenu(this, anchor);
    popup.inflate(R.menu.tab_menu);
    popup.setOnMenuItemClickListener(
        item -> {
          int id = item.getItemId();
          if (id == R.id.close) {
            closeTab(position);
          } else if (id == R.id.close_others) {
            closeOtherTabs(position);
          } else if (id == R.id.close_all) {
            closeAllTabs();
          } else if (id == R.id.pin) {
            togglePin(position);
          }
          return true;
        });
    popup.show();
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == 100 && resultCode == RESULT_OK && data != null) {
      String path = data.getStringExtra("selected_file_path");
      String name = data.getStringExtra("selected_file_name");
      if (path != null) {
        openFile(path, name);
      }
    }
  }

  private IdeEditor getCurrentEditor() {
    if (adapter == null || binding.viewPager.getCurrentItem() < 0) return null;
    Fragment fragment =
        getSupportFragmentManager().findFragmentByTag("f" + binding.viewPager.getCurrentItem());
    if (fragment instanceof EditorFragment) {
      return ((EditorFragment) fragment).getEditor();
    }
    return null;
  }
}
