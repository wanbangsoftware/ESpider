package com.hlk.wbs.espider.tasks;

import com.google.gson.Gson;
import com.hlk.wbs.espider.api.Api;
import com.hlk.wbs.espider.applications.App;
import com.hlk.wbs.espider.etc.Utils;
import com.hlk.wbs.espider.helpers.PreferenceHelper;
import com.hlk.wbs.espider.models.Account;
import com.hlk.wbs.espider.services.NotificationService;

/**
 * <b>功能：</b>拉取服务器上消息的Task<br />
 * <b>作者：</b>Hsiang Leekwok <br />
 * <b>时间：</b>2016/09/03 10:32 <br />
 * <b>邮箱：</b>xiang.l.g@gmail.com <br />
 */
public class FetchingMessageTask extends AsyncExecutableTask<Void, Void, Void> {

    private Gson gson = App.getInstance().Gson();

    @Override
    protected Void doInTask(Void... params) {
        String topic = PreferenceHelper.get(NotificationService.TOPIC, "");
        if (Utils.isEmpty(topic)) {
            log("Do not need to fetching message, \"topic\" is empty.");
        } else {
            String api = Api.ApiUrl();
            Account account = new Account();
            account.name = topic;
            String data = Api.GetParameter(account);
            fetchingJson(api, data);
            if (null != result && result.State == 0) {

            }
        }
        return null;
    }

    @Override
    protected void doAfterExecute() {

    }
}
