package com.cqrs.scoreeventprocessor.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Standing {
  String teamName;
  int rank;
  int points;
  int wins;
  int draws;
  int losses;
  int goalDifference;
  int goalsScored;
  int goalsConceded;
}
