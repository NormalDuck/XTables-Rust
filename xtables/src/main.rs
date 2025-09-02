use xtables_server::xtables_server::XTablesServer;

//simple usage of using xtables server and xtables client
#[tokio::main]
async fn main() -> Result<(), Box<dyn std::error::Error>> {
    let xtables_server = XTablesServer::new();
    xtables_server.start();
    println!("XTables server started and running...");

    // Prevent main from exiting
    loop {}
}
