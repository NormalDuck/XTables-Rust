use xtables_server::xtables_server::XTablesServer;

//simple usage of using xtables server and xtables client
#[tokio::main]
async fn main() -> Result<(), Box<dyn std::error::Error>> {
    let xtables_server = XTablesServer::new();
    xtables_server.start();
    println!("XTables server started and running...");

    // Prevent main from exiting
    loop {
        // Here you can add logic to interact with the server or handle other tasks
        // For demonstration, we will just sleep for a while
        tokio::time::sleep(tokio::time::Duration::from_secs(5)).await;
    }
}
