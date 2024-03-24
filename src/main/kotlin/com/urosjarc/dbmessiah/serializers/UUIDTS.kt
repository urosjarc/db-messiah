package com.urosjarc.dbmessiah.serializers

import com.urosjarc.dbmessiah.data.TypeSerializer
import java.util.*

/**
 * Represents a collection of type serializers for UUID in different databases.
 */
public object UUIDTS {
    public val sqlite: TypeSerializer<UUID> = IdTS.uuid.sqlite { it }
    public val postgresql: TypeSerializer<UUID> = IdTS.uuid.postgresql({ it }, { it })
    public val oracle: TypeSerializer<UUID> = IdTS.uuid.oracle { it }
    public val mysql: TypeSerializer<UUID> = IdTS.uuid.mysql { it }
    public val maria: TypeSerializer<UUID> = IdTS.uuid.maria({ it }, { it })
    public val mssql: TypeSerializer<UUID> = IdTS.uuid.mssql { it }
    public val h2: TypeSerializer<UUID> = IdTS.uuid.h2({ it }, { it })
    public val derby: TypeSerializer<UUID> = IdTS.uuid.derby { it }
    public val db2: TypeSerializer<UUID> = IdTS.uuid.db2 { it }
}
