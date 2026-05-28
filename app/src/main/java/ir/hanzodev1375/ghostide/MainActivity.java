package ir.hanzodev1375.ghostide;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import ir.hanzodev1375.ghostide.activity.FileManagerActivity;
import ir.hanzodev1375.ghostide.utils.PermissionUtils;

public class MainActivity extends AppCompatActivity {

  private ActivityResultLauncher<Intent> manageStorageLauncher;

  @Override
  public void onCreate(Bundle arg0) {
    super.onCreate(arg0);
    setContentView(R.layout.activity_main);

    manageStorageLauncher =
        registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
              if (PermissionUtils.hasManageStoragePermission(this)) {
                startFileManager();
              } else {
                Toast.makeText(this, "برای مدیریت فایل به مجوز نیاز است", Toast.LENGTH_LONG).show();
                finish();
              }
            });

    checkPermissionsAndStart();
  }

  private void checkPermissionsAndStart() {
    if (!PermissionUtils.hasPermissions(this)) {
      PermissionUtils.requestPermissions(this);
      PermissionUtils.requestManageStoragePermission(this, manageStorageLauncher);
    } else {
      startFileManager();
    }
  }

  private void startFileManager() {
    startActivity(new Intent(this, FileManagerActivity.class));
    finish();
  }

  @Override
  protected void onResume() {
    super.onResume();
    if (PermissionUtils.hasPermissions(this) && PermissionUtils.hasManageStoragePermission(this)) {
      startFileManager();
    }
  }
}
