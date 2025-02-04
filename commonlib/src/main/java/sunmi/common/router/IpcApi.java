package sunmi.common.router;

import android.content.Context;

import com.xiaojinzi.component.anno.ParameterAnno;
import com.xiaojinzi.component.anno.router.HostAnno;
import com.xiaojinzi.component.anno.router.PathAnno;
import com.xiaojinzi.component.anno.router.RouterApiAnno;

import java.util.ArrayList;

import sunmi.common.constant.RouterConfig;

/**
 * Description:
 *
 * @author linyuanpeng on 2019-11-20.
 */
@RouterApiAnno()
@HostAnno(RouterConfig.Ipc.NAME)
public interface IpcApi {

    @PathAnno(RouterConfig.Ipc.IPC_START_CONFIG)
    void goToIpcStartConfig(Context context, @ParameterAnno("ipcType") int type, @ParameterAnno("source") int source);

    @PathAnno(RouterConfig.Ipc.CASH_VIDEO_OVERVIEW)
    void goToCashVideoOverview(Context context, @ParameterAnno("serviceBeans") ArrayList beans, @ParameterAnno("isSingleDevice") boolean isSingle, @ParameterAnno("hasCashLossPrevent") boolean hasPrevent);
}
