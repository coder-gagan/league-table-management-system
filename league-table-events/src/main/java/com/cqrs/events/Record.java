package com.cqrs.events;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class Record implements Serializable {
  String teamName;
  Integer matchNumber;
  boolean isHomeGame;
  Integer points;
  Integer goalsScored;
  Integer goalsConceded;
}
