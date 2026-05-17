package com.cqrs.table.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Standing {
  private String teamName;
  private int rank;
  private int points;
  private int wins;
  private int draws;
  private int losses;
  private int goalDifference;
  private int goalsScored;
  private int goalsConceded;
}
