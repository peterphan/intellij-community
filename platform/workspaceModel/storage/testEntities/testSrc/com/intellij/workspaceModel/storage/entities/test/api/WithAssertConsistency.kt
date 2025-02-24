package com.intellij.workspaceModel.storage.entities.test.api

import com.intellij.workspaceModel.storage.*
import org.jetbrains.deft.ObjBuilder
import org.jetbrains.deft.Type
import com.intellij.workspaceModel.storage.EntitySource
import com.intellij.workspaceModel.storage.GeneratedCodeApiVersion
import com.intellij.workspaceModel.storage.ModifiableWorkspaceEntity
import com.intellij.workspaceModel.storage.MutableEntityStorage




// ------------------- Entity with consistency assertion --------------------------------

interface AssertConsistencyEntity : WorkspaceEntity {
  val passCheck: Boolean

  //region generated code
  //@formatter:off
  @GeneratedCodeApiVersion(0)
  interface Builder: AssertConsistencyEntity, ModifiableWorkspaceEntity<AssertConsistencyEntity>, ObjBuilder<AssertConsistencyEntity> {
      override var passCheck: Boolean
      override var entitySource: EntitySource
  }
  
  companion object: Type<AssertConsistencyEntity, Builder>() {
      operator fun invoke(passCheck: Boolean, entitySource: EntitySource, init: (Builder.() -> Unit)? = null): AssertConsistencyEntity {
          val builder = builder()
          builder.passCheck = passCheck
          builder.entitySource = entitySource
          init?.invoke(builder)
          return builder
      }
  }
  //@formatter:on
  //endregion

}
//region generated code
fun MutableEntityStorage.modifyEntity(entity: AssertConsistencyEntity, modification: AssertConsistencyEntity.Builder.() -> Unit) = modifyEntity(AssertConsistencyEntity.Builder::class.java, entity, modification)
//endregion

fun MutableEntityStorage.addAssertConsistencyEntity(passCheck: Boolean, source: EntitySource = MySource): AssertConsistencyEntity {
  val assertConsistencyEntity = AssertConsistencyEntity(passCheck, source)
  this.addEntity(assertConsistencyEntity)
  return assertConsistencyEntity
}
