package com.caiyun.weather;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
import android.widget.Toast;
import androidx.core.content.FileProvider;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * 无服务器自动更新：以 GitHub 仓库为更新源
 * 1. 启动时拉取 update.json（api.github.com / raw / jsdelivr 三源容灾）
 * 2. 版本号大于当前版本时弹窗提示
 * 3. 下载 APK（带进度条）后调起系统安装器
 */
public class UpdateChecker {

    /** 与 build.gradle versionCode 保持一致 */
    private static final int CURRENT_VERSION_CODE = 12;
    private static final String CURRENT_VERSION_NAME = "2.8.0";

    private static final String REPO = "gyz-111/gyz-1";
    private static final String GITEE_REPO = "gyz-1/gyz-1";
    private static final String AUTHORITY = "com.caiyun.weather.fileprovider";
    private static final String RAW_ACCEPT = "application/vnd.github.raw";

    /** 入口：silent=true 静默检查（失败不打扰用户） */
    public static void check(final Activity act, final boolean silent) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String lastErr = null;
                JSONObject j = null;
                for (String u : jsonUrls()) {
                    try {
                        j = fetchJson(u);
                        if (j != null) break;
                    } catch (Exception e) {
                        lastErr = e.getMessage();
                    }
                }
                if (j == null) {
                    if (!silent) toast(act, "检查更新失败: " + lastErr);
                    return;
                }
                final int code = j.optInt("versionCode", 0);
                final String vname = j.optString("versionName", "");
                final String desc = j.optString("desc", "");
                final String apkPath = j.optString("apk", "");
                final boolean force = j.optBoolean("force", false);

                if (code <= CURRENT_VERSION_CODE || apkPath.isEmpty()) {
                    if (!silent) toast(act, "已是最新版本 v" + CURRENT_VERSION_NAME);
                    return;
                }
                act.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        AlertDialog.Builder b = new AlertDialog.Builder(act)
                                .setTitle("发现新版本 v" + vname)
                                .setMessage(desc)
                                .setPositiveButton("立即更新",
                                        (d, w) -> downloadAndInstall(act, apkPath, vname));
                        if (force) {
                            b.setCancelable(false)
                                    .setNegativeButton("退出应用",
                                            (d, w) -> act.finish());
                        } else {
                            b.setNegativeButton("暂不更新", null);
                        }
                        b.show();
                    }
                });
            }
        }).start();
    }

    // ---------- 版本清单多源（Gitee 国内优先） ----------

    private static String[] jsonUrls() {
        return new String[]{
                "https://gitee.com/" + GITEE_REPO + "/raw/main/update.json",
                "https://api.github.com/repos/" + REPO + "/contents/update.json",
                "https://raw.githubusercontent.com/" + REPO + "/main/update.json",
                "https://cdn.jsdelivr.net/gh/" + REPO + "@main/update.json"
        };
    }

    // ---------- APK 下载多源 ----------
    // jsdelivr 优先（有 Content-Length，进度条显示百分比；国内极快）
    // 其次 raw.githubusercontent.com / Gitee

    private static String[] apkUrls(String apkPath) {
        String enc = segmentEncode(apkPath);
        return new String[]{
                "https://cdn.jsdelivr.net/gh/" + REPO + "@main/" + enc,
                "https://raw.githubusercontent.com/" + REPO + "/main/" + enc,
                "https://gitee.com/" + GITEE_REPO + "/raw/main/" + enc,
                "https://github.com/" + REPO + "/raw/main/" + enc
        };
    }

    private static String segmentEncode(String path) {
        String[] parts = path.split("/");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            if (i > 0) sb.append('/');
            try {
                sb.append(java.net.URLEncoder.encode(parts[i], "UTF-8"));
            } catch (Exception e) {
                sb.append(parts[i]);
            }
        }
        return sb.toString();
    }

    private static JSONObject fetchJson(String url) throws Exception {
        HttpURLConnection c = (HttpURLConnection) new URL(url).openConnection();
        c.setConnectTimeout(8000);
        c.setReadTimeout(8000);
        c.setRequestMethod("GET");
        if (url.contains("api.github.com")) {
            c.setRequestProperty("Accept", RAW_ACCEPT);
        }
        int code = c.getResponseCode();
        InputStream is = code >= 400 ? c.getErrorStream() : c.getInputStream();
        String body = readAll(is);
        c.disconnect();
        if (code != 200) throw new Exception("HTTP " + code);
        return new JSONObject(body);
    }

    // ---------- 下载与安装 ----------

    private static void downloadAndInstall(final Activity act, final String apkPath,
                                           final String ver) {
        final ProgressDialog pd = new ProgressDialog(act);
        pd.setTitle("下载更新 v" + ver);
        pd.setMessage("准备中…");
        pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        pd.setMax(100);
        pd.setCancelable(false);
        pd.setIndeterminate(true); // 先显示不定进度，拿到总长度后再切
        pd.show();

        new Thread(new Runnable() {
            @Override
            public void run() {
                Exception lastErr = null;
                boolean ok = false;
                for (String u : apkUrls(apkPath)) {
                    try {
                        download(act, pd, u);
                        ok = true;
                        break;
                    } catch (Exception e) {
                        lastErr = e;
                    }
                }
                final boolean okF = ok;
                final Exception errF = lastErr;
                act.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        pd.dismiss();
                        if (okF) {
                            install(act);
                        } else {
                            toast(act, "下载失败: "
                                    + (errF != null ? errF.getMessage() : "未知错误"));
                        }
                    }
                });
            }
        }).start();
    }

    private static void download(Activity act, ProgressDialog pd, String url) throws Exception {
        HttpURLConnection c = (HttpURLConnection) new URL(url).openConnection();
        c.setConnectTimeout(10000);
        c.setReadTimeout(30000);
        if (url.contains("api.github.com")) {
            c.setRequestProperty("Accept", RAW_ACCEPT);
        }
        int code = c.getResponseCode();
        if (code != 200) throw new Exception("HTTP " + code);
        long total = c.getContentLength();

        File dir = act.getExternalFilesDir("apks");
        if (dir == null) dir = act.getFilesDir();
        if (!dir.exists()) dir.mkdirs();
        File out = new File(dir, "update.apk");

        InputStream is = c.getInputStream();
        FileOutputStream fos = new FileOutputStream(out);
        byte[] buf = new byte[8192];
        long done = 0;
        int n;
        final long totalFinal = total > 0 ? total : -1;

        // 如果拿到总长度，切换为确定进度
        if (totalFinal > 0) {
            act.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    pd.setIndeterminate(false);
                    pd.setMessage("下载中…");
                }
            });
        }

        while ((n = is.read(buf)) > 0) {
            fos.write(buf, 0, n);
            done += n;
            if (totalFinal > 0) {
                final int p = (int) (done * 100 / totalFinal);
                act.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        pd.setProgress(p);
                    }
                });
            }
        }
        fos.close();
        is.close();
        c.disconnect();

        // 校验是合法APK（ZIP魔数PK）且大小合理，防止把错误页存成APK
        if (out.length() < 100 * 1024) {
            out.delete();
            throw new Exception("文件过小，非有效APK");
        }
        try (FileInputStream fis = new FileInputStream(out)) {
            byte[] magic = new byte[2];
            if (fis.read(magic) != 2 || magic[0] != 'P' || magic[1] != 'K') {
                out.delete();
                throw new Exception("下载内容非APK");
            }
        }
    }

    private static void install(final Activity act) {
        File dir = act.getExternalFilesDir("apks");
        if (dir == null) dir = act.getFilesDir();
        File apk = new File(dir, "update.apk");
        Uri uri = FileProvider.getUriForFile(act, AUTHORITY, apk);
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setDataAndType(uri, "application/vnd.android.package-archive");
        i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION
                | Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            act.startActivity(i);
        } catch (Exception e) {
            toast(act, "请允许安装未知来源应用");
            try {
                act.startActivity(new Intent(
                        Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                        Uri.parse("package:" + act.getPackageName())));
            } catch (Exception ignored) {
            }
        }
    }

    // ---------- 工具 ----------

    private static String readAll(InputStream is) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] buf = new byte[4096];
        int n;
        while ((n = is.read(buf)) > 0) {
            bos.write(buf, 0, n);
        }
        is.close();
        return new String(bos.toByteArray(), StandardCharsets.UTF_8);
    }

    private static void toast(final Activity act, final String msg) {
        act.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(act, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
