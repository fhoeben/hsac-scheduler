package nl.hsac.scheduler.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks job statuses.
 */
public final class StatusManager {
    /** Start of response in case all is well. */
    public final static String OK = "OK";
    /** Start of response in case all is is NOT well. */
    public final static String NOK = "NOK";

    private final static StatusManager INSTANCE = new StatusManager();

    private final ConcurrentHashMap<String, String> statuses = new ConcurrentHashMap<String, String>();

    private StatusManager() {
        // prevent instances
    }

    /**
     * Provides access to singleton.
     * @return singleton instance.
     */
    public static StatusManager getInstance() {
        return INSTANCE;
    }

    /**
     * Adds or updates status of an element.
     * @param success whether status is OK or not.
     * @param name name of element.
     * @param msg message to accompany status.
     */
    public void storeStatus(boolean success, String name, String msg) {
        String status = getStatus(success, name, msg);
        statuses.put(name, status);
    }

    private String getStatus(boolean success, String name, String msg) {
        StringBuilder result = new StringBuilder();
        if (success) {
            result.append(OK);
        } else {
            result.append(NOK);
        }
        result.append(": ");
        result.append(name);
        if (msg != null) {
            result.append(msg);
        }
        return result.toString();
    }

    /**
     * @return all known statuses (sorted by name of reporter).
     */
    public Collection<String> getStatuses() {
        List<String> result = new ArrayList<String>();
        SortedSet<String> names = new TreeSet<String>(statuses.keySet());
        for (String name : names) {
            String status = statuses.get(name);
            if (status != null) {
                result.add(status);
            }
        }
        return result;
    }
}
