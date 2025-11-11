package com.example.newbase_2025.base;

import android.app.Dialog;

public class ProgressDialogAvl {
    Dialog dialog;

    public void isLoading(boolean isLoading) {
        if (isLoading) {
            if (!dialog.isShowing()) {
                try {
                    System.gc();
                    dialog.show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else {
            try {
                System.gc();
                if (dialog.isShowing()) dialog.dismiss();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
