package ir.hanzodev1375.ghostide.mvvm.viewmodel;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import ir.hanzodev1375.ghostide.utils.FileUtil;
import ir.hanzodev1375.ghostide.utils.PathManager;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import android.os.Environment;
import ir.hanzodev1375.ghostide.enums.FileState;
import ir.hanzodev1375.ghostide.models.FileManagerModel;

public class FileViewModel extends AndroidViewModel {

  private MutableLiveData<List<FileManagerModel>> filesLiveData = new MutableLiveData<>();
  private MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
  private MutableLiveData<String> currentPath = new MutableLiveData<>();
  private PathManager pathManager;

  public FileViewModel(Application app) {
    super(app);
    pathManager = new PathManager(app.getApplicationContext());
    
    // تعیین مسیر اولیه
    String initialPath;
    if (pathManager.isSaveEnabled()) {
      String savedPath = pathManager.getLastPath();
      if (FileUtil.isExists(savedPath)) {
        initialPath = savedPath;
      } else {
        initialPath = Environment.getExternalStorageDirectory().getAbsolutePath();
      }
    } else {
      initialPath = Environment.getExternalStorageDirectory().getAbsolutePath();
    }
    
    currentPath.setValue(initialPath);
    loadFiles(initialPath);
  }

  public LiveData<List<FileManagerModel>> getFiles() {
    return filesLiveData;
  }

  public LiveData<Boolean> getIsLoading() {
    return isLoading;
  }

  public LiveData<String> getCurrentPath() {
    return currentPath;
  }

  public void savePath(boolean save) {
    pathManager.savePath(save);
    if (save) {
      pathManager.setLastPath(currentPath.getValue());
    } else {
      pathManager.clearLastPath();
    }
  }

  public void navigateTo(String path) {
    File file = new File(path);
    if (file.exists() && file.isDirectory()) {
      currentPath.setValue(path);
      loadFiles(path);
      
      if (pathManager.isSaveEnabled()) {
        pathManager.setLastPath(path);
      }
    }
  }

  public void navigateUp() {
    String parent = new File(currentPath.getValue()).getParent();
    if (parent != null) {
      navigateTo(parent);
    }
  }

  private void loadFiles(String dirPath) {
    isLoading.setValue(true);
    new Thread(() -> {
      List<FileManagerModel> list = new ArrayList<>();
      File dir = new File(dirPath);
      File[] files = dir.listFiles();
      if (files != null) {
        Arrays.sort(files,
            Comparator.comparing(File::isDirectory)
                .reversed()
                .thenComparing(File::getName));
        for (File file : files) {
          String name = file.getName();
          if (!name.startsWith(".")) {
            FileState state = file.isDirectory() ? FileState.CREATOR : FileState.SERACH;
            FileManagerModel model = new FileManagerModel(
                file.getAbsolutePath(), name, state, file.lastModified());
            list.add(model);
          }
        }
      }
      filesLiveData.postValue(list);
      isLoading.postValue(false);
    }).start();
  }

  public void renameFile(FileManagerModel model, String newName) {
    File oldFile = new File(model.getPath());
    String newPath = oldFile.getParent() + "/" + newName;
    File newFile = new File(newPath);
    if (oldFile.renameTo(newFile)) {
      model.setState(FileState.RENAME);
      loadFiles(currentPath.getValue());
    }
  }

  public void deleteFile(FileManagerModel model) {
    File file = new File(model.getPath());
    if (file.delete()) {
      loadFiles(currentPath.getValue());
    }
  }
}