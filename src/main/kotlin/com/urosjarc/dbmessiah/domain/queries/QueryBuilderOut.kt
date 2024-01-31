package com.urosjarc.dbmessiah.domain.queries

import com.urosjarc.dbmessiah.DbMessiahRepository
import com.urosjarc.dbmessiah.exceptions.SerializerException
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1


open class QueryBuilderOut<OUT: Any>(
    output: KClass<OUT>,
    repo: DbMessiahRepository,
) : QueryBuilder(repo = repo) {
    init {
        if (!repo.globalOutputs.contains(output))
            throw SerializerException("Output class '${output.simpleName}' is not registered in serializers global outputs!")
    }

    fun out(kp: KProperty1<OUT, *>): String = this.repo.escaper.wrap(kp.name)

}
