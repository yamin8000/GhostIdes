package ir.hanzodev1375.ghostide.mvvm.viewmodel;

import android.app.Application;

import android.os.Handler;
import android.os.Looper;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import ir.hanzodev1375.ghostide.utils.FileUtil;
import ir.hanzodev1375.ghostide.utils.PathManager;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
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
    isLoading.postValue(true); // استفاده از postValue
    new Thread(
            () -> {
              List<FileManagerModel> list = new ArrayList<>();
              File dir = new File(dirPath);
              File[] files = dir.listFiles();
              if (files != null) {
                Arrays.sort(
                    files,
                    Comparator.comparing(File::isDirectory)
                        .reversed()
                        .thenComparing(File::getName, String.CASE_INSENSITIVE_ORDER));
                for (File file : files) {
                  String name = file.getName();
                  if (!name.startsWith(".")) {
                    FileState state = file.isDirectory() ? FileState.CREATOR : FileState.SERACH;
                    FileManagerModel model =
                        new FileManagerModel(
                            file.getAbsolutePath(), name, state, file.lastModified());
                    list.add(model);
                  }
                }
              }
              filesLiveData.postValue(list);
              isLoading.postValue(false);
            })
        .start();
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

  public void deleteFiles(List<FileManagerModel> items) {
    new Thread(
            () -> {
              for (FileManagerModel item : items) {
                File file = new File(item.getPath());
                deleteRecursive(file);
              }
              loadFiles(currentPath.getValue());
            })
        .start();
  }

  private void deleteRecursive(File file) {
    if (file.isDirectory()) {
      File[] children = file.listFiles();
      if (children != null) {
        for (File child : children) deleteRecursive(child);
      }
    }
    file.delete();
  }

  public void pasteFiles(
      List<FileManagerModel> sources, String destDir, boolean isCut, OnPasteComplete callback) {
    new Thread(
            () -> {
              boolean success = true;
              File destFolder = new File(destDir);
              if (!destFolder.exists()) destFolder.mkdirs();
              for (FileManagerModel model : sources) {
                File src = new File(model.getPath());
                File dest = new File(destFolder, src.getName());
                if (isCut) {
                  if (!src.renameTo(dest)) {
                    copyRecursive(src, dest);
                    deleteRecursive(src);
                  }
                } else {
                  copyRecursive(src, dest);
                }
              }
              loadFiles(currentPath.getValue());
              if (callback != null) {
                new Handler(Looper.getMainLooper()).post(() -> callback.onComplete(success));
              }
            })
        .start();
  }

  private void copyRecursive(File src, File dest) {
    try {
      if (src.isDirectory()) {
        if (!dest.exists()) dest.mkdirs();
        File[] children = src.listFiles();
        if (children != null) {
          for (File child : children) {
            copyRecursive(child, new File(dest, child.getName()));
          }
        }
      } else {
        FileInputStream in = new java.io.FileInputStream(src);
        FileOutputStream out = new java.io.FileOutputStream(dest);
        byte[] buffer = new byte[8192];
        int len;
        while ((len = in.read(buffer)) != -1) out.write(buffer, 0, len);
        in.close();
        out.close();
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public interface OnPasteComplete {
    void onComplete(boolean success);
  }
}
