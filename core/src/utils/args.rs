use std::sync::OnceLock;

use clap::{Parser, command};

// XTables server configuration
#[derive(Parser, Debug)]
#[command(version, about, long_about = None)]
pub struct XTablesArgs {
    /// Enable logging for the XTables server
    #[arg(short, long, default_value_t = false)]
    pub log: bool,
}

pub static CONFIG: OnceLock<XTablesArgs> = OnceLock::new();
