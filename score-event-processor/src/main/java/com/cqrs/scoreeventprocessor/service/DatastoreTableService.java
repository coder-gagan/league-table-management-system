package com.cqrs.scoreeventprocessor.service;

import com.cqrs.table.model.PointsTable;
import com.cqrs.scoreeventprocessor.repository.PointsTableRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DatastoreTableService {

  @Autowired
  private final PointsTableRepository repository;

  public DatastoreTableService(PointsTableRepository repository) {
    this.repository = repository;
  }

  public PointsTable savePointsTable(PointsTable pointsTable) {
    return repository.save(pointsTable);
  }

  public Optional<PointsTable> getPointsTableById(String id) {
    return repository.findByTableId(id);
  }

  public List<PointsTable> getAllPointsTables() {
    return repository.findAll();
  }

  public void deletePointsTable(String id) {
    repository.deleteById(id);
  }
}

