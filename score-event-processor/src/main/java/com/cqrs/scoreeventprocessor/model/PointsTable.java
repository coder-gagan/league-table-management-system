package com.cqrs.scoreeventprocessor.model;

import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "points_table")
@Setter
public class PointsTable {

  @Id
  String tableId;
  List<Standing> standings;

  public List<Standing> getStandings() {
    if(null == standings) {
      standings = new ArrayList<>();
    }
    return standings;
  }
}