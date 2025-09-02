# XTables RUST
Make sure you have installed rust and use a rust ide
To start the project, change directory to xtables. Then run 
```rs
cargo run
```
This should give you an example of the public api of xtables server. 

This project uses protobufs to compress bandwith and zmq servers. 

Note: .get method from xtables client uses req rep zmq method, I am unsure how this behaves and there might be collison when mutliple clients requests the server a req method and the server responds to the wrong client?

## Roadmap
- [ ] Graceful shutdown
- [ ] Unit Testing
- [ ] Custom Logging
- [ ] Client Registry?
- [ ] Server Logger Interface
- [ ] Further Benchmarking