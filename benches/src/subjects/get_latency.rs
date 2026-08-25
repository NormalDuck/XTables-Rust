use crate::harness::Recorder;
use std::time::{Duration, Instant};
use xtables_client::xtables_client::{XTablesClient, XTablesConfig};

pub fn run(host: &str, samples: u64) -> std::io::Result<()> {
    let client = XTablesClient::with_config(XTablesConfig {
        host: host.to_string(),
        request_timeout: Duration::from_millis(500),
        ..Default::default()
    });
    client.send_double("probe", 1.0);
    std::thread::sleep(Duration::from_millis(300));

    let mut recorder = Recorder::new();
    for seq in 0..samples {
        let started = Instant::now();
        let _ = client.get("probe");
        recorder.record_latency(seq, started.elapsed().as_nanos() as u64);
    }
    recorder.report("zmq-req-rep-get", 0);
    Ok(())
}
