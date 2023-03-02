package com.gp.chatbot.config;

import java.util.HashMap;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
	    basePackages = "com.gp.chatbot.model.repositories.sm", 
	    entityManagerFactoryRef = "smEntityManager", 
	    transactionManagerRef = "smTransactionManager"
)
public class SmDatabaseConfig {

	@Autowired
    private Environment env;
	
	@Bean
	@Qualifier
    public LocalContainerEntityManagerFactoryBean smEntityManager() {
        LocalContainerEntityManagerFactoryBean em
          = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(smDataSource());
        em.setPackagesToScan(
          new String[] { "com.gp.chatbot.model.vo.sm" });

        HibernateJpaVendorAdapter vendorAdapter
          = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);
        HashMap<String, Object> properties = new HashMap<>();
        properties.put("hibernate.dialect",
          env.getProperty("hibernate.dialect"));
        em.setJpaPropertyMap(properties);

        return em;
    }
	
    @Bean
    @Qualifier
    public DataSource smDataSource() {
 
        DriverManagerDataSource dataSource
          = new DriverManagerDataSource();
        dataSource.setDriverClassName(
          env.getProperty("spring.datasource-sm.driver-class-name"));
        dataSource.setUrl(env.getProperty("spring.datasource-sm.url"));
        dataSource.setUsername(env.getProperty("spring.datasource-sm.username"));
        dataSource.setPassword(env.getProperty("spring.datasource-sm.password"));

        return dataSource;
    }
	
    @Bean
    @Qualifier
    public PlatformTransactionManager smTransactionManager() {
 
        JpaTransactionManager transactionManager
          = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(
        		smEntityManager().getObject());
        return transactionManager;
    }
	
}
