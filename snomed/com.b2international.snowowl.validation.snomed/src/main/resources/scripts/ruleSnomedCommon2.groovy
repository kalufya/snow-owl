package scripts;

import com.b2international.index.Hits
import com.b2international.index.aggregations.Aggregation
import com.b2international.index.aggregations.AggregationBuilder
import com.b2international.index.query.Expression
import com.b2international.index.query.Expressions
import com.b2international.index.query.Query
import com.b2international.index.revision.RevisionSearcher
import com.b2international.snowowl.core.ComponentIdentifier
import com.b2international.snowowl.snomed.common.SnomedTerminologyComponentConstants
import com.b2international.snowowl.snomed.common.SnomedConstants.Concepts
import com.b2international.snowowl.snomed.datastore.index.entry.SnomedConceptDocument
import com.b2international.snowowl.snomed.datastore.index.entry.SnomedDescriptionIndexEntry
import com.google.common.collect.Lists
import com.google.common.collect.Sets

RevisionSearcher searcher = ctx.service(RevisionSearcher.class)

Iterable<Hits<String>> activeConceptBatches = searcher.scroll(Query.select(String.class)
		.from(SnomedConceptDocument.class)
		.fields(SnomedConceptDocument.Fields.ID)
		.where(SnomedConceptDocument.Expressions.active())
		.limit(10_000)
		.build())

Set<String> activeConceptIds = Sets.newHashSet()

activeConceptBatches.each({ conceptBatch ->
	activeConceptIds.addAll(conceptBatch.getHits())
})

Expression activeFsnExpression = Expressions.builder()
		.filter(SnomedDescriptionIndexEntry.Expressions.active())
		.filter(SnomedDescriptionIndexEntry.Expressions.type(Concepts.FULLY_SPECIFIED_NAME))
		.filter(SnomedDescriptionIndexEntry.Expressions.concepts(activeConceptIds))
		.build()

Aggregation<String> activeDescriptionsByOriginalTerm = searcher
		.aggregate(AggregationBuilder.bucket("ruleSnomedCommon2", String.class, SnomedDescriptionIndexEntry.class)
		.query(activeFsnExpression)
		.onFieldValue(SnomedDescriptionIndexEntry.Fields.ORIGINAL_TERM)
		.fields(SnomedDescriptionIndexEntry.Fields.ID)
		.minBucketSize(2))

List<ComponentIdentifier> issueDetails = Lists.newArrayList()

activeDescriptionsByOriginalTerm.getBuckets().values().each({ bucket ->
	bucket.each({ descId ->
		issueDetails.add(ComponentIdentifier.of(SnomedTerminologyComponentConstants.DESCRIPTION_NUMBER, descId))
	})
})

return issueDetails
