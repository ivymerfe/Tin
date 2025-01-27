package me.ivy.tin;

import com.google.gson.*;
import net.fabricmc.loader.impl.launch.FabricLauncherBase;
import sun.misc.Unsafe;

import java.io.*;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Collection;
import java.util.WeakHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.jar.JarFile;

public class Tin {
    private static Tin Instance;

    private ScheduledExecutorService executorService;
    private final RenderHack renderHack;
    private final CombatHack combatHack;
    private Unsafe unsafe;
    private Path jarFilePath;
    private byte[] jarFileBytes;
    private Path configPath;
    private boolean enabled = true;
    public Config config;

    private Tin() {
        config = new Config();
        combatHack = new CombatHack(this);
        renderHack = new RenderHack(this);
        // Movement hacks: bridging + mlg
        // Trajectories
        // ChestESP, Freecam, Xray, Tracers
    }

    public static Tin getInstance() {
        if (Instance == null) {
            Instance = new Tin();
        }
        return Instance;
    }

    public static CombatHack getHack() {
        return getInstance().combatHack;
    }

    public static RenderHack getRenderHack() {
        return getInstance().renderHack;
    }

    public void init() {
        try {
            URI uri = this.getClass().getProtectionDomain().getCodeSource().getLocation().toURI();
            Path me = Paths.get(uri);
            configPath = me.getParent().resolve("tinconfig.json");
            if (me.toString().endsWith("jar")) {
                jarFilePath = me;
                jarFileBytes = Files.readAllBytes(me);
            }
        } catch (URISyntaxException | IOException e) {
            throw new RuntimeException(e);
        }
        reloadConfig();
        saveConfig();
        executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.schedule(this::deleteMyself, 10, TimeUnit.SECONDS);
        executorService.scheduleAtFixedRate(this::reloadConfig, 1, 1, TimeUnit.SECONDS);
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        if (enabled) {
            saveConfig();
        } else {
            try {
                Files.deleteIfExists(configPath);
            } catch (IOException ignored) {

            }
        }
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    private void reloadConfig() {
        if (isEnabled() && configPath != null && Files.exists(configPath)) {
            try {
                config = new Gson().fromJson(Files.readString(configPath), Config.class);
            } catch (Throwable e) {
                // e.printStackTrace();
            }
        }
    }

    private void saveConfig() {
        try {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            Files.writeString(configPath, gson.toJson(config));
        } catch (IOException ignored) {

        }
    }

    public void onExit() {
        executorService.close();
        saveConfig();
        restoreMyself();
    }

    private void deleteMyself() {
        try {
            URI uri = this.getClass().getProtectionDomain().getCodeSource().getLocation().toURI();
            Path me = Paths.get(uri);
            if (me.toString().endsWith("jar")) {
                if(!closeJarInGlobalCache(uri)) {
                    throw new RuntimeException("Failed to delete tin.jar: failed to close jar in global cache");
                }
                if (!closeJarInKnotClassloader(me.toString())) {
                    throw new RuntimeException("Failed to delete tin.jar: failed to close jar in classloader");
                }
                try {
                    Files.deleteIfExists(me);
                } catch (IOException e) {
                    e.printStackTrace();
                    throw new RuntimeException("Failed to delete tin.jar");
                }
            }
        } catch (URISyntaxException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to delete tin.jar.");
        }
    }

    private void restoreMyself() {
        if (jarFilePath == null || jarFileBytes == null) {
            return;
        }
        try {
            Files.write(jarFilePath, jarFileBytes, StandardOpenOption.CREATE);
        } catch (IOException e) {
        }
    }

    // https://bugs.openjdk.org/browse/JDK-8239054
    private boolean closeJarInGlobalCache(URI fileUri) {
        try {
            URL jarUrl = URI.create("jar:"+fileUri+"!/").toURL();
            ((JarURLConnection)jarUrl.openConnection()).getJarFile().close();
            return true;
        } catch (IOException e) {
        }
        return false;
    }

    private boolean closeJarInKnotClassloader(String jarPath) {
        if (unsafe == null) {
            try {
                Field f = Unsafe.class.getDeclaredField("theUnsafe");
                f.setAccessible(true);
                unsafe = (Unsafe) f.get(null);
            } catch (IllegalAccessException | NoSuchFieldException exc) {
                return false;
            }
        }
        // Close jar in global cache
        ClassLoader classLoader = FabricLauncherBase.getLauncher().getTargetClassLoader();
        try {
            Class<?> knotClassLoaderCls = Class.forName("net.fabricmc.loader.impl.launch.knot.KnotClassLoader");
            if (knotClassLoaderCls.isInstance(classLoader)) {
                Field urlClassLoaderField = knotClassLoaderCls.getDeclaredField("urlLoader");
                urlClassLoaderField.setAccessible(true);
                URLClassLoader urlClassLoader = (URLClassLoader) urlClassLoaderField.get(classLoader);
                return closeJarInUrlClassLoader(urlClassLoader, jarPath);
            }
        } catch (ClassNotFoundException | NoSuchFieldException | IllegalAccessException e) {}
        return false;
    }

    // https://github.com/toolfactory/jvm-driver/blob/main/java/src/main/java/io/github/toolfactory/jvm/function/catalog/SetAccessibleFunction.java
    // https://stackoverflow.com/a/78487108
    private void setAccessible(AccessibleObject accessibleObject, boolean flag) {
        unsafe.putBoolean(accessibleObject, 12, flag);  // AccessibleObject.override offset = 12
    }

    private boolean closeJarInUrlClassLoader(URLClassLoader urlClassLoader, String jarPath) {
        try {
            Field closeablesField = URLClassLoader.class.getDeclaredField("closeables");
            setAccessible(closeablesField, true);
            WeakHashMap<Closeable,Void> closeables = (WeakHashMap<Closeable,Void>)closeablesField.get(urlClassLoader);
            for (Closeable closeable : closeables.keySet()) {
                if (closeable instanceof JarFile jarFile) {
                    if (jarFile.getName().equals(jarPath)) {
                        try {
                            jarFile.close();
                        } catch (IOException ignored) {}
                    }
                }
            }
            Field urlClassPathField = URLClassLoader.class.getDeclaredField("ucp");
            setAccessible(urlClassPathField, true);
            Object urlClassPath = urlClassPathField.get(urlClassLoader);
            Field loadersField = urlClassPath.getClass().getDeclaredField("loaders");
            setAccessible(loadersField, true);
            Collection<?> loaders = (Collection<?>)loadersField.get(urlClassPath);
            for (Object urlClassPathLoader : loaders.toArray()) {
                try {
                    Field jarField = urlClassPathLoader.getClass().getDeclaredField("jar");
                    setAccessible(jarField, true);
                    JarFile jarFile = (JarFile)jarField.get(urlClassPathLoader);
                    if (jarFile.getName().equals(jarPath)) {
                        ((Closeable)urlClassPathLoader).close();
                        loaders.remove(urlClassPathLoader);  // !!! Remove invalid loader
                        return true;
                    }
                } catch (Throwable t) {
                    // not a JAR loader so skip it
                }
            }
        } catch (NoSuchFieldException | IllegalAccessException ignored) {}
        return false;
    }
}
