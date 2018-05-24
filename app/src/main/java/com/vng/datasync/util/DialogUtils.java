package com.vng.datasync.util;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;

import com.vng.datasync.R;

/**
 * Copyright (C) 2017, VNG Corporation.
 *
 * @author thuannv
 * @since 09/08/2017
 */

public final class DialogUtils {

    private DialogUtils() {
    }

    public static Dialog showProgressDialog(Context context, CharSequence text) {
        ProgressDialog dialog = new ProgressDialog(context);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        dialog.setMessage(text);
        dialog.show();
        return dialog;
    }

    public static Dialog showConfirmDeleteConversationDialog(@NonNull final Context context,
                                                             final DialogInterface.OnClickListener onPositiveClick,
                                                             final DialogInterface.OnClickListener onNegativeClick) {
        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(context);

        builder.setMessage(context.getString(R.string.conversation_delete_message))
                .setCancelable(true)
                .setPositiveButton(context.getString(R.string.dialog_positive_button), onPositiveClick)
                .setNegativeButton(context.getString(R.string.dialog_negative_button), onNegativeClick)
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        if (onNegativeClick != null) {
                            onNegativeClick.onClick(dialog, DialogInterface.BUTTON_NEGATIVE);
                        }
                    }
                });

        android.support.v7.app.AlertDialog dialog = builder.create();
        dialog.show();

        return dialog;
    }

    public static Dialog showDialogAlert(@NonNull final Context context, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(message)
                .setTitle(R.string.dialog_title)
                .setCancelable(true)
                .setPositiveButton(context.getString(R.string.dialog_positive_button), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        AlertDialog dialog = builder.create();
        dialog.show();

        return dialog;
    }


    public static Dialog showDialogConfirm(@NonNull final Context context,
                                           String message,
                                           final DialogInterface.OnClickListener onPositiveClick,
                                           final DialogInterface.OnClickListener onNegativeClick) {
        String title = context.getString(R.string.dialog_title);
        String positiveButtonText = context.getString(R.string.dialog_positive_button);
        String negativeButtonText = context.getString(R.string.dialog_negative_button);

        return showDialogConfirm(context, title, message, positiveButtonText, negativeButtonText, onPositiveClick, onNegativeClick);
    }

    public static Dialog showDialogConfirm(@NonNull final Context context,
                                           final String title,
                                           final String message,
                                           final String positiveButtonText,
                                           final String negativeButtonText,
                                           final DialogInterface.OnClickListener onPositiveClick,
                                           final DialogInterface.OnClickListener onNegativeClick) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        if (!TextUtils.isEmpty(title)) {
            builder.setTitle(title);
        }

        if (!TextUtils.isEmpty(message)) {
            builder.setMessage(message);
        }

        builder.setCancelable(true)
                .setPositiveButton(positiveButtonText,
                        (dialog, which) -> {
                            if (onPositiveClick != null) {
                                onPositiveClick.onClick(dialog, DialogInterface.BUTTON_POSITIVE);
                            }
                        })
                .setNegativeButton(negativeButtonText,
                        (dialog, which) -> {
                            dialog.dismiss();
                            if (onNegativeClick != null) {
                                onNegativeClick.onClick(dialog, DialogInterface.BUTTON_NEGATIVE);
                            }
                        });

        AlertDialog dialog = builder.create();
        dialog.show();

        return dialog;
    }

    public static Dialog showDialogOnlyPositive(Context context, String title, String message, boolean touchOutSide, final DialogInterface.OnClickListener listener) {
        if (context == null)
            return null;
        return new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setCancelable(touchOutSide)
                .setPositiveButton(context.getText(R.string.dialog_positive_button), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (listener != null) {
                            listener.onClick(dialog, id);
                        }
                        dialog.dismiss();
                    }
                }).show();
    }

    public static void dismiss(Dialog dialog) {
        if (isDialogShowing(dialog)) {
            dialog.dismiss();
        }
    }

    public static boolean isDialogShowing(Dialog dialog) {
        return dialog != null && dialog.isShowing();
    }

    public static boolean isDialogFragmentShowing(Fragment dialogFragment) {
        return dialogFragment != null && dialogFragment.isAdded();
    }

    public static void dismiss(FragmentActivity activity, DialogFragment dialogFragment) {
        if (activity == null || dialogFragment == null) {
            return;
        }

        FragmentManager fragmentManager = activity.getSupportFragmentManager();

        if (fragmentManager == null) {
            return;
        }

        if (!fragmentManager.isStateSaved() && isDialogFragmentShowing(dialogFragment)) {
            dialogFragment.dismiss();
        }
    }

    public static void showDialogFragment(FragmentActivity activity, DialogFragment dialogFragment, String tag) {
        if (activity == null || dialogFragment == null) {
            return;
        }

        FragmentManager fragmentManager = activity.getSupportFragmentManager();

        if (fragmentManager != null && !fragmentManager.isStateSaved()) {
            dialogFragment.show(fragmentManager, tag);
        }
    }
}
