package com.craftmanship.tfm.idls;

import java.io.IOException;

import com.craftmanship.tfm.idls.models.Item;
import com.craftmanship.tfm.idls.stubs.ItemsPersistenceStub;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import v1.ItemPersistence.CreateItemResponse;
import v1.ItemPersistence.GrpcItem;
import v1.ItemPersistenceServiceGrpc.ItemPersistenceServiceImplBase;

public class ItemPersistenceExampleServer {
  private static final Logger logger = LoggerFactory.getLogger(ItemPersistenceExampleServer.class);

  private Server server;

  private void start() throws IOException {
    /* The port on which the server should run */
    int port = 50051;
    server = ServerBuilder.forPort(port).addService(new ItemPersistenceServiceImpl()).build().start();
    logger.info("Server started, listening on " + port);
    Runtime.getRuntime().addShutdownHook(new Thread() {
      @Override
      public void run() {
        // Use stderr here since the logger may have been reset by its JVM shutdown
        // hook.
        System.err.println("*** shutting down gRPC server since JVM is shutting down");
        ItemPersistenceExampleServer.this.stop();
        System.err.println("*** server shut down");
      }
    });
  }

  private void stop() {
    if (server != null) {
      server.shutdown();
    }
  }

  /**
   * Await termination on the main thread since the grpc library uses daemon
   * threads.
   */
  private void blockUntilShutdown() throws InterruptedException {
    if (server != null) {
      server.awaitTermination();
    }
  }

  /**
   * Main launches the server from the command line.
   */
  public static void main(String[] args) throws IOException, InterruptedException {
    final ItemPersistenceExampleServer server = new ItemPersistenceExampleServer();
    server.start();
    server.blockUntilShutdown();
  }

  static class ItemPersistenceServiceImpl extends ItemPersistenceServiceImplBase {

    private static ItemsPersistenceStub itemsPersistence = new ItemsPersistenceStub();

    @Override
    public void create(v1.ItemPersistence.CreateItemRequest request,
        io.grpc.stub.StreamObserver<v1.ItemPersistence.CreateItemResponse> responseObserver) {
      logger.info("CREATE RPC CALLED");
      GrpcItem grpcItem = request.getItem();
      Item item = new Item.Builder().withDescription(grpcItem.getDescription()).build();
      Item createdItem = itemsPersistence.create(item);

      GrpcItem grpcItemResponse = GrpcItem.newBuilder()
        .setId(createdItem.getId())
        .setDescription(createdItem.getDescription())
        .build();
      CreateItemResponse response = CreateItemResponse.newBuilder()
        .setItem(grpcItemResponse)
        .build();

      responseObserver.onNext(response);
      responseObserver.onCompleted();
    }

    @Override
    public void list(v1.ItemPersistence.ListItemRequest request,
        io.grpc.stub.StreamObserver<v1.ItemPersistence.ListItemResponse> responseObserver) {
      logger.info("LIST RPC CALLED");
    }

    @Override
    public void get(v1.ItemPersistence.GetItemRequest request,
        io.grpc.stub.StreamObserver<v1.ItemPersistence.GetItemResponse> responseObserver) {
      logger.info("GET RPC CALLED");
    }

    @Override
    public void update(v1.ItemPersistence.UpdateItemRequest request,
        io.grpc.stub.StreamObserver<v1.ItemPersistence.UpdateItemResponse> responseObserver) {
      logger.info("UPDATE RPC CALLED");
    }

    @Override
    public void delete(v1.ItemPersistence.DeleteItemRequest request,
        io.grpc.stub.StreamObserver<v1.ItemPersistence.DeleteItemResponse> responseObserver) {
      logger.info("DELETE RPC CALLED");
    }
  }
}