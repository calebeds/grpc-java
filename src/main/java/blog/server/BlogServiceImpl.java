package blog.server;

import com.mongodb.MongoException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.InsertOneResult;
import com.proto.blog.Blog;
import com.proto.blog.BlogId;
import com.proto.blog.BlogServiceGrpc;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.bson.Document;

import java.util.Objects;

public class BlogServiceImpl extends BlogServiceGrpc.BlogServiceImplBase {

    private final MongoCollection<Document> mongoCollection;

    public BlogServiceImpl(final MongoClient client) {
        final MongoDatabase db = client.getDatabase("blogdb");

        this.mongoCollection = db.getCollection("blog");
    }

    @Override
    public void createBlog(final Blog request, final StreamObserver<BlogId> responseObserver) {
        final Document doc = new Document("author", request.getAuthor())
                .append("title", request.getTitle())
                .append("content", request.getContent());

        InsertOneResult result;

        try {
            result = mongoCollection.insertOne(doc);
        } catch (final MongoException exception) {
            responseObserver.onError(Status.INTERNAL
                    .withDescription(exception.getLocalizedMessage())
                    .asRuntimeException());
            return;
        }

        if(!result.wasAcknowledged() || result.getInsertedId() == null) {
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Blog couldn't be created")
                    .asRuntimeException());
            return;
        }

        final String id = result.getInsertedId().asObjectId().getValue().toString();

        responseObserver.onNext(BlogId.newBuilder().setId(id).build());
        responseObserver.onCompleted();
    }
}
