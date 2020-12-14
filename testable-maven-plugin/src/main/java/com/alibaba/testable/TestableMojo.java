package com.alibaba.testable;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

import java.util.Map;
import java.util.Properties;

/**
 * Goal which prepare testable agent.
 *
 * @author flin
 */
@Mojo(name = "prepare", defaultPhase = LifecyclePhase.INITIALIZE,
    requiresDependencyResolution = ResolutionScope.RUNTIME, threadSafe = true)
public class TestableMojo extends AbstractMojo
{
    /**
     * Maven project.
     */
    @Parameter(property = "project", required = true, readonly = true)
    private MavenProject project;

    /**
     * Map of plugin artifacts.
     */
    @Parameter(property = "plugin.artifactMap", required = true, readonly = true)
    private Map<String, Artifact> pluginArtifactMap;

    /**
     * JavaAgent log level (mute/debug/verbose)
     */
    @Parameter
    private String logLevel;

    /**
     * Name of the Testable Agent artifact.
     */
    private static final String AGENT_ARTIFACT_NAME = "com.alibaba.testable:testable-agent";
    /**
     * Name of the property used in maven-osgi-test-plugin.
     */
    private static final String TYCHO_ARG_LINE = "tycho.testArgLine";
    /**
     * Name of the property used in maven-surefire-plugin.
     */
    private static final String SUREFIRE_ARG_LINE = "argLine";
    /**
     * Name of eclipse test plugin
     */
    private static final String ECLIPSE_TEST_PLUGIN = "eclipse-test-plugin";

    @Override
    public void execute() throws MojoExecutionException
    {
        final String testArgsPropertyKey = getEffectivePropertyKey();
        final Properties projectProperties = project.getProperties();
        if (projectProperties == null) {
            getLog().error("failed to fetch project properties");
            return;
        }
        String extraArgs = "";
        if (!logLevel.isEmpty()) {
            extraArgs += logLevel;
        }
        final String oldArgs = projectProperties.getProperty(testArgsPropertyKey);
        String newArgs = (oldArgs == null) ? getAgentJarArgs().trim() : (oldArgs + getAgentJarArgs());
        getLog().info(testArgsPropertyKey + " set to " + newArgs);
        if (!extraArgs.isEmpty()) {
            newArgs += ("=" + extraArgs);
        }
        projectProperties.setProperty(testArgsPropertyKey, newArgs);
    }

    private String getAgentJarArgs() {
        final Artifact testableAgentArtifact = pluginArtifactMap.get(AGENT_ARTIFACT_NAME);
        if (testableAgentArtifact == null) {
            getLog().error("failed to find testable agent jar");
        }
        return " -javaagent:" + testableAgentArtifact.getFile().getAbsolutePath();
    }

    private String getEffectivePropertyKey() {
        return ECLIPSE_TEST_PLUGIN.equals(project.getPackaging()) ? TYCHO_ARG_LINE : SUREFIRE_ARG_LINE;
    }
}
