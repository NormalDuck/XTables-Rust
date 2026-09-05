use clap::Parser;
use log::info;
use xtables_server::{
    server::XTablesServer,
    utils::{
        args::{CONFIG, XTablesArgs},
        log::init_logger,
    },
};

fn main() {
    CONFIG
        .set(XTablesArgs::parse())
        .expect("Failed to set configuration");

    init_logger();

    let config = CONFIG.get().expect("configuration was just set");
    eprintln!(
        "xtables: WebSocket {}, telemetry UDP {}",
        config.rep_port, config.telemetry_port
    );

    let xtables_server = match XTablesServer::try_with_ports_and_telemetry(
        config.pub_port,
        config.pull_port,
        config.rep_port,
        config.telemetry_port,
    ) {
        Ok(server) => server,
        Err(error) => {
            let mut message = error.to_string();
            let mut cause = std::error::Error::source(&error);
            while let Some(source) = cause {
                message.push_str(&format!(": {source}"));
                cause = source.source();
            }
            eprintln!("xtables: {message}");
            std::process::exit(1);
        }
    };
    xtables_server.start();

    info!("XTables server started successfully.");
    eprintln!("xtables: ready");

    loop {
        std::thread::park();
    }
}
