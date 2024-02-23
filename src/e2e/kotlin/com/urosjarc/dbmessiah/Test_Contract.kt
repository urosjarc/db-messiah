package com.urosjarc.dbmessiah

interface Test_Contract {
    fun prepare()

    fun `test table drop`()

    fun `test table create`()

    fun `test table delete`()

    fun `test table select`()

    fun `test table select page`()

    fun `test row select pk`()

    fun `test row insert`()

    fun `test row update`()

    fun `test row delete`()

    fun `test rows insert`()

    fun `test rows update`()

    fun `test rows delete`()


    fun `test rows insertBatch`()

    fun `test rows updateBatch`()

    fun `test rows deleteBatch`()

    fun `test query`()

    fun `test query(outputs)`()

    fun `test query(outputs, input)`()

    fun `test transaction with rollback all`()

    fun `test transaction with exception`()

    fun `test transaction with roolback snapshot`()
}
