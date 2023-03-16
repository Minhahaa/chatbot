package com.gp.chatbot.model.repositories.wis.order;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;
import org.springframework.stereotype.Repository;

import com.gp.chatbot.model.vo.wis.order.QTwDirectDeliEntity;
import com.gp.chatbot.model.vo.wis.order.TwDirectDeliEntity;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;

@Repository
public class TwDirectDeliRepositorySupport extends QuerydslRepositorySupport {
	
	private EntityManager em;
    private JPAQueryFactory queryFactory;
    private QTwDirectDeliEntity twDirectDeliEntity = new QTwDirectDeliEntity("wisEntityManager"); //BestiDatabaseConfig.class "entityManagerFactoryRef"

	public TwDirectDeliRepositorySupport() {
        super(QTwDirectDeliEntity.class);
    }

	@Override
    @PersistenceContext(unitName="wisEntityManager")
    public void setEntityManager(EntityManager entityManager) {
        super.setEntityManager(entityManager);
        em = entityManager;
        this.queryFactory = new JPAQueryFactory(entityManager);
    }

	public List<TwDirectDeliEntity> findAll() {
        return queryFactory.selectFrom(twDirectDeliEntity).fetch();
    }
    
    public List<TwDirectDeliEntity> getDeliNo(String deliNo){
    	return queryFactory.select(twDirectDeliEntity)
				.from(twDirectDeliEntity)
				.where(Expressions.stringTemplate("function('replace', {0}, {1}, {2})", twDirectDeliEntity.deliNo, "-", "").like("%"+deliNo.replaceAll("-", "")+"%"))
				.limit(5)
				.fetch();
    }

}
