package com.cqrs.table.model;

import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "points_table")
public class PointsTable {

  @Id private String tableId;
  private List<Standing> standings;

  public List<Standing> getStandings() {
    if (standings == null) {
      standings = new ArrayList<>();
    }
    return standings;
  }
}
