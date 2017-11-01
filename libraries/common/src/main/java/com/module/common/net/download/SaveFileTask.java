package com.module.common.net.download;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import com.module.common.net.FileUtil;
import com.module.common.net.callback.IRequestCallback;
import com.module.common.utils.Utils;

import java.io.File;
import java.io.InputStream;
import okhttp3.ResponseBody;

/**
 * 下载保存文件
 */

final class SaveFileTask extends AsyncTask<Object, Void, File> {

    private final IRequestCallback iRequestCallback;

    SaveFileTask(IRequestCallback iRequestCallback) {
        this.iRequestCallback = iRequestCallback;
    }

    @Override
    protected File doInBackground(Object... params) {
        String downloadDir = (String) params[0];
        String extension = (String) params[1];
        final ResponseBody body = (ResponseBody) params[2];
        final String name = (String) params[3];
        final InputStream is = body.byteStream();
        if (downloadDir == null || downloadDir.equals("")) {
            downloadDir = "down_loads";
        }
        if (extension == null || extension.equals("")) {
            extension = "";
        }
        if (name == null) {
            return FileUtil.writeToDisk(is, downloadDir, extension.toUpperCase(), extension);
        } else {
            return FileUtil.writeToDisk(is, downloadDir, name);
        }
    }

    @Override
    protected void onPostExecute(File file) {
        super.onPostExecute(file);
        if (iRequestCallback != null) {
            iRequestCallback.onSuccess(file.getPath());
        }
        if (iRequestCallback != null) {
            iRequestCallback.onComplete();
        }
        autoInstallApk(file);
    }

    private void autoInstallApk(File file) {
        if (FileUtil.getExtension(file.getPath()).equals("apk")) {
            final Intent install = new Intent();
            install.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            install.setAction(Intent.ACTION_VIEW);
            install.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
            Utils.getContext().startActivity(install);
        }
    }
}
