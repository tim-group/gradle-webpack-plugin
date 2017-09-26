package com.timgroup.gradle.webpack;

import javax.annotation.Nonnull;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.tasks.Delete;

public class WebpackPlugin implements Plugin<Project> {
    @Override
    public void apply(@Nonnull Project project) {
        project.getPluginManager().apply("base");

        ((Delete) project.getTasks().getByName("clean")).delete("node_modules");

        NpmInstallTask npmInstallTask = project.getTasks().create("npmInstall", NpmInstallTask.class);

        WebpackTask webpackTask = project.getTasks().create("webpack", WebpackTask.class);
        webpackTask.getDependsOn().add(npmInstallTask);
        webpackTask.setOutput("build/site");
        webpackTask.setConfigFile("webpack.config.js");
        webpackTask.setSources("src/main/javascript");
        webpackTask.setOptions("-p", "--devtool", "source-map");
        project.getTasks().getByName("assemble").getDependsOn().add(webpackTask);

        MochaTestTask mochaTestTask = project.getTasks().create("mochaTest", MochaTestTask.class);
        mochaTestTask.getDependsOn().add(npmInstallTask);
        mochaTestTask.setMainFiles("src/main/javascript");
        mochaTestTask.setTestFiles("src/test/javascript");
        mochaTestTask.setTestOutput("build/test-results/mochaTest/test-reports.xml");
        mochaTestTask.setMochaOptionsFile("mocha.opts");

        project.getTasks().getByName("check").getDependsOn().add(mochaTestTask);
    }
}
