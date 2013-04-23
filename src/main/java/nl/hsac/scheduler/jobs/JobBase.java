package nl.hsac.scheduler.jobs;

import nl.hsac.scheduler.util.StatusManager;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.quartz.TriggerKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Base class for Jobs.
 */
// suppress since we want to log using the actual class and not this abstract base class
@SuppressWarnings("PMD.LoggerIsNotStaticFinal")
public abstract class JobBase implements Job {
    private static final String JOB_CLASS_KEY = "jobClass";
    private static final String JOB_LOGGER_KEY = "jobLogger";
    private static final String JOB_KEY_KEY = "jobKey";
    private static final String JOB_NAME_KEY = "jobName";
    private static final String JOB_GRP_KEY = "jobGroup";
    private static final String TRIGGER_KEY_KEY = "triggerKey";
    private static final String TRIGGER_NAME_KEY = "triggerName";
    private static final String TRIGGER_GRP_KEY = "triggerGroup";

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        MDC.put(JOB_CLASS_KEY, jobExecutionContext.getJobDetail().getJobClass().getName());
        JobKey jobKey = jobExecutionContext.getJobDetail().getKey();
        MDC.put(JOB_KEY_KEY, jobKey.toString());
        MDC.put(JOB_NAME_KEY, jobKey.getName());
        MDC.put(JOB_GRP_KEY, jobKey.getGroup());
        TriggerKey triggerKey = jobExecutionContext.getTrigger().getKey();
        MDC.put(TRIGGER_KEY_KEY, triggerKey.toString());
        MDC.put(TRIGGER_NAME_KEY, triggerKey.getName());
        MDC.put(TRIGGER_GRP_KEY, triggerKey.getGroup());

        String logger = String.format("%s.%s", JobBase.class.getPackage().getName(), jobKey.toString());
        MDC.put(JOB_LOGGER_KEY, logger);
        try {
            executeImpl(jobExecutionContext);
        } catch (JobExecutionException e) {
            reportStatus(jobExecutionContext, false, e.getMessage());
            throw e;
        } catch (RuntimeException e) {
            reportStatus(jobExecutionContext, false, e.toString());
            throw e;
        } finally {
            MDC.remove(JOB_LOGGER_KEY);
            MDC.remove(TRIGGER_GRP_KEY);
            MDC.remove(TRIGGER_NAME_KEY);
            MDC.remove(TRIGGER_KEY_KEY);
            MDC.remove(JOB_GRP_KEY);
            MDC.remove(JOB_NAME_KEY);
            MDC.remove(JOB_KEY_KEY);
            MDC.remove(JOB_CLASS_KEY);
        }
    }

    /**
     * Performs actual work for Job.
     * @param jobExecutionContext context for job.
     * @throws org.quartz.JobExecutionException if job encountered a problem.
     */
    protected abstract void executeImpl(JobExecutionContext jobExecutionContext) throws JobExecutionException;

    /**
     * @return logger to use.
     */
    public Logger getLog() {
        String logger = MDC.get(JOB_LOGGER_KEY);
        Logger result;
        if (logger == null) {
            result = log;
        } else {
            result = LoggerFactory.getLogger(logger);
        }
        return result;
    }

    /**
     * Indicates status of last execution (for status check).
     * @param context job context.
     * @param status whether job went OK.
     * @param message message to publish.
     */
    protected void reportStatus(JobExecutionContext context, boolean status, String message) {
        String jobKey = context.getJobDetail().getKey().toString();
        String msg = getMessage(context, message);
        StatusManager.getInstance().storeStatus(status, jobKey, msg);
    }

    private String getMessage(JobExecutionContext context, String message) {
        StringBuilder result = new StringBuilder();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        result.append(" (fired on: ");
        Date fireTime = context.getFireTime();
        if (fireTime == null) {
            result.append("unknown");
        } else {
            result.append(format.format(fireTime));
        }
        result.append(" by ");
        result.append(context.getTrigger().getKey());
        result.append(", next scheduled fire time: ");
        Date nextFireTime = context.getNextFireTime();
        if (nextFireTime == null) {
            result.append("none");
        } else {
            result.append(format.format(nextFireTime));
        }
        result.append(").");
        if (message != null) {
            result.append(" Message: '");
            result.append(message);
            result.append("'");
        }
        return result.toString();
    }
}
