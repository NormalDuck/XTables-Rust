# XTables RUST
[![CI](https://github.com/NormalDuck/XTables-Rust/actions/workflows/ci-rust.yml/badge.svg)](https://github.com/NormalDuck/XTables-Rust/actions/workflows/ci-rust.yml)[![Release](https://github.com/NormalDuck/XTables-Rust/actions/workflows/release.yml/badge.svg)](https://github.com/NormalDuck/XTables-Rust/actions/workflows/release.yml)


Make sure you have installed rust and use a rust ide
To start the project, change directory to xtables. Then run 
```rs
cargo run
```
This should give you an example of the public api of xtables server. 

This project uses protobufs to compress bandwith and zmq servers. 

Note: .get method from xtables client uses req rep zmq method, I am unsure how this behaves and there might be collison when mutliple clients requests the server a req method and the server responds to the wrong client?

It is still unclear how this can replace the original java implementation of [Xtables](https://github.com/Kobeeeef/XTABLES), but rust is generally considered more memory safe & friendly and faster since it is a compiled programming language with no garbage collectors.

## Tools
Make sure you have nodejs, rust, python, java, protoc installed.

## Example
```rs
use xtables_client::xtables_client::XTablesClient;

#[tokio::main]
async fn main() -> Result<(), Box<dyn std::error::Error>> {
    println!("Starting xtables client...");
    let client = XTablesClient::new();

    let _ = client.subscribe_to_logs(|logs| {
        println!("{}", logs);
    });

    let _ = client.subscribe("test", |data| {
        println!("Received data on 'test': {:?}", data);
    });
    client.start();

    client.send_bool("test", true);

    loop {
        tokio::time::sleep(tokio::time::Duration::from_secs(5)).await;
    }
}
```

## Roadmap
- [x] Graceful shutdown
- [ ] Unit Testing
- [x] Custom Logging
~~- [ ] Client Registry?~~
- [x] Server Logger Interface
- [ ] Further Benchmarking