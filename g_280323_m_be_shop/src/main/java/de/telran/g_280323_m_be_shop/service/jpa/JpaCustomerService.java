package de.telran.g_280323_m_be_shop.service.jpa;

import de.telran.g_280323_m_be_shop.domain.entity.interfaces.Cart;
import de.telran.g_280323_m_be_shop.domain.entity.interfaces.Customer;
import de.telran.g_280323_m_be_shop.domain.entity.interfaces.Product;
import de.telran.g_280323_m_be_shop.domain.entity.jpa.JpaCart;
import de.telran.g_280323_m_be_shop.domain.entity.jpa.JpaCustomer;
import de.telran.g_280323_m_be_shop.repository.jpa.JpaCartRepository;
import de.telran.g_280323_m_be_shop.repository.jpa.JpaCustomerRepository;
import de.telran.g_280323_m_be_shop.repository.jpa.JpaProductRepository;
import de.telran.g_280323_m_be_shop.service.interfaces.CustomerService;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class JpaCustomerService implements CustomerService {

    //slf4j here
    private final Logger LOGGER = LoggerFactory.getLogger(JpaCustomerService.class);


    private JpaCustomerRepository repository;
    private JpaCartRepository cartRepository;
    private JpaProductRepository productRepository;

    public JpaCustomerService(JpaCustomerRepository repository, JpaCartRepository cartRepository, JpaProductRepository productRepository) {
        this.repository = repository;
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
    }

    @Override
    public List<Customer> getAll() {
        return new ArrayList<>(repository.findAll());
    } // (GET) http://localhost:8080/customer - OK

    @Override
    public Customer getById(int id) {
        LOGGER.info("Запрошен покупатель с идентификатором {}.", id);
        LOGGER.warn("Запрошен покупатель с идентификатором {}.", id);
        LOGGER.error("Запрошен покупатель с идентификатором {}.", id);
        return repository.findById(id).orElse(null);
    } // (GET) http://localhost:8080/customer/findCustomerById/1 - OK

    @Override
    public void add(Customer customer) {
        JpaCustomer savedCustomer = repository.save(new JpaCustomer(0, customer.getName()));
        cartRepository.save(new JpaCart(savedCustomer));
    }  // (POST) http://localhost:8080/customer/addCustomer - OK
    // USE THE "BODY" SECTION, like
//    {
//        "name": "Checking",
//            "cart": {
//        "products": []
//    }
//    }

    @Override
    public void deleteById(int id) {
        cartRepository.deleteById(id);
        repository.deleteById(id);
    } // (DELETE) http://localhost:8080/customer/deleteCustomerById/5 - OK

    @Transactional
    @Override
    public void deleteByName(String name) {
        repository.deleteByName(name);
    } // НЕ РАБОТАЕТ; в консоли текст ошибки следующий:
// 2023-11-15T01:45:26.279+02:00  WARN 7324 --- [nio-8080-exec-4] o.h.engine.jdbc.spi.SqlExceptionHelper   :
// SQL Error: 1451, SQLState: 23000
// 2023-11-15T01:45:26.282+02:00 ERROR 7324 --- [nio-8080-exec-4] o.h.engine.jdbc.spi.SqlExceptionHelper   :
// Cannot delete or update a parent row: a foreign key constraint fails (`280323-m-be-shop-jpa`.`cart`,
// CONSTRAINT `fk_cart_to_customer` FOREIGN KEY (`customer_id`) REFERENCES `customer` (`customer_id`))
// 2023-11-15T01:45:26.306+02:00 ERROR 7324 --- [nio-8080-exec-4] o.a.c.c.C.[.[.[/].[dispatcherServlet]    : Servlet.service() for servlet [dispatcherServlet] in context with path [] threw exception [Request processing failed: org.springframework.dao.DataIntegrityViolationException: could not execute statement [Cannot delete or update a parent row: a foreign key constraint fails (`280323-m-be-shop-jpa`.`cart`,
// CONSTRAINT `fk_cart_to_customer` FOREIGN KEY (`customer_id`) REFERENCES `customer` (`customer_id`))] [delete from customer where customer_id=?]; SQL [delete from customer where customer_id=?]; constraint [null]] with root cause
// Андрей, если не затруднит, сможете подсказать, в чём дело? Ту же проблему встретил в предыдущем методе,
// но том удалось справиться, сначала удалив корзину, а уже потом покупателя

    @Override
    public int getCount() {
        return (int) repository.count();
    } // (GET) http://localhost:8080/customer/countCustomers - OK

    @Override
    public double getTotalPriceById(int id) {
        return getById(id).getCart().getTotalPrice();
    } // (GET) http://localhost:8080/customer/cartPriceById/1 - OK

    @Override
    public double getAveragePriceById(int id) {
        return getById(id).getCart().getAveragePrice();
    } // (GET) http://localhost:8080/customer/averageCartPriceById/1 - OK

    @Transactional
    @Override
    public void addToCartById(int customerId, int productId) {
        Customer customer = repository.findById(customerId).orElse(null);
        Product product = productRepository.findById(productId).orElse(null);
        Cart cart = customer.getCart();
        cart.addProduct(product);
    } // (POST) http://localhost:8080/customer/addProductToCartById/4/2 - OK


    @Transactional
    @Override
    public void deleteFromCartById(int customerId, int productId) {
        getById(customerId).getCart().deleteProduct(productId);
    } // (DELETE) http://localhost:8080/customer/deleteFromCart/4/2 - OK

    @Transactional
    @Override
    public void clearCartById(int customerId) {
        getById(customerId).getCart().clear();
    } // (DELETE) http://localhost:8080/customer/clear/4 - OK
}