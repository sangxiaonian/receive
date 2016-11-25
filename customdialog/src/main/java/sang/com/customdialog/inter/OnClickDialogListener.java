package sang.com.customdialog.inter;

/**
 * Description：
 *
 * @Author：桑小年
 * @Data：2016/11/23 11:45
 */
public interface OnClickDialogListener  {

    /**
     * 点击取消或者disimiss
     * @param tag
     */
    void clickDismiss(String tag);

    /**
     * 点击确认按钮
     */
    void clickEnrty();
}
