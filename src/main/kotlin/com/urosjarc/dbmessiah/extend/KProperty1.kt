package com.urosjarc.dbmessiah.extend

import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty1
import kotlin.reflect.typeOf

public val <T : Any> KProperty1<T, *>.ext_canBeNull: Boolean get() = this.returnType.isMarkedNullable
public val <T : Any> KProperty1<T, *>.ext_isMutable: Boolean get() = this is KMutableProperty1<T, *>
public val <T : Any> KProperty1<T, *>.ext_isWholeNumber: Boolean get() = listOf(typeOf<Int>(), typeOf<UInt>()).contains(this.returnType)
