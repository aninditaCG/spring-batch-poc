package org.sb.batch.poc.app.config;

import org.sb.batch.poc.app.model.Customer;
import org.springframework.batch.item.ItemProcessor;

public class CustomerProcessor implements ItemProcessor<Customer,Customer> {

//    @Override
//    public Customer process(Customer customer) throws Exception {
//        if(customer.getCountry().equals("United States")) {
//            return customer;
//        }else{
//            return null;
//        }
//    }
    @Override
    public Customer process(Customer item) throws Exception {
        return item;
    }
}