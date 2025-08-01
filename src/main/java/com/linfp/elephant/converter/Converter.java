package com.linfp.elephant.converter;

import com.linfp.elephant.api.RunRequest;
import com.linfp.elephant.robot.ActionData;

public class Converter {

    public static ActionData convert(RunRequest.Action act) {
        var data = new ActionData();
        data.setData(act.data);
        data.setTimeout(act.timeout);
        data.setDelay(act.delay);
        data.setComment(act.comment);
        data.setLoop(act.loop);
        return data;
    }
}
