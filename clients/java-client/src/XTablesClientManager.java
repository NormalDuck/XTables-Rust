import java.nio.file.Files;
import java.nio.file.Path;
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
            "could not locate libxtables_ffi.so; set -Dxtables.library=/path/to/libxtables_ffi.so");
    }
}
