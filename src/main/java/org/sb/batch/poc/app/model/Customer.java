package org.sb.batch.poc.app.model;

import jakarta.persistence.*;
//import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

//import javax.persistence.*;


@Entity
@Table(name = "CUSTOMERS_INFO")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document
public class Customer {
    @Id
    @Column(name = "CUSTOMER_ID")
    private int id;
    @Column(name = "LAST_NAME")
    private String name;
    @Column(name = "EMAIL")
    private String email;
}