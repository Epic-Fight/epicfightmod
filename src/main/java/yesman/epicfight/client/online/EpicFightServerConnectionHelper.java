package yesman.epicfight.client.online;

import net.minecraft.Util;
import yesman.epicfight.EpicFight;
import yesman.epicfight.api.client.model.Mesh;
import yesman.epicfight.api.utils.ParseUtil;
import yesman.epicfight.main.EpicFightMod;

import javax.net.ssl.SSLContext;
import java.io.*;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.text.MessageFormat;
import java.time.Duration;
import java.util.function.BiConsumer;

public class EpicFightServerConnectionHelper {
    public static HttpClient HTTP_CLIENT;
    private static final String LIB_FILE = "ServerCommunicationHelper";
    private static boolean SUPPORTED;

    public static boolean supported() {
        return SUPPORTED;
    }

    public static boolean init(String configPath) {
        EpicFight.LOGGER.info("Epic Fight web server connection helper: Initialize");

        SupportedOS os = SupportedOS.getOS();

        if (os == null) {
            EpicFight.LOGGER.error("Unsupported OS type {} for dynamic library", Util.getPlatform());
            return false;
        }

        boolean supported = false;

        try {
            SSLContext ssl = SSLContext.getInstance("TLSv1.3");
            ssl.init(null, null, null);

            HTTP_CLIENT = HttpClient.newBuilder().sslContext(ssl).connectTimeout(Duration.ofMillis(60000)).build();
        } catch (NoSuchAlgorithmException e) {
            EpicFight.LOGGER.warn("TLS 1.3 not found, we do not support TLS communication lower than 1.3");
            HTTP_CLIENT = null;
            SUPPORTED = false;
            return false;
        } catch (KeyManagementException e) {
            EpicFight.LOGGER.warn("Failed at initializing SSL context");
            HTTP_CLIENT = null;
            SUPPORTED = false;
            return false;
        } catch (Exception e) {
            EpicFight.LOGGER.warn("Failed at initializing " + e);
            HTTP_CLIENT = null;
            SUPPORTED = false;
            return false;
        }

        String libpath = MessageFormat.format("/assets/epicfight/nativelib/{0}/{1}{2}", os.telemetryName(), LIB_FILE, os.libExtension());
        InputStream inputstream = EpicFightMod.class.getResourceAsStream(libpath);

        if (inputstream != null) {
            File configNativeFile = new File(configPath + "/epicfight/native/" + LIB_FILE + os.libExtension());
            byte[] resourceBytes = null;
            boolean shouldCreate;

            if (configNativeFile.exists()) {
                try {
                    String configFileSHA256 = ParseUtil.getBytesSHA256Hash(new FileInputStream(configNativeFile).readAllBytes());
                    resourceBytes = inputstream.readAllBytes();
                    String resourceFileSHA256 = ParseUtil.getBytesSHA256Hash(resourceBytes);
                    shouldCreate = !configFileSHA256.equals(resourceFileSHA256);
                } catch (IOException e) {
                    e.printStackTrace();
                    shouldCreate = true;
                }
            } else {
                shouldCreate = true;
            }

            if (shouldCreate) {
                try {
                    EpicFight.LOGGER.info("Created temporary lib configNativeFile at: " + configNativeFile.getPath());
                    configNativeFile.delete();

                    if (!configNativeFile.getParentFile().isDirectory()) {
                        configNativeFile.getParentFile().mkdirs();
                    }

                    configNativeFile.createNewFile();
                    FileOutputStream fos = new FileOutputStream(configNativeFile);
                    if (resourceBytes == null) resourceBytes = inputstream.readAllBytes();
                    fos.write(resourceBytes, 0, resourceBytes.length);
                    fos.flush();
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    EpicFight.LOGGER.info("Can't read library configNativeFile: " + e.getMessage());
                }
            }

            boolean exceptionOccurred = false;

            try {
                System.load(configNativeFile.toString());
            } catch (UnsatisfiedLinkError e) {
                exceptionOccurred = true;
                EpicFight.LOGGER.warn("Failed at loading library configNativeFile");
            }

            supported = !exceptionOccurred;
        } else {
            EpicFight.LOGGER.info("Can't read library file: " + libpath);
        }

        SUPPORTED = supported;

        return supported;
    }

    public static native void autoLogin(String domain, String minecraftUuid, String accessToken, String refreshToken, String provider, BiConsumer<HttpResponse<String>, Exception> onResponse);

    public static native void signIn(String domain, String minecraftUuid, String authenticationCode, BiConsumer<HttpResponse<String>, Exception> onResponse);

    public static native void signOut(String domain, String minecraftUuid, String accessToken, String refreshToken, String provider, BiConsumer<HttpResponse<String>, Exception> onResponse);

    public static native void getAvailableCosmetics(String domain, String minecraftUuid, String accessToken, String refreshToken, String provider, BiConsumer<HttpResponse<String>, Exception> onResponse);

    public static native void saveConfiguration(String domain, String postBody, BiConsumer<HttpResponse<String>, Exception> onResponse);

    public static native void getPlayerSkinInfo(String domain, String minecraftUuid, BiConsumer<HttpResponse<String>, Exception> onResponse);

    public static native void loadRemoteMesh(String domain, String path, BiConsumer<Mesh, Exception> onResponse);

    private enum SupportedOS {
        LINUX("linux", ".so"),
        //SOLARIS("solaris", ".so"),
        WINDOWS("windows", ".dll"),
        OSX("osx", ".dylib"),
        MAC("mac", ".dylib")
        ;

        public static SupportedOS getOS() {
            try {
                return SupportedOS.valueOf(Util.getPlatform().name());
            } catch (IllegalArgumentException ex) {
                return null;
            }
        }

        private final String telemetryName;
        private final String libExtension;

        SupportedOS(String telemetryName, String libExtension) {
            this.telemetryName = telemetryName;
            this.libExtension = libExtension;
        }

        String telemetryName() {
            return this.telemetryName;
        }

        String libExtension() {
            return this.libExtension;
        }
    }
}
