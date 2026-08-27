use std::sync::OnceLock;

use clap::Parser;

#[derive(Parser, Debug)]
#[command(version, about, long_about = None)]
/// Command-line arguments for the server binary.
pub struct XTablesArgs {
    /// Enable logging for the XTables server
    #[arg(short, long, default_value_t = false)]
    pub log: bool,
}

/// The parsed arguments, set once at startup and read from anywhere.
pub static CONFIG: OnceLock<XTablesArgs> = OnceLock::new();
