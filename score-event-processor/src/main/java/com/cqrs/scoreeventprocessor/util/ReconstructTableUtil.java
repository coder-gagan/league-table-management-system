package com.cqrs.scoreeventprocessor.util;

import com.cqrs.table.LeagueTableReconstructor;
import org.springframework.stereotype.Component;

/** Spring facade for shared {@link LeagueTableReconstructor} logic. */
@Component
public class ReconstructTableUtil extends LeagueTableReconstructor {}
