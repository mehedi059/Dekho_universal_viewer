package com.mehedi.dekho;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.util.Base64;
import com.getcapacitor.BridgeActivity;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class MainActivity extends BridgeActivity {
  @Override public void onResume() { super.onResume(); handle(getIntent()); }
  @Override protected void onNewIntent(Intent i) { super.onNewIntent(i); setIntent(i); handle(i); }

  private void handle(Intent i) {
    if (i == null || !Intent.ACTION_VIEW.equals(i.getAction()) || i.getData() == null) return;
    final Uri u = i.getData();
    i.setAction(Intent.ACTION_MAIN);
    new Thread(() -> {
      try {
        String n = "file";
        try (Cursor c = getContentResolver().query(u, null, null, null, null)) {
          if (c != null && c.moveToFirst()) {
            int x = c.getColumnIndex(OpenableColumns.DISPLAY_NAME);
            if (x >= 0) n = c.getString(x);
          }
        }
        InputStream in = getContentResolver().openInputStream(u);
        ByteArrayOutputStream o = new ByteArrayOutputStream();
        byte[] b = new byte[65536]; int r;
        while ((r = in.read(b)) > 0) o.write(b, 0, r);
        in.close();
        final String call = "window.dekhoOpen(" + org.json.JSONObject.quote(n) + ",\"" + Base64.encodeToString(o.toByteArray(), Base64.NO_WRAP) + "\")";
        final String js = "(function t(){if(window.dekhoOpen){" + call + "}else setTimeout(t,200)})()";
        runOnUiThread(() -> getBridge().getWebView().evaluateJavascript(js, null));
      } catch (Exception e) { }
    }).start();
  }
}
