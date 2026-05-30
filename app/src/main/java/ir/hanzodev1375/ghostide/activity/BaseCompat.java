package ir.hanzodev1375.ghostide.activity;

import android.os.Bundle;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class BaseCompat extends AppCompatActivity {
  @Override
  protected void onCreate(Bundle arg0) {
    EdgeToEdge.enable(this);
    super.onCreate(arg0);
    // TODO: Implement this method
  }
}
