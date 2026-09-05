//! A Rust client for [XTABLES](https://github.com/Kobeeeef/XTABLES).
//!
//! [`XTablesClient`](client::XTablesClient) speaks the same method names
//! as the original: every public `put`/`get` on its `Requests` class exists here,
//! across scalars, the seven list types, poses, coordinates and bezier curves.
//! `send_float` is an addition, as are the control-plane calls and
//! [`compare_and_set`](client::XTablesClient::compare_and_set).
//!
//! Values move over two transports. Publishes and reads go over ZeroMQ, which is
//! reliable and framed; [`publish_telemetry`](client::XTablesClient::publish_telemetry)
//! goes over UDP, which is roughly 3.6x faster and makes no delivery guarantee.
//!
//! ```no_run
//! use xtables_client::client::XTablesClient;
//!
//! let client = XTablesClient::new();
//! let _unsubscribe = client.subscribe("test", |value| println!("{value:?}"));
//! client.start();
//! client.send_bool("test", true);
//! ```
//!
//! # Reserved names
//!
//! Channels beginning with `XTABLES_INTERNAL` are reserved for the server's own
//! traffic and may conflict with it.

#![warn(missing_docs)]

mod ports;

/// The client itself, its configuration, and the value types it carries.
pub mod client;

pub use client::{XTablesClient, XTablesConfig};
