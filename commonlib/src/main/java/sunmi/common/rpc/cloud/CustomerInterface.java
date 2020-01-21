package sunmi.common.rpc.cloud;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import sunmi.common.model.CustomerAgeGenderResp;
import sunmi.common.model.CustomerAgeNewOldResp;
import sunmi.common.model.CustomerCountResp;
import sunmi.common.model.CustomerDataResp;
import sunmi.common.model.CustomerEnterRateTrendResp;
import sunmi.common.model.CustomerFrequencyAvgResp;
import sunmi.common.model.CustomerFrequencyDistributionResp;
import sunmi.common.model.CustomerFrequencyTrendResp;
import sunmi.common.model.CustomerHistoryDetailResp;
import sunmi.common.model.CustomerHistoryResp;
import sunmi.common.model.CustomerHistoryTrendResp;
import sunmi.common.model.CustomerRateResp;
import sunmi.common.rpc.retrofit.BaseRequest;
import sunmi.common.rpc.retrofit.BaseResponse;

/**
 * @author yinhui
 * @date 2019-09-18
 */
public interface CustomerInterface {

    String path = "api/passengerFlow/statistic/";

    /**
     * 按时间获取客流统计数据（今日、本周、本月，昨日、上周、上月）
     */
    @POST(path + "getLatest")
    Call<BaseResponse<CustomerCountResp>> getCustomer(@Body BaseRequest request);

    /**
     * 按时间获取交易转化率，客流数和订单数（今日，本周，本月）
     */
    @POST(path + "order/getLatest")
    Call<BaseResponse<CustomerRateResp>> getCustomerRate(@Body BaseRequest request);

    /**
     * 按时间获取客流年龄性别分布
     */
    @POST(path + "age/getByGender")
    Call<BaseResponse<CustomerAgeGenderResp>> getCustomerByAgeGender(@Body BaseRequest request);

    /**
     * 按时间获取客流年龄生熟分布
     */
    @POST(path + "age/getByRegular")
    Call<BaseResponse<CustomerAgeNewOldResp>> getCustomerByAgeNewOld(@Body BaseRequest request);

    /**
     * 按时间获取历史客流统计数据（今日、本周、本月，昨日）
     */
    @POST(path + "getHistory")
    @Deprecated
    Call<BaseResponse<CustomerHistoryResp>> getHistoryCustomer(@Body BaseRequest request);

    /**
     * 按时间获取历史客流统计数据（自定义时间段）
     */
    @POST(path + "history/getByTimeRange")
    Call<BaseResponse<CustomerHistoryResp>> getHistoryCustomerByRange(@Body BaseRequest request);

    /**
     * 获取客流变化趋势（今日、本周、本月，昨日）
     */
    @POST(path + "history/getList")
    Call<BaseResponse<CustomerHistoryTrendResp>> getHistoryCustomerTrend(@Body BaseRequest request);

    /**
     * 获取客流变化趋势（今日、本周、本月，昨日）
     */
    @POST(path + "age/getHistoryByGender")
    Call<BaseResponse<CustomerHistoryDetailResp>> getHistoryCustomerDetail(@Body BaseRequest request);

    /**
     * T+1客流分析概览(2:本周 3:本月 4:昨日)
     */
    @POST(path + "history/getOverview")
    Call<BaseResponse<CustomerDataResp>> getCustomerData(@Body BaseRequest request);

    /**
     * T+1客流分析-进店率趋势( 2:本周 3:本月 4:昨日)
     *
     * @param request
     * @return
     */
    @POST(path + "history/getEnteringDistribution")
    Call<BaseResponse<CustomerEnterRateTrendResp>> getCustomerEnterRateTrend(@Body BaseRequest request);


    /**
     * T+1客流分析-客群到店频率分布(2:本周 3:本月 4:昨日)
     *
     * @param request
     * @return
     */
    @POST(path + "history/getFrequencyList")
    Call<BaseResponse<CustomerFrequencyDistributionResp>> getCustomerFrequencyDistribution(@Body BaseRequest request);

    /**
     * T+1客流分析-客群到店频率趋势(2:本周 3:本月)
     *
     * @param request
     * @return
     */
    @POST(path + "history/getFrequencyDistribution")
    Call<BaseResponse<CustomerFrequencyTrendResp>> getCustomerFrequencyTrend(@Body BaseRequest request);

    /**
     * T+1客流分析-客群平均到店频率(2:本周 3:本月 )
     * @param request
     * @return
     */
    @POST(path + "history/getFrequencyWithAgeAndGender")
    Call<BaseResponse<CustomerFrequencyAvgResp>> getCustomerFrequencyAvg(@Body BaseRequest request);

}
