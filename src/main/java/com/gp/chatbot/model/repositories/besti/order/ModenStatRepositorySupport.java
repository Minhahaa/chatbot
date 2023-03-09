package com.gp.chatbot.model.repositories.besti.order;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;
import org.springframework.stereotype.Repository;

import com.gp.chatbot.model.vo.besti.order.ModenStatEntity;
import com.gp.chatbot.model.vo.besti.order.QModenStatEntity;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;

@Repository
public class ModenStatRepositorySupport extends QuerydslRepositorySupport {
	
	private EntityManager em;
    private JPAQueryFactory queryFactory;
    private QModenStatEntity modenStatEntity = new QModenStatEntity("modenStatEntity"); //BestiDatabaseConfig.class "entityManagerFactoryRef"

	public ModenStatRepositorySupport() {
        super(ModenStatEntity.class);
    }

	@Override
    @PersistenceContext(unitName="bestiEntityManager")
    public void setEntityManager(EntityManager entityManager) {
        super.setEntityManager(entityManager);
        em = entityManager;
        this.queryFactory = new JPAQueryFactory(entityManager);
    }

	public List<ModenStatEntity> findAll() {
        return queryFactory.selectFrom(modenStatEntity).fetch();
    }
    
    public List<ModenStatEntity> getDeliNo(String deliNo){
    	return queryFactory.select(modenStatEntity)
				.from(modenStatEntity)
				.where(Expressions.stringTemplate("function('replace', {0}, {1}, {2})", modenStatEntity.parcelData, "-", "").like("%"+deliNo.replaceAll("-", "")+"%"))
				.limit(5)
				.fetch();
    }

}
