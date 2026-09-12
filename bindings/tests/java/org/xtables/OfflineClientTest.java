package org.xtables;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.function.Function;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

final class OfflineClientTest {
    private static Updater discard() {
        return update -> { };
    }

    private static XTablesClient offline() {
        return XTablesClient.withPorts(
            "127.0.0.1", (short) 26882, (short) 26883, (short) 26881, (short) 26884, 150L, 500);
    }

    static List<Arguments> readers() {
        return List.of(
            Arguments.of("getString", (Function<XTablesClient, Object>) c -> c.getString("absent")),
            Arguments.of("getInteger", (Function<XTablesClient, Object>) c -> c.getInteger("absent")),
            Arguments.of("getLong", (Function<XTablesClient, Object>) c -> c.getLong("absent")),
            Arguments.of("getDouble", (Function<XTablesClient, Object>) c -> c.getDouble("absent")),
            Arguments.of("getFloat", (Function<XTablesClient, Object>) c -> c.getFloat("absent")),
            Arguments.of("getBoolean", (Function<XTablesClient, Object>) c -> c.getBoolean("absent")),
            Arguments.of("getBytes", (Function<XTablesClient, Object>) c -> c.getBytes("absent")),
            Arguments.of("getStringList", (Function<XTablesClient, Object>) c -> c.getStringList("absent")),
            Arguments.of("getBytesList", (Function<XTablesClient, Object>) c -> c.getBytesList("absent")),
            Arguments.of("getDoubleList", (Function<XTablesClient, Object>) c -> c.getDoubleList("absent")),
            Arguments.of("getFloatList", (Function<XTablesClient, Object>) c -> c.getFloatList("absent")),
            Arguments.of("getIntegerList", (Function<XTablesClient, Object>) c -> c.getIntegerList("absent")),
            Arguments.of("getLongList", (Function<XTablesClient, Object>) c -> c.getLongList("absent")),
            Arguments.of("getBooleanList", (Function<XTablesClient, Object>) c -> c.getBooleanList("absent")),
            Arguments.of("getCoordinates", (Function<XTablesClient, Object>) c -> c.getCoordinates("absent")),
            Arguments.of("getPose2d", (Function<XTablesClient, Object>) c -> c.getPose2d("absent")),
            Arguments.of("getPose3d", (Function<XTablesClient, Object>) c -> c.getPose3d("absent")),
            Arguments.of("getBezierCurve", (Function<XTablesClient, Object>) c -> c.getBezierCurve("absent")),
            Arguments.of("getUnknownBytes", (Function<XTablesClient, Object>) c -> c.getUnknownBytes("absent")),
            Arguments.of("getPing", (Function<XTablesClient, Object>) c -> c.getPing()),
            Arguments.of("getServerStatistics",
                (Function<XTablesClient, Object>) c -> c.getServerStatistics()));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("readers")
    void a_read_reports_absence_rather_than_inventing_a_value(
        String name, Function<XTablesClient, Object> read) {
        try (XTablesClient client = offline()) {
            assertNull(read.apply(client), name + " invented a value with no server");
        }
    }

    @Test
    void publishing_into_the_void_neither_blocks_nor_throws() {
        try (XTablesClient client = offline()) {
            assertDoesNotThrow(() -> {
                client.putDouble("pose", 1.5);
                client.putString("mode", "auto");
                client.putBytes("frame", new byte[] {1, 2, 3});
                client.putDoubleList("wheels", new double[] {1.0, 2.0});
                client.publishTelemetry("fast", new byte[] {4, 5});
            });
        }
    }

    @Test
    void a_listing_is_empty_rather_than_null_when_the_server_is_absent() {
        try (XTablesClient client = offline()) {
            assertTrue(client.getTables("").isEmpty());
            assertEquals("{}", client.getRawJson(""));
            assertEquals(0, client.delete("absent"));
            assertEquals(0, client.deleteAll());
        }
    }

    @Test
    void a_compare_and_set_fails_rather_than_claiming_it_swapped() {
        try (XTablesClient client = offline()) {
            assertFalse(client.compareAndSetAbsentString("lock", "agent-a"));
            assertFalse(client.compareAndSetDouble("counter", 1.0, 2.0));
            assertFalse(client.compareAndSetLong("counter", 1L, 2L));
            assertFalse(client.compareAndSetBoolean("flag", false, true));
        }
    }

    @Test
    void logging_reports_healthy_before_it_is_started() {
        try (XTablesClient client = offline()) {
            assertTrue(client.loggingHealthy());
            assertEquals(0L, client.droppedLogRecords());
        }
    }

    @Test
    void a_typed_put_rejects_bytes_that_are_not_that_type() {
        try (XTablesClient client = offline()) {
            assertFalse(client.putTypedBytes("pose", 2, new byte[] {1, 2, 3}));
            assertTrue(client.putTypedBytes("pose", 9999, new byte[] {1, 2, 3}));
        }
    }

    @Test
    void cancelling_a_subscription_stops_it_rather_than_leaking_it() {
        try (XTablesClient client = offline()) {
            assertTrue(client.subscribe("pose", discard()), "the first subscribe should take");
            assertFalse(client.subscribe("pose", discard()), "a second subscribe should report the first");
            assertTrue(client.unsubscribe("pose"), "the cancel handle should have been kept");
            assertFalse(client.unsubscribe("pose"), "cancelling twice should report the first");
            assertTrue(client.subscribe("pose", discard()));
        }
    }

    @Test
    void cancelling_a_log_subscription_frees_it_to_be_taken_again() {
        try (XTablesClient client = offline()) {
            assertTrue(client.subscribeToLogs(discard()));
            assertFalse(client.subscribeToLogs(discard()));
            assertTrue(client.unsubscribeFromLogs());
            assertFalse(client.unsubscribeFromLogs());
        }
    }

    @Test
    void the_bundled_native_is_unpacked_rather_than_searched_for_on_a_library_path() {
        try (XTablesClient client = offline()) {
            assertNotNull(
                System.getProperty("uniffi.component.xtables.libraryOverride"),
                "the client should have unpacked the native it bundles");
        }
    }

    @Test
    void a_pose_reads_back_as_a_wpilib_type_rather_than_ours() {
        try (XTablesClient client = offline()) {
            assertNull(client.getPose2d("absent"));
            assertDoesNotThrow(() -> client.putPose2d(
                "pose", new org.wpilib.math.geometry.Pose2d(
                    1.5, -2.0, new org.wpilib.math.geometry.Rotation2d(0.25))));
        }
    }
}
