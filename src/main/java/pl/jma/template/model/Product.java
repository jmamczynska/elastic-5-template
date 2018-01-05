package pl.jma.template.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Entity
@Document(indexName = "products", type = "products", shards = 1)
public class Product {

	@Id
	@GenericGenerator(name = "invoice_id", strategy = "uuid")
	@GeneratedValue(generator = "invoice_id")
	@Column(name = "invoice_id", length = 100)
	@org.springframework.data.annotation.Id
	private String id;

	@Column(name = "product_name")
	@Field(type = FieldType.text, index = true)
	private String productName;

	@Field(type = FieldType.text)
	private InvoiceStateEnum state;

	public Product() {
	}

	public Product(String productName) {
		this.productName = productName;
	}

	public String getId() {
		return id;
	}

	public String getProductName() {
		return productName;
	}

	public InvoiceStateEnum getState() {
		return state;
	}

	public void setState(InvoiceStateEnum state) {
		this.state = state;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setProductName(String productName) {
		this.productName = productName;
	}

}
