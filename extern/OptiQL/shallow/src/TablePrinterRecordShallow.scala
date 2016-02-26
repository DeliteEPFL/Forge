package optiql.shallow

import optiql.shallow.classes._
import ppl.delite.runtime.data._
import scala.collection.mutable.{ArrayBuffer,HashMap}
import scala.reflect.RefinedManifest


object TablePrinterRecordShallow {

  def writeAsJSON[A:Manifest](table: Table[A], path: String) {
    val numRows = tableSize(table)
    val data = table.getClass.getMethod("data").invoke(table).asInstanceOf[Array[_]]
    val columnStrings = getFields(table)

    val xfs = new java.io.BufferedWriter(new java.io.FileWriter(path))
    xfs.write("[")
    var firstO = true
    for (i <- 0 until numRows) {
      if(!firstO) xfs.write(",")
      firstO = false
      xfs.write("{")
      var firstI = true
      for (name <- columnStrings) {
        if (!firstI) xfs.write(",")
        firstI = false
        xfs.write("\"" + name + "\" : \"" + readArray(data, name, i) + "\"")
      }
      xfs.write("}\n")
    }
    xfs.write("]")
    xfs.close()
  }

  def printAsTable[A:Manifest](table: Table[A], maxRows: Int) {
    implicit val tableStr = new StringBuilder
    val numRows = math.min(tableSize(table), maxRows)
    val data = table.getClass.getMethod("data").invoke(table).asInstanceOf[Array[_]]
    val columnStrings = getFields(table)
    val columnSizes = getTableColSizes(data, columnStrings, numRows)

    def repeat(s: String, n:Int) {
      for (i <- 0 until n) tableStr append s
    }

    def horizontalRule = {
      for(size <- columnSizes.values)
        repeat("=", size+1)
      tableStr append "=\n"
    }

    def emitRecordAsRow(row: Int) {
      tableStr append "| "

      for(col <- columnStrings) {
        val str = readArray(data, col, row)
        tableStr append str
        repeat(" ", columnSizes(col) - str.length - 1)
        tableStr append "| "
      }
      tableStr append "\n"
    }

    // Check if Table is empty
    if(numRows == 0) {
      println("=====================================================")
      println("|                  EMPTY TABLE                      |")
      println("=====================================================")
      return
    }

    horizontalRule
    tableStr append "|"
    for(col <- columnStrings) {
      tableStr append (" " + col)
      repeat(" " , columnSizes(col) - col.length - 1)
      tableStr append "|"
    }
    tableStr append "\n"
    horizontalRule

    for(r <- 0 until numRows) {
      emitRecordAsRow(r)
    }

    horizontalRule
    println(tableStr.toString)
  }

  private def tableSize(table: AnyRef) = table.getClass.getMethod("size").invoke(table) match {
    case i:Integer => i.intValue
    case i:java.lang.Long => i.intValue
    case i => throw new RuntimeException("Unexpected type: " + i + " for table size")
  }

  private def getFields[A:Manifest](table: Table[A]) = {
    implicitly[Manifest[A]] match {
      case rm:RefinedManifest[A] => rm.fields.map(_._1).toArray
      case _ => Array[String]()
    }
  }

  private def readColumn(record: Any, col: String) = {
    try {
      record.getClass.getMethod(col).invoke(record)
    } catch { case e: NoSuchMethodException => 
      ""
    }
  }

  private def readArray(x: Array[_], col: String, row: Int): String = x match {
    case a: Array[_] if col != "" => 
      readColumn(a(row), col).toString
    case a: Array[_] =>
      a(row).toString
    case _ => 
      throw new IllegalArgumentException(x.getClass.getSimpleName + " cannot be printed as a table")

  }

  private def getTableColSizes(data: Array[_], columnStrings: Array[String], numRows: Int) = {
    val colSizes = new HashMap[String,Int]

    //columns should be at least the size of the headers
    for(col <- columnStrings) {
      colSizes(col) = col.length + 2
    }

    //columns should be at least the size of maximal element
    for (col <- columnStrings) {
      for (j <- 0 until numRows) {
        val d = readArray(data, col, j)
        colSizes(col) = math.max(colSizes(col), d.length + 2)
      }
    }

    colSizes
  }
}
