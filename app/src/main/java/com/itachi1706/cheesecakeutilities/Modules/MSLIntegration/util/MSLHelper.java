package com.itachi1706.cheesecakeutilities.Modules.MSLIntegration.util;

import com.itachi1706.cheesecakeutilities.Modules.MSLIntegration.model.MSLData;

/**
 * Created by Kenneth on 17/1/2019.
 * for com.itachi1706.cheesecakeutilities.Modules.MSLIntegration.util in CheesecakeUtilities
 */
public class MSLHelper {

    public static boolean completeMatch(MSLData.Task task, MSLData.Task otherTask) {
        if (checkNull(task.getGuid(), otherTask.getGuid()) != 0) return false;
        if (task.getGuid() != null && !task.getGuid().equals(otherTask.getGuid())) return false;

        if (checkNull(task.getType(), otherTask.getType()) != 0) return false;
        if (task.getType() != null && !task.getType().equals(otherTask.getType())) return false;

        if (checkNull(task.getTitle(), otherTask.getTitle()) != 0) return false;
        if (task.getTitle() != null && !task.getTitle().equals(otherTask.getTitle())) return false;

        if (checkNull(task.getDetail(), otherTask.getDetail()) != 0) return false;
        if (task.getDetail() != null && !task.getDetail().equals(otherTask.getDetail())) return false;

        if (checkNull(task.getDue_date(), otherTask.getDue_date()) != 0) return false;
        if (task.getDue_date() != null && !task.getDue_date().equals(otherTask.getDue_date())) return false;

        if (checkNull(task.getTimestamp(), otherTask.getTimestamp()) != 0) return false;
        if (task.getTimestamp() != otherTask.getTimestamp()) return false;

        if (checkNull(task.getCompleted_at(), otherTask.getCompleted_at()) != 0) return false;
        if (task.getCompleted_at() != null && !task.getCompleted_at().equals(otherTask.getCompleted_at())) return false;

        if (checkNull(task.getSubject_guid(), otherTask.getSubject_guid()) != 0) return false;
        if (task.getSubject_guid() != null && !task.getSubject_guid().equals(otherTask.getSubject_guid())) return false;

        if (checkNull(task.getProgress(), otherTask.getProgress()) != 0) return false;
        return task.getProgress() == otherTask.getProgress();
    }

    /**
     * Check if either object is null
     * @param o1 First Object
     * @param o2 Second Object
     * @return 0 if both null/not-null, -1 if o1 is null, 1 if o2 is null
     */
    private static int checkNull(Object o1, Object o2) {
        if (o1 == null && o2 == null) return 0;
        else if (o1 == null) return -1;
        else if (o2 == null) return 1;
        else return 0;
    }
}
