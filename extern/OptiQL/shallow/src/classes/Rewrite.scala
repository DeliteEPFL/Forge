package optiql.shallow

//TODO: this object is basically a misc. grab bag of features, but most of it should be pushed directly into Forge
// We either need to import Rewrite._ if it is an object - only for OptiQL (can we do this in Forge?), or we make it a package object
package object classes {

  def groupByHackImpl[K:Manifest,V:Manifest](self: Table[V], keySelector: V => K): Table[Tuple2[K,Table[V]]] = {
    val arr = self.data.take(self.size)
    val keys = arr.map(keySelector).distinct //DeliteMap is order-preserving on keys, be consistent for sanity
    val map = arr.groupBy(keySelector)

    val pairs = for (key <- keys) yield {
      val v = map(key)
      new Tuple2(key, new Table(v.length, v))
    }

    new Table(pairs.length, pairs)
  }

  def sortHackImpl[A:Manifest](self: Table[A], comparator: (A,A) => Int): Table[A] = {
    val arr = self.data.take(self.size)
    val ord = new Ordering[A] {
      def compare(x: A, y: A): Int = comparator(x,y)
    }
    val sorted = arr.sorted(ord)
    new Table(self.size, sorted)
  }

  def compareHackImpl[A:Manifest:Ordering](lhs: A, rhs: A): Int = {
    implicitly[Ordering[A]].compare(lhs, rhs)
  }

  ///////

  def table_printastable[A:Manifest](self: Table[A],maxRows: Int = 100) = {
    TablePrinter.printAsTable(self, maxRows)
  }
  
  def table_writeasjson[A:Manifest](self: Table[A],path: String) = {
    TablePrinter.writeAsJSON(self, path)
  }

  ////////////

  // Done through Numeric
  def zeroType[T:Manifest]: T = (manifest[T] match { //need a more robust solution, e.g. type class
    //case StructType(tag,elems) => struct[T](tag, elems.map(e => (e._1, zeroType(e._2))))
    case v if v == manifest[Int] => 0
    case v if v == manifest[Long] => 0L
    case v if v == manifest[Double] => 0.0
    case v if v == manifest[Float] => 0.0f
    case _ => null
  }).asInstanceOf[T]

  // Make Bounded typeclass
  def minValue[T:Manifest]: T = (manifest[T] match {
    case v if v == manifest[Int] => scala.Int.MinValue
    case v if v == manifest[Long] => scala.Long.MinValue
    case v if v == manifest[Double] => scala.Double.MinValue
    case v if v == manifest[Float] => scala.Float.MinValue
    case v if v == manifest[Char] => scala.Char.MinValue
    case _ => null //cast_asinstanceof[Null,T](null)) //shouldn't be used for reference types
  }).asInstanceOf[T]

  // Make Bounded typeclass
  def maxValue[T:Manifest]: T = (manifest[T] match {
    case v if v == manifest[Int] => scala.Int.MaxValue
    case v if v == manifest[Long] => scala.Long.MaxValue
    case v if v == manifest[Double] => scala.Double.MaxValue
    case v if v == manifest[Float] => scala.Float.MaxValue
    case v if v == manifest[Char] => scala.Char.MaxValue
    case _ => null //cast_asinstanceof[Null,T](null)) //shouldn't be used for reference types
  }).asInstanceOf[T] 

  // TODO how to do this?
  def upgradeInt[T:Manifest](value: Int): T = (manifest[T] match {
    case v if v == manifest[Int] => value
    case v if v == manifest[Long] => value.toLong
    case v if v == manifest[Double] => value.toDouble
    case v if v == manifest[Float] => value.toFloat
    case _ => throw new RuntimeException("ERROR: don't know how to average type " + manifest[T].toString)
  }).asInstanceOf[T]

  def createRecord[T:Manifest](record: ForgeArray[String]): T = {
    val elems = manifest[T] match {
      case rm: PimpedRefinedManifest[T] => rm.fields
      case m => throw new RuntimeException(m + " does not have a PimpedRefinedManifest!")
    }

    val fields = Range(0,elems.length) map { i =>
      val (field, tp) = elems(i)
      tp.toString match {
        case s if s.contains("String") => (field, record(i))
        case "Double" => (field, record(i).toDouble)
        case "Float" => (field, record(i).toFloat)
        case "Boolean" => (field, record(i) == "true")
        case "Int" => (field, record(i).toInt)
        case "Long" => (field, record(i).toLong)
        case "Char" => (field, record(i).charAt(0))
        case d if d.contains("Date") => (field, Date(record(i)))
        case _ => throw new RuntimeException("Don't know hot to automatically parse type " + tp.toString + ". Try passing in your own parsing function instead.")
      }
    }
    
    implicitly[Manifest[T]].asInstanceOf[PimpedRefinedManifest[T]].create(fields)
  }
}