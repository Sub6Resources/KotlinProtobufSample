# ProtobufSample
A sample Kotlin Android client with gRPC and Protobuf!

Before working, you will either need to `make` the protobuf module, or run the project to generate the Java and gRPC protobuf source code.

After that, it's as simple as adding your own `.proto` files to the `proto` module and building your app in your `app` module!

The sample uses the grpc sample java server. To set it up see https://grpc.io/docs/quickstart/android.html 

To access a local server from the AVD, use IP 10.0.2.2
