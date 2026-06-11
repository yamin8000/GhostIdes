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
import java.io.IOException;
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
  private MutableLiveData<CopyProgress> copyProgress = new MutableLiveData<>();
  private PathManager pathManager;
  private FileState fileState = FileState.NONE;

  public static class CopyProgress {
    public final String fileName;
    public final String fromDir;
    public final String toDir;
    public final int remaining;
    public final long bytesCopied;
    public final long totalBytes;
    public final long speedBps;
    public final boolean isRunning;

    public CopyProgress(
        String fileName,
        String fromDir,
        String toDir,
        int remaining,
        long bytesCopied,
        long totalBytes,
        long speedBps,
        boolean isRunning) {
      this.fileName = fileName;
      this.fromDir = fromDir;
      this.toDir = toDir;
      this.remaining = remaining;
      this.bytesCopied = bytesCopied;
      this.totalBytes = totalBytes;
      this.speedBps = speedBps;
      this.isRunning = isRunning;
    }
  }

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

  public LiveData<CopyProgress> getCopyProgress() {
    return copyProgress;
  }

  public void savePath(boolean save) {
    pathManager.savePath(save);
    if (save) pathManager.setLastPath(currentPath.getValue());
    else pathManager.clearLastPath();
  }

  public void navigateTo(String path) {
    File file = new File(path);
    if (file.exists() && file.isDirectory()) {
      currentPath.setValue(path);
      loadFiles(path);
      if (pathManager.isSaveEnabled()) pathManager.setLastPath(path);
    }
  }

  public void navigateUp() {
    String parent = new File(currentPath.getValue()).getParent();
    if (parent != null) navigateTo(parent);
  }

  public void loadFiles(String dirPath) {
    isLoading.postValue(true);
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
                    FileManagerModel model =
                        new FileManagerModel(
                            file.getAbsolutePath(), name, fileState, file.lastModified());
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
    if (file.delete()) loadFiles(currentPath.getValue());
  }

  public void deleteFiles(List<FileManagerModel> items) {
    new Thread(
            () -> {
              for (FileManagerModel item : items) deleteRecursive(new File(item.getPath()));
              loadFiles(currentPath.getValue());
            })
        .start();
  }

  private void deleteRecursive(File file) {
    if (file.isDirectory()) {
      File[] children = file.listFiles();
      if (children != null) for (File child : children) deleteRecursive(child);
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

              long totalBytes = 0;
              int totalFiles = 0;
              for (FileManagerModel model : sources) {
                totalBytes += getDirSize(new File(model.getPath()));
                totalFiles += countFiles(new File(model.getPath()));
              }

              long[] bytesCopied = {0};
              int[] remaining = {totalFiles};
              long[] lastTime = {System.currentTimeMillis()};
              long[] lastBytes = {0};

              String fromDir = new File(sources.get(0).getPath()).getParent();
              String fromName = (fromDir != null) ? new File(fromDir).getName() : "Unknown";
              String toName = new File(destDir).getName();

              for (FileManagerModel model : sources) {
                File src = new File(model.getPath());
                File dest = new File(destFolder, src.getName());

                if (isCut) {
                  if (!src.renameTo(dest)) {
                    copyRecursiveWithProgress(
                        src,
                        dest,
                        bytesCopied,
                        remaining,
                        lastTime,
                        lastBytes,
                        totalBytes,
                        fromName,
                        toName);
                    deleteRecursive(src);
                  } else {
                    remaining[0]--;
                  }
                } else {
                  copyRecursiveWithProgress(
                      src,
                      dest,
                      bytesCopied,
                      remaining,
                      lastTime,
                      lastBytes,
                      totalBytes,
                      fromName,
                      toName);
                }
              }

              copyProgress.postValue(new CopyProgress("", "", "", 0, 0, 0, 0, false));
              loadFiles(currentPath.getValue());
              if (callback != null) {
                new Handler(Looper.getMainLooper()).post(() -> callback.onComplete(success));
              }
            })
        .start();
  }

  private void copyRecursiveWithProgress(
      File src,
      File dest,
      long[] bytesCopied,
      int[] remaining,
      long[] lastTime,
      long[] lastBytes,
      long totalBytes,
      String fromDir,
      String toDir) {
    try {
      if (src.isDirectory()) {
        if (!dest.exists()) dest.mkdirs();
        File[] children = src.listFiles();
        if (children != null) {
          for (File child : children) {
            copyRecursiveWithProgress(
                child,
                new File(dest, child.getName()),
                bytesCopied,
                remaining,
                lastTime,
                lastBytes,
                totalBytes,
                fromDir,
                toDir);
          }
        }
      } else {
        FileInputStream in = new FileInputStream(src);
        FileOutputStream out = new FileOutputStream(dest);
        byte[] buffer = new byte[8192];
        int len;
        while ((len = in.read(buffer)) != -1) {
          out.write(buffer, 0, len);
          bytesCopied[0] += len;

          long now = System.currentTimeMillis();
          long elapsed = now - lastTime[0];
          if (elapsed >= 300) {
            long speed = (bytesCopied[0] - lastBytes[0]) * 1000 / Math.max(elapsed, 1);
            lastTime[0] = now;
            lastBytes[0] = bytesCopied[0];
            copyProgress.postValue(
                new CopyProgress(
                    src.getName(),
                    fromDir,
                    toDir,
                    remaining[0],
                    bytesCopied[0],
                    totalBytes,
                    speed,
                    true));
          }
        }
        in.close();
        out.close();
        remaining[0]--;
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private long getDirSize(File f) {
    if (f.isFile()) return f.length();
    long size = 0;
    File[] children = f.listFiles();
    if (children != null) for (File c : children) size += getDirSize(c);
    return size;
  }

  private int countFiles(File f) {
    if (f.isFile()) return 1;
    int count = 0;
    File[] children = f.listFiles();
    if (children != null) for (File c : children) count += countFiles(c);
    return count;
  }

  public void createFolder(String folderName) {
    String currentDir = currentPath.getValue();
    if (currentDir == null) return;
    File newFolder = new File(currentDir, folderName);
    if (!newFolder.exists()) {
      newFolder.mkdirs();
      loadFiles(currentDir);
    }
  }

  public void createFile(String fileName) {
    String currentDir = currentPath.getValue();
    if (currentDir == null) return;
    File newFile = new File(currentDir, fileName);
    if (!newFile.exists()) {
      try {
        newFile.createNewFile();
        loadFiles(currentDir);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  public interface OnPasteComplete {
    void onComplete(boolean success);
  }
}
