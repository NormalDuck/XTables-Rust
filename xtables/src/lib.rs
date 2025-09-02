pub mod utils {
    pub mod ports;
    pub mod ring_buffer;
}

pub mod xtables_client;
pub mod xtables_server;

pub mod xtables {
    include!(concat!(env!("OUT_DIR"), "/xtables.rs"));
}
