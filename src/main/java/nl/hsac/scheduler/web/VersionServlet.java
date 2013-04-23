package nl.hsac.scheduler.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

/**
 *  Servlet to get version information.
 *
 *  Example pom content in application to fill manifest with version info, using TeamCity:
<pre>
 &lt;plugin>
 &lt;groupId>org.apache.maven.plugins&lt;/groupId>
 &lt;artifactId>maven-war-plugin&lt;/artifactId>
 &lt;version>2.3&lt;/version>
 &lt;configuration>
 &lt;archive>
 &lt;manifest>
 &lt;addDefaultImplementationEntries>true&lt;/addDefaultImplementationEntries>
 &lt;/manifest>
 &lt;manifestEntries>
 &lt;Implementation-Source-Revision>${build.vcs.number}&lt;/Implementation-Source-Revision>
 &lt;/manifestEntries>
 &lt;/archive>
 &lt;warName>scheduler&lt;/warName>
 &lt;failOnMissingWebXml>false&lt;/failOnMissingWebXml>
 &lt;/configuration>
 &lt;/plugin>
 </pre>
 */
public class VersionServlet extends HttpServlet {
    private static final Logger LOG = LoggerFactory.getLogger(VersionServlet.class);

    @Override
    protected void doGet(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse)
            throws IOException {
        String version = getVersion(httpServletRequest.getServletContext());
        httpServletResponse.setContentType("text/plain");
        PrintWriter writer = httpServletResponse.getWriter();
        writer.append(version);
        writer.flush();
    }

    /**
     * Gets version information, using attributes in manifest.
     * @param servletContext context to retrieve version for.
     * @return version number.
     */
    public static String getVersion(ServletContext servletContext) {
        String version = "unknown";
        InputStream inputStream = null;
        try {
            inputStream = servletContext.getResourceAsStream("/META-INF/MANIFEST.MF");
            if (inputStream != null) {
                Manifest manifest = new Manifest(inputStream);
                Attributes attributes = manifest.getMainAttributes();
                String impVersion = attributes.getValue("Implementation-Version");
                String sourceVersion = attributes.getValue("Implementation-Source-Revision");
                version = impVersion + "." + sourceVersion;
            }
        } catch (Exception e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Exception getting version from manifest", e);
            }
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException ex) {
                    LOG.debug("Unable to close stream", ex);
                }
            }
        }
        return version;
    }
}
