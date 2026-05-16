package io.zlero.cRFramework.database.repository

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.transactions.transaction

/**
 * Exposed DAO 기반 CRUD 추상 레포지토리
 *
 * 사용법:
 *   @Repository
 *   class ItemRepository : CrudRepository<ItemEntity, ItemTable>(ItemTable, ItemEntity) {
 *       fun findByName(name: String) = query {
 *           ItemEntity.find { ItemTable.name eq name }.toList()
 *       }
 *   }
 */
abstract class CrudRepository<E : IntEntity, T : IntIdTable>(
    protected val table: T,
    protected val entityClass: IntEntityClass<E>
) {
    protected fun <R> query(block: Transaction.() -> R): R = transaction { block() }

    fun findAll(): List<E>    = query { entityClass.all().toList() }
    fun findById(id: Int): E? = query { entityClass.findById(id) }

    // Exposed 0.44: select { 조건 } 방식 사용 (selectAll().where {} 는 0.45+)
    fun existsById(id: Int): Boolean = query {
        entityClass.find { table.id eq id }.count() > 0
    }

    fun count(): Long = query {
        entityClass.all().count()
    }

    fun deleteById(id: Int): Boolean = query {
        table.deleteWhere { table.id eq id } > 0
    }

    // 전체 삭제: DAO의 all()로 전부 가져와서 각각 delete
    fun deleteAll(): Int = query {
        val all = entityClass.all().toList()
        all.forEach { it.delete() }
        all.size
    }

    fun create(init: E.() -> Unit): E = query {
        entityClass.new(init)
    }

    fun update(id: Int, block: E.() -> Unit): E? = query {
        entityClass.findById(id)?.apply(block)
    }
}