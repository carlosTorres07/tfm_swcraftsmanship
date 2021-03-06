package com.craftsmanship.tfm.dal.grpc.services;

import com.craftsmanship.tfm.dal.grpc.server.EntityConversion;
import com.craftsmanship.tfm.dal.model.EntityItem;
import com.craftsmanship.tfm.dal.model.ItemDAO;
import com.craftsmanship.tfm.exceptions.ItemAlreadyExists;
import com.craftsmanship.tfm.exceptions.ItemDoesNotExist;
import com.craftsmanship.tfm.idls.v2.ItemPersistence.CreateItemRequest;
import com.craftsmanship.tfm.idls.v2.ItemPersistence.CreateItemResponse;
import com.craftsmanship.tfm.idls.v2.ItemPersistence.DeleteItemRequest;
import com.craftsmanship.tfm.idls.v2.ItemPersistence.DeleteItemResponse;
import com.craftsmanship.tfm.idls.v2.ItemPersistence.Empty;
import com.craftsmanship.tfm.idls.v2.ItemPersistence.GetItemRequest;
import com.craftsmanship.tfm.idls.v2.ItemPersistence.GetItemResponse;
import com.craftsmanship.tfm.idls.v2.ItemPersistence.GrpcItem;
import com.craftsmanship.tfm.idls.v2.ItemPersistence.ListItemResponse;
import com.craftsmanship.tfm.idls.v2.ItemPersistence.UpdateItemRequest;
import com.craftsmanship.tfm.idls.v2.ItemPersistence.UpdateItemResponse;
import com.craftsmanship.tfm.idls.v2.ItemPersistence.ListItemResponse.Builder;
import com.craftsmanship.tfm.idls.v2.ItemPersistenceServiceGrpc.ItemPersistenceServiceImplBase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.grpc.Status;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;

public class ItemPersistenceService extends ItemPersistenceServiceImplBase {
    private static final Logger LOGGER = LoggerFactory.getLogger(ItemPersistenceService.class);

    private ItemDAO itemPersistence;
    private EntityConversion conversionLogic;
    private final Counter itemGrpcRequestCounter;

    public ItemPersistenceService(ItemDAO itemsPersistence, EntityConversion conversionLogic, MeterRegistry registry) {
        this.itemPersistence = itemsPersistence;
        this.conversionLogic = conversionLogic;
        itemGrpcRequestCounter = Counter
            .builder("item_grpc_request")
            .description("Number of requests to Item gRPC service")
            .register(registry);
    }

    @Override
    public void create(CreateItemRequest request, io.grpc.stub.StreamObserver<CreateItemResponse> responseObserver) {
        LOGGER.info("CREATE RPC CALLED");
        itemGrpcRequestCounter.increment();

        GrpcItem grpcItem = request.getItem();
        EntityItem item = conversionLogic.getItemFromGrpcItem(grpcItem);

        try {
            EntityItem createdItem = itemPersistence.create(item);

            GrpcItem grpcItemResponse = conversionLogic.getGrpcItemFromItem(createdItem);
            CreateItemResponse response = CreateItemResponse.newBuilder().setItem(grpcItemResponse).build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (ItemAlreadyExists e) {
            responseObserver.onError(Status.ALREADY_EXISTS
                    .withDescription("Item with id " + item.getId() + " or name " + item.getName() + " already exists")
                    .asRuntimeException());
        }
    }

    @Override
    public void list(Empty request, io.grpc.stub.StreamObserver<ListItemResponse> responseObserver) {
        LOGGER.info("LIST RPC CALLED");
        itemGrpcRequestCounter.increment();

        Builder responseBuilder = ListItemResponse.newBuilder();
        for (EntityItem item : itemPersistence.list()) {
            GrpcItem grpcItem = conversionLogic.getGrpcItemFromItem(item);
            responseBuilder.addItem(grpcItem);
        }
        ListItemResponse response = responseBuilder.build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void get(GetItemRequest request, io.grpc.stub.StreamObserver<GetItemResponse> responseObserver) {
        LOGGER.info("GET RPC CALLED");
        itemGrpcRequestCounter.increment();

        try {
            EntityItem item = itemPersistence.get(request.getId());
            GrpcItem grpcItem = conversionLogic.getGrpcItemFromItem(item);
            GetItemResponse response = GetItemResponse.newBuilder().setItem(grpcItem).build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (ItemDoesNotExist e) {
            responseObserver.onError(Status.NOT_FOUND
                    .withDescription("Item with id " + request.getId() + " does not exist").asRuntimeException());
        }
    }

    @Override
    public void update(UpdateItemRequest request, io.grpc.stub.StreamObserver<UpdateItemResponse> responseObserver) {
        LOGGER.info("UPDATE RPC CALLED");
        itemGrpcRequestCounter.increment();

        try {
            // first check if the item does not exist
            itemPersistence.get(request.getId());

            EntityItem item = conversionLogic.getItemFromGrpcItem(request.getItem());
            EntityItem createdItem = itemPersistence.update(request.getId(), item);
    
            GrpcItem grpcItemResponse = conversionLogic.getGrpcItemFromItem(createdItem);
    
            UpdateItemResponse response = UpdateItemResponse.newBuilder().setItem(grpcItemResponse).build();
    
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (ItemDoesNotExist e) {
            responseObserver.onError(Status.NOT_FOUND
                    .withDescription("Item with id " + request.getId() + " does not exist").asRuntimeException());
        }
    }

    @Override
    public void delete(DeleteItemRequest request, io.grpc.stub.StreamObserver<DeleteItemResponse> responseObserver) {
        LOGGER.info("DELETE RPC CALLED");
        itemGrpcRequestCounter.increment();

        try {
            EntityItem deletedItem = itemPersistence.delete(request.getId());

            GrpcItem grpcItemResponse = conversionLogic.getGrpcItemFromItem(deletedItem);
            DeleteItemResponse response = DeleteItemResponse.newBuilder().setItem(grpcItemResponse).build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (ItemDoesNotExist e) {
            responseObserver.onError(Status.NOT_FOUND
                    .withDescription("Item with id " + request.getId() + " does not exist").asRuntimeException());
        }
    }
}
