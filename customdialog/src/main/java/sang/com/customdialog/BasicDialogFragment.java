package sang.com.customdialog;

import android.app.DialogFragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.view.View;

import sang.com.customdialog.exception.CustomException;
import sang.com.customdialog.inter.OnClickDialogListener;

/**
 * Description：
 *
 * @Author：桑小年
 * @Data：2016/11/23 14:06
 */
public class BasicDialogFragment extends DialogFragment {
    public FragmentManager manager;
    public OnClickDialogListener listener;
    public String tag = "dialog";
    public int layoutID = R.layout.shape_dialog;
    public View view;
    public String entry = "确认";
    public String cancle = "取消";
    AlertDialog dialog;


    public void setDialogData(FragmentManager manager, OnClickDialogListener listener) {
        this.manager = manager;
        this.listener = listener;
    }



    public void setDialogData(FragmentManager manger) {
        this.manager = manger;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (getActivity() != null && getActivity() instanceof OnClickDialogListener) {
            listener = (OnClickDialogListener) getActivity();
        } else if (getParentFragment() != null && getParentFragment() instanceof OnClickDialogListener) {
            listener = (OnClickDialogListener) getParentFragment();
        }

    }



    @Override
    public void onStart() {
        super.onStart();


    }

    public void show() {
        FragmentTransaction fragmentTransaction = manager.beginTransaction();
        if (manager.findFragmentByTag(tag)!=null){
            fragmentTransaction.remove(manager.findFragmentByTag(tag));
        }
        show(manager, tag);
    }





    public void showCanable(boolean isCancelable) {
        setCancelable(isCancelable);
        show();
    }

    public void setLayoutID(int layoutID) {
        if (view != null) {
            throw new CustomException("setView(View view) 已经被调用 ");
        }
        this.layoutID = layoutID;

    }

    public void setDialogView(View view) {
        this.view = view;
        if (layoutID != R.layout.shape_dialog) {
            throw new CustomException("setLayoutID(int layoutID) 已经被调用 ");
        }

    }

    public void setEntry(String entry){
        this.entry = entry;
    }

    public void setCancle(String cancle){
        this.cancle = cancle;
    }

    public String getEntry(){
        return entry;
    }

    public String getCancle(){
        return cancle;
    }
}

