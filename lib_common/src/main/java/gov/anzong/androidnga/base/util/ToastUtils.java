package gov.anzong.androidnga.base.util;

import android.widget.Toast;

import androidx.annotation.StringRes;

/**
 * Created by Justwen on 2018/8/11.
 */
// TODO 2023/1/17 Migrate to Snackbars
public class ToastUtils {
    public static void showToast(String text) {
        ThreadUtils.runOnMainThread(() -> Toast.makeText(ContextUtils.getApplication(), text, Toast.LENGTH_SHORT).show());
    }

    public static void showToast(@StringRes int strId) {
        ToastUtils.showToast(ContextUtils.getString(strId));
    }
}
