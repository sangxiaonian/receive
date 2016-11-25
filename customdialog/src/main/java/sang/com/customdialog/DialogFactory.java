//package sang.com.customdialog;
//
//import android.app.FragmentManager;
//
//import sang.com.customdialog.exception.CustomException;
//import sang.com.customdialog.inter.OnClickDialogListener;
//
///**
// * Description：
// *
// * @Author：桑小年
// * @Data：2016/11/23 14:02
// */
//public class DialogFactory  {
//
//    private final FragmentManager manager;
//    private OnClickDialogListener listener;
//    private BasicDialogFragment dialog;
//    private static DialogFactory factory;
//    public static final int SHAPE_PRO = 0;
//
//    public static DialogFactory getInstance(OnClickDialogListener listener, FragmentManager manager){
//        return new DialogFactory(listener,manager);
//    }
//
//    public static DialogFactory getInstance( FragmentManager manager){
//        return new DialogFactory(null,manager);
//    }
//
//
//    public DialogFactory(OnClickDialogListener listener, FragmentManager manager){
//        this.listener =listener;
//        this.manager = manager;
//    }
//
//    public BasicDialogFragment creatDialog(int type){
//
//        switch (type){
//            case SHAPE_PRO:
//                dialog = new ShapeDialog();
//                break;
//            default:
//                throw new CustomException("类型错误");
//
//        }
//        dialog.setDialogData(manager,listener);
//
//        return dialog;
//    }
//
//
//
//
//}
