package pl.jma.template.esrepository;

import org.springframework.data.elasticsearch.repository.ElasticsearchCrudRepository;
import org.springframework.stereotype.Repository;

import pl.jma.template.model.Product;

@Repository
public interface ProductSearchRepository extends ElasticsearchCrudRepository<Product, String> {

}
