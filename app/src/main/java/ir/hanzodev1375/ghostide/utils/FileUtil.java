package ir.hanzodev1375.ghostide.utils;
import java.io.File;

public class FileUtil {
  public static boolean isExists(String path){
    return new File(path).exists();
  }
}
