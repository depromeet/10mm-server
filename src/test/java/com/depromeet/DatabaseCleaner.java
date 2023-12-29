package com.depromeet;

import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Table;
import jakarta.persistence.metamodel.EntityType;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DatabaseCleaner implements InitializingBean {

    @PersistenceContext private EntityManager entityManager;
    private List<String> tableNames;

    @Override
    public void afterPropertiesSet() {
        tableNames =
                entityManager.getMetamodel().getEntities().stream()
                        .filter(e -> e.getJavaType().getAnnotation(Entity.class) != null)
                        .filter(e -> e.getJavaType().getAnnotation(Table.class) == null)
                        .map(EntityType::getName)
                        .collect(Collectors.toList());
        List<String> tableNamesWithAnnotation =
                entityManager.getMetamodel().getEntities().stream()
                        .filter(e -> e.getJavaType().getAnnotation(Table.class) != null)
                        .map(e -> e.getJavaType().getAnnotation(Table.class).name())
                        .toList();
        tableNames.addAll(tableNamesWithAnnotation);
    }

    @Transactional
    public void execute() {
        // 쓰기 지연 저장소에 남은 SQL을 마저 수행
        entityManager.flush();
        // 연관 관계 매핑된 테이블 참조 무결성 해제
        entityManager.createNativeQuery("SET REFERENTIAL_INTEGRITY FALSE").executeUpdate();

        for (String tableName : tableNames) {
            entityManager.createNativeQuery("TRUNCATE TABLE " + tableName).executeUpdate();
            entityManager
                    .createNativeQuery(
                            "ALTER TABLE "
                                    + tableName
                                    + " ALTER COLUMN "
                                    + tableName
                                    + "_id RESTART WITH 1")
                    .executeUpdate();
        }

        entityManager.createNativeQuery("SET REFERENTIAL_INTEGRITY TRUE").executeUpdate();
    }
}
