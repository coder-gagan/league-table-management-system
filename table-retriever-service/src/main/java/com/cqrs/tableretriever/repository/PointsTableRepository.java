package com.cqrs.tableretriever.repository;

import com.cqrs.tableretriever.model.PointsTable;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PointsTableRepository extends MongoRepository<PointsTable, String> {

  Optional<PointsTable> findByTableId(String tableId);
}
