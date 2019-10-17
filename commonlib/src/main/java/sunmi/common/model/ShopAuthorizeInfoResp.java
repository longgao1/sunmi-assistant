package sunmi.common.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * @author yinhui
 * @date 2019-10-17
 */
public class ShopAuthorizeInfoResp {

    @SerializedName("authorized_list")
    private List<Info> authorizedList;

    public List<Info> getAuthorizedList() {
        return authorizedList;
    }

    public static class Info {
        /**
         * authorized_time : 123456789
         * import_status : 1
         * shop_no : S001000013
         * saas_source : 1
         */

        @SerializedName("authorized_time")
        private long authorizedTime;
        @SerializedName("import_status")
        private int importStatus;
        @SerializedName("shop_no")
        private String shopNo;
        @SerializedName("saas_source")
        private int saasSource;

        /**
         * 获取授权时间
         *
         * @return 授权时间Unix时间戳
         */
        public long getAuthorizedTime() {
            return authorizedTime;
        }

        /**
         * 获取授权状态
         *
         * @return 1：已授权；2：未授权
         */
        public int getImportStatus() {
            return importStatus;
        }

        public String getShopNo() {
            return shopNo;
        }

        /**
         * 获取历史数据导入状态
         * 0：未导入
         * 1：导入中
         * 2：导入成功
         * 3：导入失败
         *
         * @return 导入状态
         */
        public int getSaasSource() {
            return saasSource;
        }
    }
}
