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

    let xtables_server = XTablesServer::new();
    xtables_server.start();

    info!("XTables server started successfully.");

    std::thread::park();
}
