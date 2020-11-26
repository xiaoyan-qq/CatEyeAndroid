package com.vtm.library.tools;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class CatEyeTileDownloadReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() == "catEye_tile_download") {
            TileDownloader.getInstance().showDialog();
            Intent startMainActivityIntent = new Intent("catEye_tile_download");
            startMainActivityIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            context.startActivity(startMainActivityIntent);
        }
    }
}
