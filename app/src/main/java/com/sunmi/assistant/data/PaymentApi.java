package com.sunmi.assistant.data;

import com.sunmi.assistant.data.response.OrderDetailListResp;
import com.sunmi.assistant.data.response.OrderListResp;
import com.sunmi.assistant.data.response.OrderPayTypeListResp;
import com.sunmi.assistant.data.response.OrderTotalAmountResp;
import com.sunmi.assistant.data.response.OrderTotalCountResp;
import com.sunmi.assistant.data.response.OrderTypeListResp;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import retrofit2.Call;
import sunmi.common.constant.CommonConfig;
import sunmi.common.rpc.cloud.SunmiStoreRetrofitClient;
import sunmi.common.rpc.retrofit.BaseRequest;
import sunmi.common.rpc.retrofit.BaseResponse;
import sunmi.common.rpc.retrofit.RetrofitCallback;
import sunmi.common.utils.DateTimeUtils;
import sunmi.common.utils.SafeUtils;
import sunmi.common.utils.SpUtils;

/**
 * 订单管理远程接口
 *
 * @author yinhui
 * @since 2019-06-20
 */
public class PaymentApi {

    private static final class Holder {
        private static final PaymentApi INSTANCE = new PaymentApi();
    }

    public static PaymentApi get() {
        return Holder.INSTANCE;
    }

    private PaymentApi() {
    }

    public Call<BaseResponse<OrderTotalAmountResp>> getOrderTotalAmount(int companyId, int shopId, long timeStart, long timeEnd,
                                                                        int rateFlag, RetrofitCallback<OrderTotalAmountResp> callback) {
        Call<BaseResponse<OrderTotalAmountResp>> call = null;
        try {
            String params = new JSONObject()
                    .put("company_id", companyId)
                    .put("shop_id", shopId)
                    .put("time_range_start", timeStart)
                    .put("time_range_end", timeEnd)
                    .put("rate_required", rateFlag)
                    .toString();
            call = SunmiStoreRetrofitClient.getInstance().create(PaymentInterface.class)
                    .getTotalAmount(createRequestBody(params));
            call.enqueue(callback);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return call;
    }

    public Call<BaseResponse<OrderTotalCountResp>> getOrderTotalCount(int companyId, int shopId, long timeStart, long timeEnd,
                                                                      int rateFlag, RetrofitCallback<OrderTotalCountResp> callback) {
        Call<BaseResponse<OrderTotalCountResp>> call = null;
        try {
            String params = new JSONObject()
                    .put("company_id", companyId)
                    .put("shop_id", shopId)
                    .put("time_range_start", timeStart)
                    .put("time_range_end", timeEnd)
                    .put("rate_required", rateFlag)
                    .toString();
            call = SunmiStoreRetrofitClient.getInstance().create(PaymentInterface.class)
                    .getTotalCount(createRequestBody(params));
            call.enqueue(callback);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return call;
    }

    public Call<BaseResponse<OrderTypeListResp>> getOrderTypeList(
            int companyId, int shopId, RetrofitCallback<OrderTypeListResp> callback) {
        Call<BaseResponse<OrderTypeListResp>> call = null;
        try {
            String params = new JSONObject()
                    .put("company_id", companyId)
                    .put("shop_id", shopId)
                    .toString();
            call = SunmiStoreRetrofitClient.getInstance().create(PaymentInterface.class)
                    .getOrderTypeList(createRequestBody(params));
            call.enqueue(callback);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return call;
    }

    public Call<BaseResponse<OrderPayTypeListResp>> getOrderPurchaseTypeList(
            int companyId, int shopId, RetrofitCallback<OrderPayTypeListResp> callback) {
        Call<BaseResponse<OrderPayTypeListResp>> call = null;
        try {
            String params = new JSONObject()
                    .put("company_id", companyId)
                    .put("shop_id", shopId)
                    .toString();
            call = SunmiStoreRetrofitClient.getInstance().create(PaymentInterface.class)
                    .getPurchaseTypeList(createRequestBody(params));
            call.enqueue(callback);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return call;
    }

    public Call<BaseResponse<OrderListResp>> getOrderList(int companyId, int shopId, long timeStart, long timeEnd,
                                                          int amountOrder, int timeOrder, List<Integer> orderType, List<Integer> purchaseType,
                                                          int pageNum, int pageSize, RetrofitCallback<OrderListResp> callback) {
        Call<BaseResponse<OrderListResp>> call = null;
        try {
            JSONArray orderArray = new JSONArray();
            if (orderType != null && !orderType.isEmpty()) {
                for (int order : orderType) {
                    orderArray.put(order);
                }
            }
            JSONArray purchaseArray = new JSONArray();
            if (purchaseType != null && !purchaseType.isEmpty()) {
                for (int purchase : purchaseType) {
                    purchaseArray.put(purchase);
                }
            }
            String params = new JSONObject()
                    .put("company_id", companyId)
                    .put("shop_id", shopId)
                    .put("time_range_start", timeStart)
                    .put("time_range_end", timeEnd)
                    .put("sort_by_amount", amountOrder)
                    .put("sort_by_time", timeOrder)
                    .put("order_type_list", orderArray)
                    .put("purchase_type_list", purchaseArray)
                    .put("page_num", pageNum)
                    .put("page_size", pageSize)
                    .toString();
            call = SunmiStoreRetrofitClient.getInstance().create(PaymentInterface.class)
                    .getList(createRequestBody(params));
            call.enqueue(callback);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return call;
    }

    public Call<BaseResponse<OrderDetailListResp>> getOrderDetailList(int orderId,
                                                                      RetrofitCallback<OrderDetailListResp> callback) {
        Call<BaseResponse<OrderDetailListResp>> call = null;
        try {
            String params = new JSONObject()
                    .put("company_id", SpUtils.getCompanyId())
                    .put("shop_id", SpUtils.getShopId())
                    .put("order_id", orderId)
                    .toString();
            call = SunmiStoreRetrofitClient.getInstance().create(PaymentInterface.class)
                    .getDetailList(createRequestBody(params));
            call.enqueue(callback);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return call;
    }

    private BaseRequest createRequestBody(String params) {
        String timeStamp = DateTimeUtils.currentTimeSecond() + "";
        String randomNum = (int) ((Math.random() * 9 + 1) * 100000) + "";
        String isEncrypted = "0";
        String sign = SafeUtils.md5(params + isEncrypted +
                timeStamp + randomNum + SafeUtils.md5(CommonConfig.CLOUD_TOKEN));
        return new BaseRequest.Builder()
                .setTimeStamp(timeStamp)
                .setRandomNum(randomNum)
                .setIsEncrypted(isEncrypted)
                .setParams(params)
                .setSign(sign)
                .setLang("zh").createBaseRequest();
    }

}
