package org.sb.batch.poc.app.repository;


import org.sb.batch.poc.app.model.Customer;
import org.springframework.data.mongodb.repository.MongoRepository;

//@Repository
public interface CustomerRepository extends MongoRepository<Customer, Integer> {}

