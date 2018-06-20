package com.wjc.buffer;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;

public class MyMojo
    extends AbstractMojo
{
    /**
     * Location of the file.
     * @parameter expression="${project}"
     * @readonly
     */
    MavenProject project;

    @Override
    public void execute()
        throws MojoExecutionException
    {
        getLog().info("The name is : " + project.getName());
    }

    public static void main(String[] args) throws MojoExecutionException {
        new MyMojo().execute();
    }
}