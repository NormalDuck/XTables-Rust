import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public final class XTablesClientManager {
    private final CompletableFuture<XTablesClient> future;
    private volatile XTablesClient client;

    private XTablesClientManager(CompletableFuture<XTablesClient> future) {
        this.future = future;
        future.thenAccept(created -> this.client = created);
    }

    public static XTablesClientManager getDefaultClientAsynchronously() {
        return getClientAsynchronously("127.0.0.1");
    }

    public static XTablesClientManager getClientAsynchronously(String host) {
        return getClientAsynchronously(host, defaultLibrary());
    }

    public static XTablesClientManager getClientAsynchronously(String host, Path library) {
        return new XTablesClientManager(
            CompletableFuture.supplyAsync(() -> new XTablesClient(library, host)));
    }

    public CompletableFuture<XTablesClient> getClientFuture() {
        return future;
    }

    public XTablesClient getOrNull() {
        return client;
    }

    public boolean isReady() {
        return client != null;
    }

    public void shutdown() {
        XTablesClient existing = client;
        if (existing != null) {
            existing.close();
        }
    }

    static Path defaultLibrary() {
        String override = System.getProperty("xtables.library");
        if (override != null) {
            return Path.of(override);
        }
        Path packaged = extractPackagedLibrary();
        if (packaged != null) {
            return packaged;
        }
        List<Path> candidates = List.of(
            Path.of("target/release/libxtables_ffi.so"),
            Path.of("../../target/release/libxtables_ffi.so"),
            Path.of("/usr/local/lib/libxtables_ffi.so"));
        for (Path candidate : candidates) {
            if (Files.isReadable(candidate)) {
                return candidate;
            }
        }
        throw new IllegalStateException(
            "could not locate " + libraryName() + " on disk or in the jar; "
                + "set -Dxtables.library=/path/to/" + libraryName());
    }

    static String platform() {
        String os = System.getProperty("os.name").toLowerCase();
        String raw = System.getProperty("os.arch").toLowerCase();
        String arch = raw.equals("amd64") || raw.equals("x86_64") ? "x86_64"
            : raw.contains("aarch") || raw.equals("arm64") ? "aarch64" : raw;
        if (os.contains("win")) {
            return "windows-" + arch;
        }
        if (os.contains("mac") || os.contains("darwin")) {
            return "macos-" + arch;
        }
        return "linux-" + arch;
    }

    static String libraryName() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            return "xtables_ffi.dll";
        }
        return os.contains("mac") || os.contains("darwin")
            ? "libxtables_ffi.dylib" : "libxtables_ffi.so";
    }

    private static Path extractPackagedLibrary() {
        String resource = "/natives/" + platform() + "/" + libraryName();
        byte[] library;
        try (InputStream stream = XTablesClientManager.class.getResourceAsStream(resource)) {
            if (stream == null) {
                return null;
            }
            library = stream.readAllBytes();
        } catch (Exception error) {
            return null;
        }

        String expected = readChecksum(resource + ".sha256");
        if (expected != null && !expected.equals(sha256(library))) {
            throw new IllegalStateException(
                "the packaged " + libraryName() + " does not match its recorded checksum");
        }

        try {
            Path directory = Files.createTempDirectory("xtables-native");
            directory.toFile().deleteOnExit();
            Path target = directory.resolve(libraryName());
            Files.copy(new java.io.ByteArrayInputStream(library), target,
                StandardCopyOption.REPLACE_EXISTING);
            target.toFile().deleteOnExit();
            return target;
        } catch (Exception error) {
            throw new IllegalStateException("could not unpack " + libraryName(), error);
        }
    }

    private static String readChecksum(String resource) {
        try (InputStream stream = XTablesClientManager.class.getResourceAsStream(resource)) {
            return stream == null ? null : new String(stream.readAllBytes()).trim();
        } catch (Exception error) {
            return null;
        }
    }

    private static String sha256(byte[] data) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(data));
        } catch (Exception error) {
            throw new IllegalStateException("SHA-256 is unavailable", error);
        }
    }
}
