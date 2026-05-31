package ir.hanzodev1375.ghostide;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Process;
import android.util.Log;

import ir.hanzodev1375.ghostide.activity.ErrorManagerActivity;
import java.util.Calendar;

public class GhostIdeAppLoader extends Application {

  private static Context mApplicationContext;
  private static GhostIdeAppLoader loader;

  private Thread.UncaughtExceptionHandler defaultExceptionHandler;
  private final StringBuilder softwareInfo = new StringBuilder();

  public static Context getContext() {
    return mApplicationContext;
  }

  public static GhostIdeAppLoader getInstance() {
    return loader;
  }

  @Override
  public void onCreate() {
    super.onCreate();
    loader = this;
    mApplicationContext = getApplicationContext();

    
    softwareInfo
        .append("SDK: ")
        .append(Build.VERSION.SDK_INT)
        .append("\n")
        .append("Android: ")
        .append(Build.VERSION.RELEASE)
        .append("\n")
        .append("Model: ")
        .append(Build.MODEL)
        .append("\n")
        .append("Incremental: ")
        .append(Build.VERSION.INCREMENTAL)
        .append("\n");

 
    defaultExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
    Thread.setDefaultUncaughtExceptionHandler(
        (thread, throwable) -> {
          String stackTrace = Log.getStackTraceString(throwable);
          String dateTime = Calendar.getInstance().getTime().toString();
          Intent intent = new Intent(mApplicationContext, ErrorManagerActivity.class);
          intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
          intent.putExtra("Error", stackTrace);
          intent.putExtra("Date", dateTime);
          intent.putExtra("Software", softwareInfo.toString());

          
          new Handler(Looper.getMainLooper())
              .postDelayed(
                  () -> {
                    try {
                      mApplicationContext.startActivity(intent);
                    } catch (Exception e) {
                      e.printStackTrace();
                    }
                  },
                  500);

          
          new Handler(Looper.getMainLooper())
              .postDelayed(
                  () -> {
                    if (defaultExceptionHandler != null) {
                      defaultExceptionHandler.uncaughtException(thread, throwable);
                    } else {
                      Process.killProcess(Process.myPid());
                      System.exit(1);
                    }
                  },
                  1500);
        });
  }

  public boolean isSdkS() {
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.S;
  }

  public boolean isSdkQ() {
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q;
  }

  public String getVersion() {
    try {
      PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), 0);
      return info.versionName;
    } catch (PackageManager.NameNotFoundException e) {
      return null;
    }
  }
}
