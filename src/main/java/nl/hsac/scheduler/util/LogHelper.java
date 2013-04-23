package nl.hsac.scheduler.util;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.FileAppender;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Helper to get access to log files.
 */
@SuppressWarnings("PMD.MoreThanOneLogger")
public final class LogHelper {
    private static final Logger LOG = (Logger) LoggerFactory.getLogger(LogHelper.class);

    /**
     * Gets the file a specific appender is writing to, if it can be read.
     * @param aLogger logger to find appender for
     * @param anAppenderName name of appender to locate
     * @return file being written to, if readable<br>
     *         <code>null</code>, if the appender does not exist, the it is
     *                            not a file appender, or the file is not
     *                            readable
     */
    public File getLogFile(Logger aLogger, String anAppenderName) {
        File result = null;
        if (aLogger != null && anAppenderName != null && anAppenderName.length() > 0) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Looking for appender: " + anAppenderName);
            }
            result = getLogFileInternal(aLogger, anAppenderName);
        } else {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Returning null because not all arguments filled. Log: " + aLogger
                        + ". AppenderName: " + anAppenderName);
            }
        }
        return result;
    }

    private File getLogFileInternal(Logger aLogger, String anAppenderName) {
        File result = null;
        Logger currentLogger = aLogger;
        while (currentLogger != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Processing logger: " + currentLogger.getName());
            }
            Appender<?> app = currentLogger.getAppender(anAppenderName);
            if (app instanceof FileAppender) {
                @SuppressWarnings("rawtypes")
                FileAppender fApp = (FileAppender) app;
                result = readableFile(fApp.getFile());
                currentLogger = null;
            } else {
                if (app == null) {
                    currentLogger = getParent(currentLogger);
                } else {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Found appender, but it is not a file appender: " + app.getClass());
                    }
                    currentLogger = null;
                }
            }
        }
        return result;
    }

    /**
     * Gets the parent Logger, if the supplied logger forwards up the hierarchy.
     * @param logger logger to start search from.
     * @return parent logger, or null if logger is not additive.
     */
    private Logger getParent(Logger logger) {
        //The root logger is always the parent of every other logger.
        Logger parent = null;
        if (logger.isAdditive()) {
            String name = logger.getName();
            LOG.debug(name);
            int lastDotIndex = name.lastIndexOf('.');
            if (lastDotIndex > 0) {
                String parentName = name.substring(0, lastDotIndex);
                parent = logger.getLoggerContext().getLogger(parentName);
            } else {
                if (!Logger.ROOT_LOGGER_NAME.equals(name)) {
                    parent = logger.getLoggerContext().getLogger(Logger.ROOT_LOGGER_NAME);
                }
            }
        }
        return parent;
    }

    /**
     * Gets all files appenders for a certain logger are writing to, if they can be read.
     * @param logger logger to find files for
     * @return readable files being written to
     */
    @SuppressWarnings("rawtypes")
    public File[] getLogFiles(Logger logger) {
        List<File> result = new ArrayList<File>();
        Logger currentLogger = logger;
        while (currentLogger != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Processing logger: " + currentLogger.getName());
            }
            Iterator<Appender<ILoggingEvent>> it = currentLogger.iteratorForAppenders();
            if (it != null) {
                while (it.hasNext()) {
                    Appender app = (Appender) it.next();
                    if (app instanceof FileAppender) {
                        FileAppender fApp = (FileAppender) app;
                        File f = readableFile(fApp.getFile());
                        if (f != null) {
                            result.add(f);
                        }
                    } else {
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("Skipping appender that is not a FileAppender: " + app);
                        }
                    }
                }
            }
            currentLogger = getParent(currentLogger);
        }
        return result.toArray(new File[result.size()]);
    }

    private File readableFile(String file) {
        File result = null;
        File f = new File(file);
        if (f.canRead()) {
            result = f;
        } else {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Found log file, but can not read it: " + f.getAbsolutePath());
            }
        }
        return result;
    }
}
