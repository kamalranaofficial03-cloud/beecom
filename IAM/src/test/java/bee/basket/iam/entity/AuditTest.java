package bee.basket.iam.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.lang.reflect.Field;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class AuditTest {

    private static class TestAuditEntity extends Audit {
    }

    private TestAuditEntity audit;

    @BeforeEach
    void setUp() {
        audit = new TestAuditEntity();
    }

    @Test
    void auditFieldsAreInitiallyNull() {
        assertThat(audit.getCreatedBy()).isNull();
        assertThat(audit.getCreatedDate()).isNull();
        assertThat(audit.getLastModifiedBy()).isNull();
        assertThat(audit.getLastModifiedDate()).isNull();
    }

    @Test
    void setAndGetCreatedBy() {
        audit.setCreatedBy("creator");

        assertThat(audit.getCreatedBy()).isEqualTo("creator");
    }

    @Test
    void setAndGetCreatedDate() {
        LocalDateTime createdDate = LocalDateTime.of(2026, 11, 19, 9, 25, 5);

        audit.setCreatedDate(createdDate);

        assertThat(audit.getCreatedDate()).isEqualTo(createdDate);
    }

    @Test
    void setAndGetLastModifiedBy() {
        audit.setLastModifiedBy("modifier");

        assertThat(audit.getLastModifiedBy()).isEqualTo("modifier");
    }

    @Test
    void setAndGetLastModifiedDate() {
        LocalDateTime modifiedDate = LocalDateTime.of(2026, 11, 19, 10, 30);

        audit.setLastModifiedDate(modifiedDate);

        assertThat(audit.getLastModifiedDate()).isEqualTo(modifiedDate);
    }

    @Test
    void settersAcceptNullAndCanClearAuditValues() {
        audit.setCreatedBy("creator");
        audit.setCreatedDate(LocalDateTime.now());
        audit.setLastModifiedBy("modifier");
        audit.setLastModifiedDate(LocalDateTime.now());

        audit.setCreatedBy(null);
        audit.setCreatedDate(null);
        audit.setLastModifiedBy(null);
        audit.setLastModifiedDate(null);

        assertThat(audit.getCreatedBy()).isNull();
        assertThat(audit.getCreatedDate()).isNull();
        assertThat(audit.getLastModifiedBy()).isNull();
        assertThat(audit.getLastModifiedDate()).isNull();
    }

    @Test
    void auditFieldsCanBeSetIndependently() {
        LocalDateTime createdDate = LocalDateTime.of(2026, 1, 1, 8, 0);
        LocalDateTime modifiedDate = LocalDateTime.of(2026, 1, 2, 9, 0);

        audit.setCreatedBy("created-user");
        audit.setCreatedDate(createdDate);

        assertThat(audit.getCreatedBy()).isEqualTo("created-user");
        assertThat(audit.getCreatedDate()).isEqualTo(createdDate);
        assertThat(audit.getLastModifiedBy()).isNull();
        assertThat(audit.getLastModifiedDate()).isNull();

        audit.setLastModifiedBy("modified-user");
        audit.setLastModifiedDate(modifiedDate);

        assertThat(audit.getCreatedBy()).isEqualTo("created-user");
        assertThat(audit.getCreatedDate()).isEqualTo(createdDate);
        assertThat(audit.getLastModifiedBy()).isEqualTo("modified-user");
        assertThat(audit.getLastModifiedDate()).isEqualTo(modifiedDate);
    }

    @Test
    void auditIsMappedSuperclassWithAuditingListener() {
        assertThat(Audit.class).hasAnnotation(MappedSuperclass.class);

        EntityListeners listeners = Audit.class.getAnnotation(EntityListeners.class);
        assertThat(listeners.value()).containsExactly(AuditingEntityListener.class);
    }

    @Test
    void createdByHasExpectedColumnAndSpringDataAnnotations() throws NoSuchFieldException {
        Field field = Audit.class.getDeclaredField("createdBy");

        assertThat(field.isAnnotationPresent(CreatedBy.class)).isTrue();
        assertColumn(field, "created_by", false);
    }

    @Test
    void createdDateHasExpectedColumnAndSpringDataAnnotations() throws NoSuchFieldException {
        Field field = Audit.class.getDeclaredField("createdDate");

        assertThat(field.isAnnotationPresent(CreatedDate.class)).isTrue();
        assertColumn(field, "created_date", false);
    }

    @Test
    void lastModifiedByHasExpectedColumnAndSpringDataAnnotations() throws NoSuchFieldException {
        Field field = Audit.class.getDeclaredField("lastModifiedBy");

        assertThat(field.isAnnotationPresent(LastModifiedBy.class)).isTrue();
        assertColumn(field, "last_modified_by", true);
    }

    @Test
    void lastModifiedDateHasExpectedColumnAndSpringDataAnnotations() throws NoSuchFieldException {
        Field field = Audit.class.getDeclaredField("lastModifiedDate");

        assertThat(field.isAnnotationPresent(LastModifiedDate.class)).isTrue();
        assertColumn(field, "last_modified_date", true);
    }

    @Test
    void auditIsInheritedByAnEntitySubclass() {
        assertThat(audit).isInstanceOf(Audit.class);
        assertThat(audit).isInstanceOf(BaseId.class);
    }

    private static void assertColumn(Field field, String name, boolean updatable) {
        Column column = field.getAnnotation(Column.class);
        assertThat(column.name()).isEqualTo(name);
        assertThat(column.updatable()).isEqualTo(updatable);
    }
}
