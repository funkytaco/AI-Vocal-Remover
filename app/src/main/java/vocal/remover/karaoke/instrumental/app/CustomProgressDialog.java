package vocal.remover.karaoke.instrumental.app;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.TextView;

public class CustomProgressDialog {

    private static CustomProgressDialog customProgressDialog = null;
    private Dialog mDialog;

    public static CustomProgressDialog getInstance() {
        if (customProgressDialog == null) {
            customProgressDialog = new CustomProgressDialog();
        }
        return customProgressDialog;
    }

    public void ShowProgress(Context context, String message, boolean cancelable) {

        ProgressBar mProgressBar;
        TextView textView;
        mDialog = new Dialog(context);

        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        mDialog.setContentView(R.layout.dialog_progressbar);
        mProgressBar = mDialog.findViewById(R.id.progress_bar);
        textView = mDialog.findViewById(R.id.progress_text);
        textView.setText("" + message);
        textView.setVisibility(View.VISIBLE);
        mProgressBar.setVisibility(View.VISIBLE);
        mProgressBar.setIndeterminate(true);
        mDialog.setCancelable(cancelable);
        mDialog.setCanceledOnTouchOutside(cancelable);
        mDialog.show();
    }

    public void hideProgress() {
        if (mDialog != null) {
            mDialog.dismiss();
            mDialog = null;
        }
    }
}