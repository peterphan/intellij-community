// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.workspaceModel.storage.entity

import com.intellij.workspaceModel.storage.WorkspaceEntity
import com.intellij.workspaceModel.storage.EntitySource
import com.intellij.workspaceModel.storage.GeneratedCodeApiVersion
import com.intellij.workspaceModel.storage.ModifiableWorkspaceEntity
import com.intellij.workspaceModel.storage.MutableEntityStorage
import org.jetbrains.deft.ObjBuilder
import org.jetbrains.deft.Type


interface TestEntity: WorkspaceEntity {
  val name: String
  val count: Int
  val anotherField: One
  //region generated code
  //@formatter:off
  @GeneratedCodeApiVersion(0)
  interface Builder: TestEntity, ModifiableWorkspaceEntity<TestEntity>, ObjBuilder<TestEntity> {
      override var name: String
      override var entitySource: EntitySource
      override var count: Int
      override var anotherField: One
  }
  
  companion object: Type<TestEntity, Builder>() {
      operator fun invoke(name: String, entitySource: EntitySource, count: Int, anotherField: One, init: (Builder.() -> Unit)? = null): TestEntity {
          val builder = builder()
          builder.name = name
          builder.entitySource = entitySource
          builder.count = count
          builder.anotherField = anotherField
          init?.invoke(builder)
          return builder
      }
  }
  //@formatter:on
  //endregion

}
//region generated code
fun MutableEntityStorage.modifyEntity(entity: TestEntity, modification: TestEntity.Builder.() -> Unit) = modifyEntity(TestEntity.Builder::class.java, entity, modification)
//endregion

data class One(val foo: String, val bar: String)