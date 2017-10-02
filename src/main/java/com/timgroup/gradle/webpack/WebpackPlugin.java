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
        npmInstallTask.setGroup("compile");
        npmInstallTask.setDescription("Runs NPM to fetch javascript packages into node_modules");

        WebpackTask webpackTask = project.getTasks().create("webpack", WebpackTask.class);
        webpackTask.getDependsOn().add(npmInstallTask);
        webpackTask.setOutput("build/site");
        webpackTask.setConfigFile("webpack.config.js");
        webpackTask.setSources("src/main/javascript");
        webpackTask.setOptions("-p", "--devtool", "source-map");
        webpackTask.setGroup("compile");
        webpackTask.setDescription("Runs Webpack to produce bundle files");
        project.getTasks().getByName("assemble").getDependsOn().add(webpackTask);

        MochaTestTask mochaTestTask = project.getTasks().create("mochaTest", MochaTestTask.class);
        mochaTestTask.getDependsOn().add(npmInstallTask);
        mochaTestTask.setMainFiles("src/main/javascript");
        mochaTestTask.setTestFiles("src/test/javascript");
        mochaTestTask.setTestOutput("build/test-results/mochaTest/test-reports.xml");
        mochaTestTask.setMochaOptionsFile("mocha.opts");
        mochaTestTask.setGroup("verification");
        mochaTestTask.setDescription("Runs the Mocha (JavaScript) tests");

        project.getTasks().getByName("check").getDependsOn().add(mochaTestTask);
    }
}
