package com.cqrs.scorekeeper.repository;

import com.cqrs.table.model.PointsTable;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PointsTableRepository extends MongoRepository<PointsTable, String> {

	Optional<PointsTable> findByTableId(String tableId);
}
