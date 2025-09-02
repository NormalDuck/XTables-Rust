use std::{sync::Arc, thread::sleep};
use tokio::{task, time::Duration};
use xtables::{xtables_client::XTablesClient, xtables_server::XTablesServer};

//simple usage of using xtables server and xtables client
#[tokio::main]
async fn main() -> Result<(), Box<dyn std::error::Error>> {
    task::spawn_blocking(|| {
        let xtables_server = XTablesServer::new();
        xtables_server.start();
        xtables_server.stop();
        xtables_server.start();
    });

    let xtables_client = Arc::new(XTablesClient::new());
    xtables_client.start();

    sleep(Duration::from_secs(1));

    let unsubscribe = xtables_client.subscribe("hello", |data| {
        println!("this should show show up once {:?}", data);
    });

    let _ = xtables_client.subscribe("hello",|data| {
        println!("this should always show up {:?}", data);
    });

    unsubscribe(); //this makes subscribing not happen again

    // Prevent main from exiting
    loop {
        tokio::time::sleep(Duration::from_secs(1)).await;
        xtables_client.send_string("hello", "heheha");
        tokio::time::sleep(Duration::from_secs(1)).await;
        let value = xtables_client.get("hello");
        println!("got value {:?}", value);
    }
}
