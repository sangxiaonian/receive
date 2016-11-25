package sang.com.customdialog;

import android.app.FragmentManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import sang.com.customdialog.inter.OnClickDialogListener;
import sang.com.customdialog.view.ShapeLoading;

/**
 * Description：
 *
 * @Author：桑小年
 * @Data：2016/11/22 15:20
 */
public class ShapeDialog extends BasicDialogFragment {


    private static ShapeDialog dialog;

    private String tag = "ShapeDialog";

    public ShapeDialog(){
    }



    static ShapeDialog newInstance(String data,FragmentManager manager, OnClickDialogListener listener ) {
        ShapeDialog f = new ShapeDialog();
        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putString("data", data);
        f.setArguments(args);
        f.setDialogData(manager,listener);
        return f;
    }

    static ShapeDialog newInstance(FragmentManager manager, OnClickDialogListener listener ) {

        return newInstance("ShapeDialog",manager,listener);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

            getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
             view = inflater.inflate(layoutID, container);

            if (layoutID==R.layout.shape_dialog){
                view.findViewById(R.id.img).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dismiss();
                        if (listener!=null){
                            listener.clickDismiss(getTag());
                        }
                    }
                });
            }
            return view;
    }


    public void show(String msg){
        ((ShapeLoading)view.findViewById(R.id.shape)).setText(msg);
        show();
    }

}
