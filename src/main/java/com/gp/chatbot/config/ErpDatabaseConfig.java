package com.gp.chatbot.config;

import java.util.HashMap;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@PropertySource("classpath:/application.yml")
@EnableJpaRepositories(
	    basePackages = "com.gp.chatbot.model.repositories.goods.erp", 
	    entityManagerFactoryRef = "erpEntityManager", 
	    transactionManagerRef = "erpTransactionManager"
)
public class ErpDatabaseConfig {

	@Autowired
    private Environment env;
	
	@Bean
    @Primary
    public LocalContainerEntityManagerFactoryBean erpEntityManager() {
        LocalContainerEntityManagerFactoryBean em
          = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(erpDataSource());
        em.setPackagesToScan(
          new String[] { "com.gp.chatbot.model.vo.order.erp" });

        HibernateJpaVendorAdapter vendorAdapter
          = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);
        HashMap<String, Object> properties = new HashMap<>();
        properties.put("hibernate.dialect",
          env.getProperty("hibernate.dialect"));
        em.setJpaPropertyMap(properties);

        return em;
    }
	
	@Primary
    @Bean
    public DataSource erpDataSource() {
 
        DriverManagerDataSource dataSource
          = new DriverManagerDataSource();
        dataSource.setDriverClassName(
          env.getProperty("spring.datasource-erp.driver-class-name"));
        dataSource.setUrl(env.getProperty("spring.datasource-erp.url"));
        dataSource.setUsername(env.getProperty("spring.datasource-erp.username"));
        dataSource.setPassword(env.getProperty("spring.datasource-erp.password"));

        return dataSource;
    }
	
	@Primary
    @Bean
    public PlatformTransactionManager erpTransactionManager() {
 
        JpaTransactionManager transactionManager
          = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(
        		erpEntityManager().getObject());
        return transactionManager;
    }
	
}
