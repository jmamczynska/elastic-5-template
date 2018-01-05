package pl.jma.template.rest;

import static org.slf4j.LoggerFactory.getLogger;

import java.util.List;

import org.slf4j.Logger;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import pl.jma.template.esrepository.ProductSearchRepository;
import pl.jma.template.model.InvoiceStateEnum;
import pl.jma.template.model.Product;
import pl.jma.template.repository.ProductRepository;
import pl.jma.template.service.ProductEsClinetService;

@RestController
@RequestMapping("/api/products")
public class ProductRestController {

	private static final Logger LOG = getLogger(ProductRestController.class);

	private final ProductRepository productRepository;
	private final ProductSearchRepository productSearchRepository;
	private final ProductEsClinetService productEsClinetService;

	public ProductRestController(ProductRepository productRepository, ProductSearchRepository productSearchRepository, ProductEsClinetService productEsClinetService) {
		this.productRepository = productRepository;
		this.productSearchRepository = productSearchRepository;
		this.productEsClinetService = productEsClinetService;
	}

	@GetMapping(value = "/all", produces = {MediaType.APPLICATION_JSON_VALUE})
	public ResponseEntity<List<Product>> findAll() {
		LOG.debug("Getting all products");

		List<Product> products = productRepository.findAll();
		return ResponseEntity.ok(products);
	}

	@GetMapping(value = "/aggregated", produces = {MediaType.APPLICATION_JSON_VALUE})
	public ResponseEntity<Void> printAggregated() {
		LOG.debug("Testing nested aggregation");

		productEsClinetService.searchForNestedWithAggregation();
		return ResponseEntity.noContent().build();
	}

	@PostMapping("/save")
	public ResponseEntity<Product> save() {
		LOG.debug("Testing mutual saving with shared UUID");

		Product pr = new Product("Product 1");
		pr.setState(InvoiceStateEnum.PAID);

		productRepository.save(pr);
		productSearchRepository.save(pr);

		return ResponseEntity.ok(pr);
	}

}
