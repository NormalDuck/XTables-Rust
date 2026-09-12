package xtables;

import org.kobe.xbot.JClient.XTablesClient;
import org.kobe.xbot.Utilities.Entities.XTableProto.XTableMessage.XTableUpdate;

/**
 * The XTABLES server driven through XTablesClient, the way a robot's own code
 * reaches it. Publishing here does not touch the wire: XTablesClient.publish
 * serializes the message into a CircularBuffer that a ConcurrentPushHandler
 * daemon drains and sends, so the difference between this probe and
 * {@link XtablesSocketProbe} is that queue.
 */
public final class XtablesClientProbe implements Probe {
    public static final String CHANNEL = "bench";

    /**
     * Publishes {@code count} messages at {@code rateHz}, stamping each with the
     * time it was due. The pacer is built only after the client has had time to
     * connect; built first, its early slots would spend the connect catching up
     * and carry that wait as latency.
     */
    @Override
    public void publish(String host, int payload, long rateHz, long count) throws Exception {
        int size = Math.max(payload, Harness.HEADER_LEN);
        XTablesClient client = new XTablesClient(host);
        byte[] buffer = new byte[size];

        Thread.sleep(1500);
        Harness.Pacer pacer = new Harness.Pacer(rateHz);

        for (long seq = 0; seq < count; seq++) {
            long due = pacer.await();
            Harness.writeLong(buffer, 0, seq);
            Harness.writeLong(buffer, 8, due);
            client.publish(CHANNEL, buffer);
        }
        System.out.printf("sent %d messages of %d B%n", count, size);
        client.shutdown();
    }

    @Override
    public void subscribe(String host, int payload, int samples) throws Exception {
        int size = Math.max(payload, Harness.HEADER_LEN);
        Harness.Samples collected = new Harness.Samples(samples);

        XTablesClient client = new XTablesClient(host);
        client.subscribe(CHANNEL, (XTableUpdate update) -> {
            long received = Harness.nowNanos();
            byte[] value = update.getValue().toByteArray();
            if (value.length >= Harness.HEADER_LEN) {
                collected.record(Harness.readLong(value, 0), Harness.readLong(value, 8), received);
            }
        });

        System.out.printf("subscribed to '%s' on %s, waiting for %d samples...%n",
            CHANNEL, host, samples);
        long deadline = System.currentTimeMillis() + Harness.deadlineMillis();
        while (collected.size() < samples && System.currentTimeMillis() < deadline) {
            Thread.onSpinWait();
        }
        collected.emit();
        System.err.printf("version      %s, payload %d B%n",
            Harness.version("BENCH_XTABLES_VERSION"), size);
        client.shutdown();
    }


}
