package haipo.com.receive.mode;

import android.app.Activity;
import android.os.Parcelable;
import android.text.TextUtils;

import com.google.gson.Gson;

import java.net.URLEncoder;
import java.util.concurrent.TimeUnit;

import haipo.com.receive.Ali.AlipayApiException;
import haipo.com.receive.Ali.AlipaySignature;
import haipo.com.receive.http.HttpParams;
import haipo.com.receive.http.HttpServer;
import haipo.com.receive.http.RetrofitUtils;
import haipo.com.receive.mode.bean.AliBean;
import haipo.com.receive.mode.bean.AliResultBean;
import haipo.com.receive.mode.bean.GlobalBean;
import haipo.com.receive.mode.inter.IAliPayMode;
import haipo.com.receive.presenter.AliPayPresenter;
import haipo.com.receive.utils.ConfigUtlis;
import haipo.com.receive.utils.JLog;
import haipo.com.receive.utils.Utils;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.schedulers.Schedulers;

import static haipo.com.receive.http.HttpParams.PARAMS_BAR;

/**
 * Description：
 *
 * @Author：桑小年
 * @Data：2016/11/18 15:02
 */
public class AliPayMode implements IAliPayMode {


    /**
     * 支付宝RSA私钥
     */
    public String priKey;

    /**
     * 支付宝RSA公钥
     */
    public String pub;

    /**
     * 支付宝公钥
     */
    public String ali_key;


    /**
     * 传递过来的数据
     */
    private Parcelable parcelable;

    private AliBean aliBean;

    private GlobalBean globalBean;

    private AliPayPresenter payPresenter;
    private Observable<String> wxPayBody, aliPay;

    private final int times = 6;

    private Subscriber<String> query;


    private Subscriber<String> cancle = new Subscriber<String>() {
        @Override
        public void onCompleted() {
            JLog.i("----------------------------onCompleted-------------------------------");

        }

        @Override
        public void onError(Throwable e) {
            e.printStackTrace();
        }

        @Override
        public void onNext(String s) {
            payPresenter.paySuccess(s);
        }

    };


    public AliPayMode(AliPayPresenter payPresenter) {
        priKey = ConfigUtlis.getConfig("private_key");
        pub = ConfigUtlis.getConfig("public_key");
        ali_key = ConfigUtlis.getConfig("alipay_public_key");
        aliBean = new AliBean();
        this.payPresenter = payPresenter;
    }

    @Override
    public void getData(Activity activity, String payInfo) {
        parcelable = activity.getIntent().getParcelableExtra(payInfo);
        if (!TextUtils.isEmpty(payInfo) && TextUtils.equals(ConfigUtlis.getConfig("payInfo"), payInfo)) {
            globalBean = (GlobalBean) parcelable;
            globalBean.BillNo = "order" + Utils.getStringDate("yyyyMMddHHmmss");
            payPresenter.onGetData(globalBean.function);
        }


    }

    @Override
    public void setAuth_code(String auth_code) {
        aliBean.auth_code = auth_code;
    }


    @Override
    public void aliPay(int function, final long delayTime) {
        HttpServer server = RetrofitUtils.getInstance().getStringClient(ConfigUtlis.getConfig("open_api_domain"));
        HttpParams params = getAliParams(function);
        final Observable<String> aliPay = server.getAliPay(params.getParams());
        aliPay.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(payPresenter);
    }


    /**
     * 获取参数
     *
     * @param function
     * @return
     */
    private HttpParams getAliParams(int function) {
        HttpParams params = null;
        String biz_content = null;
        globalBean.type = function;
        params = HttpParams.getInstance().getSpecialParams(function);
        AliBean bean = new AliBean();
        switch (function) {
            case HttpParams.PARAMS_SCAN://扫码支付
            case HttpParams.PARAMS_BAR://条码支付
                bean.out_trade_no = globalBean.BillNo;
                bean.total_amount = globalBean.payAmount;
                bean.subject = URLEncoder.encode(ConfigUtlis.getConfig("subject"));
                bean.timeout_express = ConfigUtlis.getConfig("timeout_express");
                if (function == HttpParams.PARAMS_SCAN) {
                    bean.scene = ConfigUtlis.getConfig("scan_method");
                } else if (function == HttpParams.PARAMS_BAR) {
                    bean.scene = ConfigUtlis.getConfig("ali_bar_code");
                }

                break;
            case HttpParams.PARAMS_QUERY:
            case HttpParams.PARAMS_CANCLE:
                bean.out_trade_no = globalBean.BillNo;
                break;
        }
        biz_content = new Gson().toJson(bean);
        params.put("biz_content", biz_content);
        String sign_params = params.toString();
        JLog.i(sign_params);
        try {
            String sign = AlipaySignature.rsaSign(sign_params, priKey, "gbk");
            JLog.i(sign);
            params.put("sign", sign);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        return params;
    }

    @Override
    public int getFunction() {
        return globalBean.function;
    }

    @Override
    public GlobalBean getGloble() {
        return globalBean;
    }

    @Override
    public void aliQuery() {
        aliQuery(false);
    }

    @Override
    public void aliQuery(boolean isCancle) {
        final HttpServer server = RetrofitUtils.getInstance().getStringClient(ConfigUtlis.getConfig("open_api_domain"));
        final HttpParams params = getAliParams(HttpParams.PARAMS_QUERY);
        final Observable<String> aliPay = server.getAliPay(params.getParams());
        int time = 1;
        if (!isCancle) {
            time = times;
       }
        final int finalTime = time;
        aliPay.timeout(10,TimeUnit.SECONDS).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .repeatWhen(new Func1<Observable<? extends Void>, Observable<?>>() {
                    @Override
                    public Observable<?> call(Observable<? extends Void> observable) {

                        return observable
                                .zipWith(Observable.range(1, finalTime), new Func2<Void, Integer, Integer>() {
                                    @Override
                                    public Integer call(Void aVoid, Integer integer) {
                                        return integer;
                                    }
                                }).flatMap(new Func1<Integer, Observable<Long>>() {
                                    @Override
                                    public Observable<Long> call(Integer integer) {
                                        return Observable.timer(integer * 6, TimeUnit.SECONDS);
                                    }
                                });
                    }
                })
                .filter(new Func1<String, Boolean>() {
                    @Override
                    public Boolean call(String s) {
                        JLog.i("-----------filter---------------");
                        AliResultBean bean = new Gson().fromJson(s, AliResultBean.class);
                        return TextUtils.equals(bean.alipay_trade_query_response.code, "10000");
                    }
                })
                .takeUntil(new Func1<String, Boolean>() {
                    @Override
                    public Boolean call(String s) {
                        AliResultBean bean = new Gson().fromJson(s, AliResultBean.class);
                        return TextUtils.equals(bean.alipay_trade_query_response.code, "10000");
                    }
                })
                .subscribe(payPresenter.getQuerySub());
    }

        @Override
        public void aliCancle () {
            final HttpServer server = RetrofitUtils.getInstance().getStringClient(ConfigUtlis.getConfig("open_api_domain"));
            final HttpParams params = getAliParams(HttpParams.PARAMS_CANCLE);
            final Observable<String> aliPay = server.getAliPay(params.getParams());
            aliPay.subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .repeatWhen(new Func1<Observable<? extends Void>, Observable<?>>() {
                        @Override
                        public Observable<?> call(Observable<? extends Void> observable) {
                            return observable
                                    .zipWith(Observable.range(1, times), new Func2<Void, Integer, Integer>() {
                                        @Override
                                        public Integer call(Void aVoid, Integer integer) {
                                            return integer;
                                        }
                                    }).flatMap(new Func1<Integer, Observable<Long>>() {
                                        @Override
                                        public Observable<Long> call(Integer integer) {
                                            return Observable.timer(integer * 6, TimeUnit.SECONDS);
                                        }
                                    });

                        }
                    })
                    .filter(new Func1<String, Boolean>() {
                        @Override
                        public Boolean call(String s) {
                            JLog.i("-----------filter---------------");
                            AliResultBean bean = new Gson().fromJson(s, AliResultBean.class);
                            return TextUtils.equals(bean.alipay_trade_query_response.code, "10000");
                        }
                    })
                    .takeUntil(new Func1<String, Boolean>() {
                        @Override
                        public Boolean call(String s) {
                            AliResultBean bean = new Gson().fromJson(s, AliResultBean.class);
                            return TextUtils.equals(bean.alipay_trade_query_response.code, "10000");
                        }
                    })
                    .subscribe(cancle);
        }


        @Override
        public void wxPay ( int function, long delayTime){
            globalBean.type = function;
            HttpParams params = null;
            HttpServer server = RetrofitUtils.getInstance().getStringClient(ConfigUtlis.getConfig("open_api_wx"));
            String address = "";
            switch (function) {
                case HttpParams.PARAMS_SCAN://扫码支付
                    params = HttpParams.getInstance().getWeixinPamars(HttpParams.PARAMS_SCAN);
                    address = ConfigUtlis.getConfig("wx_bar_method");
                    break;
                case PARAMS_BAR://条码支付,微信刷卡支付
                    params = HttpParams.getInstance().getWeixinPamars(PARAMS_BAR);
                    params.put("auth_code", aliBean.auth_code);
                    address = ConfigUtlis.getConfig("wx_scan_method");
                    break;
            }


            StringBuffer sb = new StringBuffer();
            sb.append(params.toString()).append("&key=").append(ConfigUtlis.getConfig("wx_key"));
            String sign = Utils.MD5(sb.toString()).toUpperCase();
            params.put("sign", sign.toUpperCase());
            String body = haipo.com.receive.utils.XmlUtils.mapToXml(params.getParams());
            wxPayBody = server.getWXScanPayBody(address, body);
            wxPayBody.subscribeOn(Schedulers.io()).observeOn(Schedulers.io())
                    .delay(delayTime, TimeUnit.SECONDS)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(payPresenter);
        }

        @Override
        public void cancle ( int function){
            payPresenter.unsubscribe();
        }


    }
