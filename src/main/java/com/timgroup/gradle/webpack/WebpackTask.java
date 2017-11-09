package com.timgroup.gradle.webpack;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.inject.Inject;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.ConfigurableFileTree;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;
import org.gradle.process.internal.ExecAction;
import org.gradle.process.internal.ExecActionFactory;

public class WebpackTask extends DefaultTask {
    private File sources;
    private File configFile;
    private File output;
    private List<String> options;
    private boolean generateManifest = true;
    private boolean gzipResources = true;

    @Inject
    protected ExecActionFactory getExecActionFactory() {
        throw new UnsupportedOperationException();
    }

    @InputDirectory
    public File getSources() {
        return sources;
    }

    public void setSources(Object sources) {
        this.sources = getProject().file(sources);
    }

    @InputFile
    public File getConfigFile() {
        return configFile;
    }

    public void setConfigFile(Object configFile) {
        this.configFile = getProject().file(configFile);
    }

    @Input
    public List<String> getOptions() {
        return options;
    }

    public void setOptions(String... options) {
        this.options = Arrays.asList(options);
    }

    @OutputDirectory
    public File getOutput() {
        return output;
    }

    public void setOutput(Object output) {
        this.output = getProject().file(output);
    }

    public boolean getGenerateManifest() {
        return generateManifest;
    }

    public void setGenerateManifest(boolean generateManifest) {
        this.generateManifest = generateManifest;
    }

    public boolean isGzipResources() {
        return gzipResources;
    }

    public void setGzipResources(boolean gzipResources) {
        this.gzipResources = gzipResources;
    }

    @TaskAction
    public void runWebpack() {
        ExecAction execAction = getExecActionFactory().newExecAction();
        execAction.setExecutable("node_modules/.bin/webpack");
        List<String> args = new ArrayList<>();
        args.addAll(options);
        args.addAll(Arrays.asList("--config", configFile.toString()));
        execAction.setArgs(args);
        execAction.environment("NODE_ENV", "production");
        execAction.execute();
        if (gzipResources)
            gzipResources();
        if (generateManifest)
            writeResourceManifest();
    }

    private void gzipResources() {
        ConfigurableFileTree fileTree = getProject().fileTree(output);
        fileTree.setIncludes(Arrays.asList("**/*.html", "**/*.js", "**/*.map"));
        fileTree.forEach(file -> {
            getProject().getLogger().info("Gzipping " + file);
            Path inputPath = file.toPath();
            Path outputPath = inputPath.resolveSibling(inputPath.getFileName().toString() + ".gz");

            ExecAction execAction = getExecActionFactory().newExecAction();
            execAction.setExecutable("gzip");
            execAction.setArgs(Collections.singletonList("-9c"));
            try {
                execAction.setStandardInput(Files.newInputStream(inputPath));
                execAction.setStandardOutput(Files.newOutputStream(outputPath));
            } catch (IOException e) {
                throw new RuntimeException("Unable to compress " + inputPath, e);
            }
            execAction.execute();
        });
    }

    private void writeResourceManifest() {
        MessageDigest sha1;
        try {
            sha1 = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Unable to get SHA-1 MessageDigest", e);
        }
        StringBuilder manifest = new StringBuilder();
        ConfigurableFileTree fileTree = getProject().fileTree(output);
        fileTree.setExcludes(Collections.singleton(".MANIFEST"));
        fileTree.forEach(file -> {
            String relativeName = file.toString().substring(output.toString().length() + 1);
            try (InputStream input = Files.newInputStream(file.toPath())) {
                byte[] buf = new byte[8192];
                int got;
                while ((got = input.read(buf)) > 0) {
                    sha1.update(buf, 0, got);
                }
            } catch (IOException e) {
                throw new RuntimeException("Unable to read file to digest: " + file, e);
            }
            String digest = hexify(sha1.digest());
            BasicFileAttributes fileAttributes = null;
            try {
                fileAttributes = Files.getFileAttributeView(file.toPath(), BasicFileAttributeView.class).readAttributes();
            } catch (IOException e) {
                throw new RuntimeException("Unable to read attributes of " + file.toPath());
            }
            FileTime lastModifiedTime = fileAttributes.lastModifiedTime();
            long fileSize = fileAttributes.size();
            manifest.append(String.format("%s %s %s %d\n", digest, relativeName, fileSize, lastModifiedTime.toInstant().toEpochMilli()));
        });
        Path manifestPath = output.toPath().resolve(".MANIFEST");
        getProject().getLogger().info("Writing " + manifestPath);
        try (OutputStream output = Files.newOutputStream(manifestPath)) {
            output.write(manifest.toString().getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new RuntimeException("Unable to write manifest file: " + manifestPath, e);
        }
    }

    private static String hexify(byte[] input) {
        String hexdigits = "0123456789abcdef";
        char[] output = new char[input.length * 2];
        for (int i = 0; i < input.length; i++) {
            int n = (int) input[i];
            output[2 * i] = hexdigits.charAt((n & 0xf0) >> 4);
            output[2 * i + 1] = hexdigits.charAt(n & 0x0f);
        }
        return new String(output);
    }
}
