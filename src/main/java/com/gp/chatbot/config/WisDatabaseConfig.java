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
	    basePackages = "com.gp.chatbot.model.repositories.wis", 
	    entityManagerFactoryRef = "wisEntityManager", 
	    transactionManagerRef = "wisTransactionManager"
)
public class WisDatabaseConfig {

	@Autowired
    private Environment env;
	
	@Bean
	@Qualifier
    public LocalContainerEntityManagerFactoryBean wisEntityManager() {
        LocalContainerEntityManagerFactoryBean em
          = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(wisDataSource());
        em.setPackagesToScan(
          new String[] { "com.gp.chatbot.model.vo.wis" });

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
    public DataSource wisDataSource() {
 
        DriverManagerDataSource dataSource
          = new DriverManagerDataSource();
        dataSource.setDriverClassName(
          env.getProperty("spring.datasource-wis.driver-class-name"));
        dataSource.setUrl(env.getProperty("spring.datasource-wis.url"));
        dataSource.setUsername(env.getProperty("spring.datasource-wis.username"));
        dataSource.setPassword(env.getProperty("spring.datasource-wis.password"));

        return dataSource;
    }
	
    @Bean
    @Qualifier
    public PlatformTransactionManager wisTransactionManager() {
 
        JpaTransactionManager transactionManager
          = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(
        		wisEntityManager().getObject());
        return transactionManager;
    }
	
}
