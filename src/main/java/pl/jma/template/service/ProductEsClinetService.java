package pl.jma.template.service;

import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.filter.InternalFilter;
import org.elasticsearch.search.aggregations.bucket.nested.InternalNested;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms.Order;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import pl.jma.template.model.Product;

@Service
public class ProductEsClinetService {

	private static final Logger LOG = getLogger(ProductEsClinetService.class);

	private final Client client;

	public ProductEsClinetService(Client client) {
		this.client = client;
	}

	public void scrollWithElasticClient() {

		QueryBuilder prodBuilder = QueryBuilders.boolQuery()
				.must(QueryBuilders.matchQuery("productName", "pen"));

		SearchResponse scrollResp = client.prepareSearch("products")
				.setScroll(new TimeValue(60000))
				.setSize(1000)
				.setTypes("products")
				.setQuery(prodBuilder)
				.execute()
				.actionGet();

		ObjectMapper mapper = new ObjectMapper();
		List<Product> products = new ArrayList<>();

		try {
			do {
				for (SearchHit hit : scrollResp.getHits().getHits()) {
					products.add(mapper.readValue(hit.getSourceAsString(), Product.class));
				}

				LOG.info("Next page with 1000 elem: " + products);
				products.clear();
				scrollResp = client.prepareSearchScroll(scrollResp.getScrollId())
						.setScroll(new TimeValue(60000)).execute().actionGet();

			} while (scrollResp.getHits().getHits().length != 0);

		} catch (IOException e) {
			LOG.error("Exception while executing query {}", e);
		}
	}

	public void searchForNested() {

		QueryBuilder matchFirst = QueryBuilders.boolQuery()
				.must(QueryBuilders.matchQuery("authorList.authorOrder", 1));
		QueryBuilder mainQuery = QueryBuilders.nestedQuery("authorList", matchFirst, ScoreMode.None);

		@SuppressWarnings("rawtypes")
		SortBuilder sb = SortBuilders.fieldSort("authorList.lastName")
				.order(SortOrder.ASC).setNestedPath("authorList")
				.setNestedFilter(matchFirst);

		SearchRequestBuilder builder = client.prepareSearch("test")
				.setSize(50)
				.setQuery(mainQuery)
				.addSort(sb);

		SearchResponse response;
		try {
			response = builder.execute().get();

			for (SearchHit hit : response.getHits().getHits()) {
				LOG.info("Result: " + hit.getSourceAsString());
			}
		} catch (InterruptedException | ExecutionException e) {
			LOG.error("Exception while executing query {}", e);
		}

	}

	public void searchForNestedWithAggregation() {

		QueryBuilder matchAll = QueryBuilders.boolQuery()
				.must(QueryBuilders.matchQuery("authorList.authorOrder", 1));
		QueryBuilder query = QueryBuilders.nestedQuery("authorList", matchAll, ScoreMode.None);

		AggregationBuilder aggregation = AggregationBuilders
				.nested("authors", "authorList")
				.subAggregation(AggregationBuilders.filter("inner", QueryBuilders.termQuery("authorList.authorOrder", 1))
						.subAggregation(AggregationBuilders.terms("name")
								.field("authorList.lastName").order(Order.term(true))));

		SearchRequestBuilder builder = client.prepareSearch("test")
				.setSize(50)
				.setQuery(query)
				.addAggregation(aggregation);

		SearchResponse response;

		try {
			response = builder.execute().get();

			for (SearchHit hit : response.getHits().getHits()) {
				LOG.info("Result: " + hit.getSourceAsString());
			}

			InternalNested authors = response.getAggregations().get("authors");
			InternalFilter inner = authors.getAggregations().get("inner");
			Terms names = inner.getAggregations().get("name");

			for (Terms.Bucket entry : names.getBuckets()) {
				LOG.info("" + entry.getKey().toString());
			}
		} catch (InterruptedException | ExecutionException e) {
			LOG.error("Exception while executing query {}", e);
		}

	}

}
