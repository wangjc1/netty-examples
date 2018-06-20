package com.phei.netty.protocol.http.xml.jibx;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import org.jibx.runtime.JiBXException;
import org.jibx.runtime.IBindingFactory;
import org.jibx.runtime.BindingDirectory;
import org.jibx.runtime.IUnmarshallingContext;

class Test {
    public static void main(String[] args) {
        try {
            IBindingFactory bfact = BindingDirectory.getFactory(Customer.class);
            IUnmarshallingContext uctx = bfact.createUnmarshallingContext();
            Customer customer = (Customer) uctx.unmarshalDocument(new FileInputStream(Test.class.getResource("/").getPath()+"/data.xml"), null);
            Person person = customer.person;

            System.out.println("cust-num:" + person.customerNumber);
            System.out.println("first-name:" + person.firstName);
            System.out.println("last-name:" + person.lastName);
            System.out.println("street:" + customer.street);
        } catch (FileNotFoundException e) {
            System.out.println(e.toString());
        } catch (JiBXException e) {
            System.out.println(e.toString());
        }
    }
}