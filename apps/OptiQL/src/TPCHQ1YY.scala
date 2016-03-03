import ppl.tests.scalatest._

import optiql.shallow._
import optiql.shallow.classes._
import optiql.shallow.classes.Table._
import optiql.shallow.RecordShallowOps._
import yy.optiql._

object TPCHQ1YY {

  @NRecord
  case class LineItem(
    val l_orderkey: Int,
    val l_partkey: Int,
    val l_suppkey: Int,
    val l_linenumber: Int,
    val l_quantity: Double,
    val l_extendedprice: Double,
    val l_discount: Double,
    val l_tax: Double,
    val l_returnflag: Char,
    val l_linestatus: Char,
    val l_shipdate: Date,
    val l_commitdate: Date,
    val l_receiptdate: Date,
    val l_shipinstruct: String,
    val l_shipmode: String,
    val l_comment: String
  )

  optiqlyy {
    val queryName = "Q1"
    def printUsage = {
      println("Usage: TPCH"+queryName+" <input directory>")
      sys.exit(-1)
    }
    
    var tpchDataPath: String = ""
    val sep = "\\|"
    def loadLineItems() = Table.fromFile[LineItem](tpchDataPath+"/lineitem.tbl", sep)
    
    // def main(args: Array[String]): Unit = {
    //   println("TPC-H " + queryName)
    //   if (args.length < 1) printUsage
      
    //   tpchDataPath = args(0)
    //   query()
    // }

    // def query() = {

    //   val lineItems = loadLineItems()

    //   val q = lineItems Where(_.l_shipdate <= Date("1998-12-01")) GroupBy(l => new Tup2(l.l_returnflag,l.l_linestatus)) Select(g => RecordShallow(
    //       returnFlag = g._1._1,
    //       lineStatus = g._1._2,
    //       sumQty = g._2.Sum(_.l_quantity),
    //       sumBasePrice = g._2.Sum(_.l_extendedprice),
    //       sumDiscountedPrice = g._2.Sum(l => l.l_extendedprice * (1.0 - l.l_discount)),
    //       sumCharge = g._2.Sum(l => l.l_extendedprice * (1.0 - l.l_discount) * (1.0 + l.l_tax)),
    //       avgQty = g._2.Average(_.l_quantity),
    //       avgPrice = g._2.Average(_.l_extendedprice),
    //       avgDiscount = g._2.Average(_.l_discount),
    //       countOrder = g._2.Count
    //     )) OrderBy(asc(_.returnFlag), asc(_.lineStatus))
      
    //   TablePrinterRecordShallow.printAsTable(q, 100)
    //   TablePrinterRecordShallow.writeAsJSON(q, "out.json")
    // }
  } 
}