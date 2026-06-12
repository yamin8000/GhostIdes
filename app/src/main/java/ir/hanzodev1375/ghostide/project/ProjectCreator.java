package ir.hanzodev1375.ghostide.project;

import android.content.Context;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ProjectCreator {

  public enum ProjectType {
    HTML,
    NODEJS,
    JAVA
  }

  public interface OnCreateResult {
    void onSuccess(String projectPath);

    void onError(String message);
  }

  private final Context context;

  public ProjectCreator(Context context) {
    this.context = context;
  }

  public void create(
      ProjectType type,
      String projectName,
      String packageName,
      String parentPath,
      OnCreateResult callback) {
    new Thread(
            () -> {
              try {
                String result = buildProject(type, projectName, packageName, parentPath);
                callback.onSuccess(result);
              } catch (Exception e) {
                callback.onError(e.getMessage());
              }
            })
        .start();
  }

  private String buildProject(
      ProjectType type, String projectName, String packageName, String parentPath)
      throws IOException {
    File projectDir = new File(parentPath, projectName);
    if (projectDir.exists()) {
      throw new IOException("A folder named \"" + projectName + "\" already exists here.");
    }
    projectDir.mkdirs();

    switch (type) {
      case HTML:
        buildHtml(projectDir, projectName);
        break;
      case NODEJS:
        buildNodejs(projectDir, projectName);
        break;
      case JAVA:
        buildJava(projectDir, projectName, packageName);
        break;
    }

    return projectDir.getAbsolutePath();
  }

  private void buildHtml(File root, String name) throws IOException {
    File cssDir = new File(root, "css");
    File jsDir = new File(root, "js");
    cssDir.mkdirs();
    jsDir.mkdirs();

    writeTemplate("templates/html/index.html", new File(root, "index.html"), name, null);
    writeTemplate("templates/html/style.css", new File(cssDir, "style.css"), name, null);
    writeTemplate("templates/html/script.js", new File(jsDir, "script.js"), name, null);
  }

  private void buildNodejs(File root, String name) throws IOException {
    writeTemplate("templates/nodejs/package.json", new File(root, "package.json"), name, null);
    writeTemplate("templates/nodejs/index.js", new File(root, "index.js"), name, null);
    writeTemplate("templates/nodejs/gitignore", new File(root, ".gitignore"), name, null);
    writeRaw("# " + name + "\n", new File(root, "README.md"));
  }

  private void buildJava(File root, String name, String packageName) throws IOException {
    String pkgPath = packageName.replace('.', '/');
    File srcDir = new File(root, "src/main/java/" + pkgPath);
    srcDir.mkdirs();

    writeTemplate("templates/java/Main.java", new File(srcDir, "Main.java"), name, packageName);
    writeTemplate("templates/java/gitignore", new File(root, ".gitignore"), name, packageName);
    writeRaw("# " + name + "\n", new File(root, "README.md"));
  }

  private void writeTemplate(String assetPath, File dest, String projectName, String packageName)
      throws IOException {
    InputStream is = context.getAssets().open(assetPath);
    byte[] bytes = is.readAllBytes();
    is.close();

    String content = new String(bytes);
    content = content.replace("{{PROJECT_NAME}}", projectName);
    content =
        content.replace(
            "{{PROJECT_NAME_LOWER}}", projectName.toLowerCase().replaceAll("[^a-z0-9\\-]", "-"));
    if (packageName != null) {
      content = content.replace("{{PACKAGE_NAME}}", packageName);
    }

    writeRaw(content, dest);
  }

  private void writeRaw(String content, File dest) throws IOException {
    dest.getParentFile().mkdirs();
    try (FileOutputStream fos = new FileOutputStream(dest)) {
      fos.write(content.getBytes());
    }
  }
}
