/*
 * Copyright 2017 The ZRPC Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.zhuyiren.loader;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import jdk.nashorn.internal.runtime.regexp.joni.constants.Arguments;

import java.io.*;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

/**
 * @author zhuyiren
 * @date 2017/9/10
 */
public class BootStrap {

    private static final Splitter SPLITTER = Splitter.on(": ").trimResults();

    public static void main(String[] args) throws Exception {
        Preconditions.checkArgument(args != null && args.length >= 1, "The size of argument array must be equal or lager than 1");
        ProtectionDomain protectionDomain = BootStrap.class.getProtectionDomain();
        CodeSource codeSource = protectionDomain.getCodeSource();
        URL rootUrl = codeSource.getLocation().toURI().toURL();
        String rootFileName = rootUrl.getFile();
        JarFile rootJarFile = new JarFile(rootFileName);
        Map<String, String> attributes = getAttributes(rootJarFile);
        String libDirectory = attributes.get("Lib-Directory");
        List<URL> urls = extractJarFiles(rootUrl.getPath(), rootJarFile.entries(), libDirectory, args[0]);
        URLClassLoader targetClassLoader = new URLClassLoader(urls.toArray(new URL[]{}), getExtendClassLoader());
        String subClass=attributes.get("Sub-Class");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(subClass),"The Sub-Class attribute is not find in Manifest");
        Class<?> targetClass = targetClassLoader.loadClass(subClass);
        String[] arguments=new String[args.length-1];
        System.arraycopy(args,1,arguments,0,arguments.length);
        invokeMain(targetClass,arguments);
    }

    private static Map<String, String> getAttributes(JarFile jarFile) throws Exception {

        Manifest manifest = jarFile.getManifest();
        ByteArrayOutputStream temp = new ByteArrayOutputStream();
        manifest.write(temp);
        BufferedReader reader = new BufferedReader(new StringReader(new String(temp.toByteArray(), StandardCharsets.UTF_8)));
        String line;
        Map<String, String> result = new HashMap<>();
        while ((line = reader.readLine()) != null) {
            line = line.trim();
            if (Strings.isNullOrEmpty(line)) {
                continue;
            }
            List<String> list = SPLITTER.splitToList(line);
            if (list.size() != 2) {
                continue;
            }
            result.put(list.get(0).trim(), list.get(1).trim());
        }
        reader.close();
        return result;
    }


    private static List<URL> extractJarFiles(String rootJarFileName, Enumeration<JarEntry> entries, String libDirectory, String tmpDirectory) throws IOException {

        if (rootJarFileName.endsWith(".jar")) {
            rootJarFileName += "!/";
        }
        rootJarFileName = "jar:file:" + rootJarFileName;
        if (libDirectory.charAt(0) == '/') {
            libDirectory = libDirectory.substring(1);
        }
        if (libDirectory.charAt(libDirectory.length() - 1) != '/') {
            libDirectory += '/';
        }

        if (tmpDirectory.charAt(tmpDirectory.length() - 1) != '/') {
            tmpDirectory += '/';
        }

        if (!Files.exists(Paths.get(tmpDirectory))) {
            Files.createDirectories(Paths.get(tmpDirectory));
        }

        System.out.println(rootJarFileName);
        System.out.println(libDirectory);
        System.out.println(tmpDirectory);
        List<URL> urls = new ArrayList<>();
        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            String childFileName = entry.getName();
            if (childFileName.endsWith(".jar") && childFileName.startsWith(libDirectory)) {
                InputStream inputStream = new URL(rootJarFileName + childFileName).openConnection().getInputStream();
                Path path = Paths.get(tmpDirectory + UUID.randomUUID() + ".jar");
                Files.copy(inputStream, path);
                inputStream.close();
                urls.add(path.toUri().toURL());
            }
        }
        return urls;
    }

    private static ClassLoader getExtendClassLoader() {
        ClassLoader current=BootStrap.class.getClassLoader();
        while (current.getParent()!=null){
            current=current.getParent();
        }
        return current;
    }

    private static void invokeMain(Class<?> clz,String[] arguments) throws Exception{
        Method method = clz.getDeclaredMethod("main",String[].class);
        method.setAccessible(true);
        method.invoke(null,new Object[]{arguments});
    }
}