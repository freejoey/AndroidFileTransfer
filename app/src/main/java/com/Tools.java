package com;

import java.io.File;
import java.io.FileInputStream;

import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.provider.MediaStore.Images.ImageColumns;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.myfiletransfer.R;

public class Tools {
	private static final String Tag = "Tools";

	public static boolean isSDExist() {
		String status = Environment.getExternalStorageState();
		if (status.equals(Environment.MEDIA_MOUNTED)) {
			return true;
		} else {
			return false;
		}
	}

	public static long getFileSize(File file) throws Exception {
		long size = 0;
		if (file.exists()) {
			FileInputStream fis = null;
			fis = new FileInputStream(file);
			size = fis.available();
		} else {
			file.createNewFile();
			Log.e("获取文件大小", "文件不存在!");
		}
		return size;
	}

	public static String getFileName(String pathandname) {

		int start = pathandname.lastIndexOf("/");
		int end = pathandname.lastIndexOf(".");
		if (start != -1 && end != -1) {
			// return pathandname.substring(start + 1, end);
			Log.i(Tag,
					"文件名字:"
							+ pathandname.substring(start + 1,
									pathandname.length()));
			return pathandname.substring(start + 1, pathandname.length());
		} else {
			return null;
		}
	}

	public static String getPureFileName(String pathandname) {

		int start = pathandname.lastIndexOf("/");
		int end = pathandname.lastIndexOf(".");
		if (start != -1 && end != -1) {
			// return pathandname.substring(start + 1, end);
			Log.i(Tag,
					"文件名字:"
							+ pathandname.substring(start + 1,
									pathandname.length()));
			return pathandname.substring(start + 1, end);
		} else {
			return null;
		}
	}

	public static String getFileNameTail(String pathandname) {

		int start = pathandname.lastIndexOf("/");
		int end = pathandname.lastIndexOf(".");
		if (start != -1 && end != -1) {
			// return pathandname.substring(start + 1, end);
			Log.i(Tag,
					"文件名尾部:" + pathandname.substring(end, pathandname.length()));
			return pathandname.substring(end + 1, pathandname.length());
		} else {
			return null;
		}
	}

	public static String getRealFilePath(final Context context, final Uri uri) {
		if (null == uri)
			return null;
		final String scheme = uri.getScheme();
		String data = null;
		if (scheme == null)
			data = uri.getPath();
		else if (ContentResolver.SCHEME_FILE.equals(scheme)) {
			data = uri.getPath();
		} else if (ContentResolver.SCHEME_CONTENT.equals(scheme)) {
			Cursor cursor = context.getContentResolver().query(uri,
					new String[] { ImageColumns.DATA }, null, null, null);
			if (null != cursor) {
				if (cursor.moveToFirst()) {
					int index = cursor.getColumnIndex(ImageColumns.DATA);
					if (index > -1) {
						data = cursor.getString(index);
					}
				}
				cursor.close();
			}
		}
		return data;
	}

	public static String checkFileExist(String filePath, int n) {
		Log.i(Tag, "filePath:" + filePath);

		String absName = Tools.getPureFileName(filePath);
		String nameTail = Tools.getFileNameTail(filePath);

		Log.i(Tag, "absName:" + absName);
		Log.i(Tag, "nameTail:" + nameTail);

		File newFile = new File(filePath);
		if (n != 0) {
			newFile = new File(Constants.getAppPath() + "/" + absName + n
					+ "." + nameTail);
		}
		if (!newFile.exists() && n == 0)
			return filePath;
		else if (!newFile.exists()) {
			Log.i(Tag, "newFile.getPath():" + newFile.getPath());
			return newFile.getPath();
		}

		return checkFileExist(filePath, n + 1);
	}

	public static Dialog createLoadingDialog(Context context, String msg) {

		LayoutInflater inflater = LayoutInflater.from(context);
		View v = inflater.inflate(R.layout.dialog_waiting_pb, null);// 得到加载view
		LinearLayout layout = (LinearLayout) v.findViewById(R.id.dialog_waiting_llt);// 加载布局
		// main.xml中的ImageView
		TextView tipTextView = (TextView) v.findViewById(R.id.tv_waiting);// 提示文字
		// 加载动画
		// 使用ImageView显示动画
		tipTextView.setText(msg);// 设置加载信息

		Dialog loadingDialog = new Dialog(context, R.style.loading_dialog);// 创建自定义样式dialog

		loadingDialog.setCancelable(false);// 不可以用“返回键”取消
		loadingDialog.setContentView(layout, new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.FILL_PARENT));// 设置布局
		return loadingDialog;
	}

	public static String getMyAddr(Context c) {
		WifiManager wifiManager = (WifiManager) c.getSystemService(Context.WIFI_SERVICE);
		if (!wifiManager.isWifiEnabled()) {
			return null;
		}
		WifiInfo wifiInfo = wifiManager.getConnectionInfo();
		int ipAddress = wifiInfo.getIpAddress();
		String ip = intToIp(ipAddress);
		return ip;
	}

	public static String intToIp(int i) {
		return (i & 0xFF) + "." + ((i >> 8) & 0xFF) + "." + ((i >> 16) & 0xFF)
				+ "." + (i >> 24 & 0xFF);
	}
}
