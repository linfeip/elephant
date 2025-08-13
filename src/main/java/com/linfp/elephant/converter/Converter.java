package com.linfp.elephant.converter;

import com.linfp.elephant.api.RunRequest;
import com.linfp.elephant.runner.ActionData;

public class Converter {

    public static ActionData convert(RunRequest.Action act) {
        var data = new ActionData();
        data.data = act.data;
        data.timeout = act.timeout;
        data.delay = act.delay;
        data.comment = act.comment;
        data.loop = act.loop;
        return data;
    }
}
