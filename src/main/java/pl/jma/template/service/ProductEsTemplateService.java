package pl.jma.template.service;

import static org.slf4j.LoggerFactory.getLogger;

import org.slf4j.Logger;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.ScrolledPage;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.stereotype.Service;

import pl.jma.template.model.Product;

@Service
public class ProductEsTemplateService {

	private static final Logger LOG = getLogger(ProductEsTemplateService.class);

	private final ElasticsearchTemplate esTemplate;

	public ProductEsTemplateService(ElasticsearchTemplate esTemplate) {
		this.esTemplate = esTemplate;
	}

	public void scrollingWithElasticTemplate() {

		CriteriaQuery criteriaQuery = new CriteriaQuery(Criteria.where("productName").is("pen"));
		criteriaQuery.addIndices("products");
		criteriaQuery.addTypes("products");
		criteriaQuery.setPageable(PageRequest.of(0, 1000));

		ScrolledPage<Product> scroll = (ScrolledPage<Product>) esTemplate.startScroll(3000, criteriaQuery, Product.class);

		while (scroll.hasContent()) {
			LOG.info("Next page with 1000 elem: " + scroll.getContent());
			scroll = (ScrolledPage<Product>) esTemplate.continueScroll(scroll.getScrollId(), 3000, Product.class);
		}

		esTemplate.clearScroll(scroll.getScrollId());
	}

}
