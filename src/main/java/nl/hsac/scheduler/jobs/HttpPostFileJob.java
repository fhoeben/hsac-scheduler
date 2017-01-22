package nl.hsac.scheduler.jobs;

import nl.hsac.scheduler.util.FileUtil;
import nl.hsac.scheduler.util.HttpClient;
import org.quartz.JobDataMap;

/**
 * Job that posts the contents of a (UTF-8) file on the classpath.
 */
public class HttpPostFileJob extends HttpPostJob {
    /** Key for file to post. */
    public static final String FILENAME_KEY = "filename";

    /**
     * Creates new, with new HttpClient.
     */
    public HttpPostFileJob() {
        super();
    }

    /**
     * Creates new.
     * @param client http client to use.
     */
    public HttpPostFileJob(HttpClient client) {
        super(client);
    }

    @Override
    protected String getRequest(JobDataMap jobDataMap) {
        String filename = jobDataMap.getString(FILENAME_KEY);
        return FileUtil.loadFile(filename);
    }
}
