use clap::Parser;
use log::info;
use xtables_server::{
    utils::{
        args::{CONFIG, XTablesArgs},
        log::init_logger,
    },
    xtables_server::XTablesServer,
};

fn main() {
    CONFIG
        .set(XTablesArgs::parse())
        .expect("Failed to set configuration");

    init_logger();

    let xtables_server = match XTablesServer::try_new() {
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

    std::thread::park();
}
