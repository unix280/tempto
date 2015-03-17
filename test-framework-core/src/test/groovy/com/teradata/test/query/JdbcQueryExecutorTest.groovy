/*
 * Copyright 2013-2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.query

import com.teradata.test.fulfillment.jdbc.JdbcConnectivityState
import org.apache.commons.dbutils.QueryRunner
import spock.lang.Specification

import java.sql.Connection

import static com.teradata.test.assertions.QueryAssert.Row.row
import static com.teradata.test.assertions.QueryAssert.assertThat
import static com.teradata.test.fulfillment.jdbc.JdbcUtils.connection
import static com.teradata.test.fulfillment.jdbc.JdbcUtils.registerDriver
import static java.sql.JDBCType.INTEGER
import static java.sql.JDBCType.VARCHAR

class JdbcQueryExecutorTest
        extends Specification
{

  private static final JdbcConnectivityState JDBC_STATE = new JdbcConnectivityState("org.hsqldb.jdbc.JDBCDriver", "jdbc:hsqldb:mem:mydb", "sa", "")
  private JdbcQueryExecutor queryExecutor = new JdbcQueryExecutor(JDBC_STATE);

  def setupSpec()
  {
    registerDriver(JDBC_STATE)
    // TODO: use try with resources when we move to groovy 2.3
    Connection c
    QueryRunner run = new QueryRunner()
    try {
      c = connection(JDBC_STATE)
      run.update(c, "DROP SCHEMA PUBLIC CASCADE")
      run.update(c,
              "CREATE TABLE  company ( \
                comp_name varchar(100) NOT NULL, \
                comp_id int \
              )")
      run.update(c, "INSERT INTO company(comp_id, comp_name) values (1, 'Teradata')")
      run.update(c, "INSERT INTO company(comp_id, comp_name) values (2, 'Oracle')")
      run.update(c, "INSERT INTO company(comp_id, comp_name) values (3, 'Facebook')")
    }
    finally {
      if (c != null) {
        c.close()
      }
    }
  }

  def 'test'()
  {
    when:
    QueryResult result = queryExecutor.executeQuery("SELECT comp_id, comp_name FROM company ORDER BY comp_id")

    then:
    assertThat(result)
            .hasColumns(INTEGER, VARCHAR)
            .hasRowsInOrder(
            row(1, "Teradata"),
            row(2, "Oracle"),
            row(3, "Facebook"))
  }
}
