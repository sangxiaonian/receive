package haipo.com.receive.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageView;

import com.uuzuche.lib_zxing.activity.CaptureActivity;
import com.uuzuche.lib_zxing.activity.CodeUtils;

import haipo.com.receive.R;
import haipo.com.receive.basic.BaseAcivity;
import haipo.com.receive.presenter.AliPayPresenter;
import haipo.com.receive.presenter.inter.IAliPayPresenter;
import haipo.com.receive.ui.inter.IAliPayView;
import haipo.com.receive.utils.ConfigUtlis;
import haipo.com.receive.utils.JLog;
import haipo.com.receive.utils.ToastUtil;
import sang.com.customdialog.BasicDialogFragment;
import sang.com.customdialog.DialogFactory;
import sang.com.customdialog.inter.OnClickDialogListener;

public class AliScanActivity extends BaseAcivity implements IAliPayView ,OnClickDialogListener{

    private static final int REQUEST_CODE = 1;
    private ImageView imageView;
    private IAliPayPresenter payPresenter;
    private BasicDialogFragment dialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ali_scan);
        dialog= DialogFactory.getInstance( getFragmentManager()).creatDialog(DialogFactory.SHAPE);
        initView();

        payPresenter = new AliPayPresenter(this);
        payPresenter.getData(this,ConfigUtlis.getConfig("payInfo"));

    }

    private void initView() {

        imageView = (ImageView) findViewById(R.id.tv_ali);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE) {
            if (null != data) {
                Bundle bundle = data.getExtras();
                if (bundle == null) {
                    return;
                }
                String result = bundle.getString(CodeUtils.RESULT_STRING);
                payPresenter.setAuth_code(result);

            }else {
                finish();
            }
        }else {
            finish();
        }
    }

    @Override
    public void showProgress(String tag) {

        JLog.e("bbbbbbbbbbbbbbbbbbbb");
        dialog.showCanable(false);
    }

    @Override
    public void dismiss() {
        JLog.e("aaaaaaaaaaaaa");
//        dialog.dismiss();
    }

    @Override
    public void showFail(String failReason) {

    }


    @Override
    public void showSuccess(String o) {
        startActivity(new Intent(this,ReceiptSuccessActivity.class));
        finish();
    }

    @Override
    public void jumpToScan() {
        Intent intent = new Intent(this, CaptureActivity.class);
        startActivityForResult(intent, REQUEST_CODE);
    }

    @Override
    public void showBar(Bitmap bar_bitmap) {
        imageView.setImageBitmap(bar_bitmap);
    }

    @Override
    public void clickDismiss(String tag) {
        ToastUtil.showTextToast(this,"取消被点击了");
        payPresenter.cancle(tag);
    }

    @Override
    public void clickEnrty() {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        payPresenter.cancle("");
    }


}
