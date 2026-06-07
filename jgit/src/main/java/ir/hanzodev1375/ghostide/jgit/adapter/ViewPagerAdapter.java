package ir.hanzodev1375.ghostide.jgit.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import ir.hanzodev1375.ghostide.jgit.fragments.ChangedFilesFragment;
import ir.hanzodev1375.ghostide.jgit.fragments.CommitHistoryFragment;
import ir.hanzodev1375.ghostide.jgit.fragments.BranchesFragment;
import ir.hanzodev1375.ghostide.jgit.fragments.RemotesFragment;
import java.util.List;

public class ViewPagerAdapter extends FragmentStateAdapter {
  private final List<String> titles;

  public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity, List<String> titles) {
    super(fragmentActivity);
    this.titles = titles;
  }

  @NonNull
  @Override
  public Fragment createFragment(int position) {
    switch (position) {
      case 0:
        return new ChangedFilesFragment();
      case 1:
        return new CommitHistoryFragment();
      case 2:
        return new BranchesFragment();
      case 3:
        return new RemotesFragment();
      default:
        return new ChangedFilesFragment();
    }
  }

  @Override
  public int getItemCount() {
    return 4;
  }

  public String getPageTitle(int position) {
    return titles.get(position);
  }
}
