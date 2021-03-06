package bootiful;

import com.vaadin.addon.jpacontainer.JPAContainer;
import com.vaadin.addon.jpacontainer.JPAContainerFactory;
import com.vaadin.annotations.Theme;
import com.vaadin.data.Item;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.VaadinRequest;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.spring.annotation.UIScope;
import com.vaadin.ui.*;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.context.annotation.Bean;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Arrays;
import java.util.Locale;

@SpringBootApplication
public class DemoApplication {

    @Bean
    CommandLineRunner init(CustomerRepository customerRepository) {
        return args ->
                Arrays.asList("Nicolas,Frankel", "Josh,Long", "John,Davies")
                        .stream()
                        .map(n -> n.split(","))
                        .forEach(x -> customerRepository.save(new Customer(x[0], x[1])));
    }

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }
}

@Theme("valo")
@SpringUI
class CustomerUI extends UI {

    @Autowired
    private CustomerView customerView;

    @Override
    protected void init(VaadinRequest vaadinRequest) {

        VerticalLayout root = new VerticalLayout(this.customerView);
        root.setSizeFull();
        root.setMargin(true);
        root.setSpacing(true);
        root.setExpandRatio(this.customerView, 1.0F);
        this.setContent(root);
    }
}

@UIScope
@SpringView
class CustomerView extends VerticalLayout
        implements MessageSourceAware, View, InitializingBean {

    private Locale locale = Locale.getDefault();

    private MessageSource messageSource;

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        // the view is constructed in the init() method()
        LogFactory.getLog(getClass()).info( "enter view");
    }

    @Override
    public void afterPropertiesSet() throws Exception {


        JPAContainer<Customer> customerJPAContainer = JPAContainerFactory.make(Customer.class, this.entityManager);

        com.vaadin.ui.Table customerTable = new com.vaadin.ui.Table(
                this.messageSource.getMessage("customer-table", new Object[0], locale),
                customerJPAContainer);

        customerTable.setColumnHeader("id",
                this.messageSource.getMessage("id-col", new Object[0], locale));

        customerTable.setColumnHeader("firstName",
                this.messageSource.getMessage("first-name-col", new Object[0], locale));

        customerTable.setColumnHeader("lastName",
                this.messageSource.getMessage("last-name-col", new Object[0], locale));

        Table.ColumnGenerator columnGenerator = (table, itemId, propertyId) -> {

            Item item = table.getItem(itemId);
            Long id = (Long) (item.getItemProperty("id")).getValue();
            String firstName = (String) (item.getItemProperty("firstName")).getValue();

            Button button = new Button(this.messageSource.getMessage("say-hello", new Object[0], locale));
            button.addClickListener(evt -> Notification.show(this.messageSource.getMessage(
                    "greeting", new Object[]{id, firstName}, locale)));
            return button;

        };
        customerTable.addGeneratedColumn("hello", columnGenerator);
        customerTable.setColumnHeader("id", this.messageSource.getMessage("id-col", new Object[0], locale));
        customerTable.setColumnHeader("hello", this.messageSource.getMessage("hello-col", new Object[0], locale));
        customerTable.setVisibleColumns("id", "firstName", "lastName", "hello");
        this.addComponent(customerTable);
    }

    @Override
    public void setMessageSource(MessageSource messageSource) {
        this.messageSource = messageSource;
    }
}