package hu.ait.finalproject;

import android.app.ProgressDialog;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by Moe on 5/20/17.
 */

public class BaseActivity extends AppCompatActivity {

    private ProgressDialog progressDialog;

    public void showProgressDialog() {
        if (progressDialog  == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage(getString(R.string.loggingin));
        }

        progressDialog.show();
    }

    public void hideProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.hide();
        }
    }

}