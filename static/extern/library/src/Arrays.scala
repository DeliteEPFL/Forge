package LOWERCASE_DSL_NAME.library

import scala.annotation.unchecked.uncheckedVariance
import scala.reflect.{Manifest,SourceContext}
import scala.virtualization.lms.common._

trait ForgeArrayWrapper extends HUMAN_DSL_NAMEBase {
  type ForgeArray[T] = scala.Array[T]
  implicit def forgeArrayManifest[T:Manifest] = manifest[Array[T]]
  
  def array_empty[T:Manifest](__arg0: Rep[Int])(implicit __imp0: SourceContext): Rep[ForgeArray[T]] 
    = new ForgeArray[T](__arg0)
  def array_copy[T:Manifest](__arg0: Rep[ForgeArray[T]],__arg1: Rep[Int],__arg2: Rep[ForgeArray[T]],__arg3: Rep[Int],__arg4: Rep[Int])(implicit __imp0: SourceContext): Rep[Unit]
    = System.arraycopy(__arg0,__arg1,__arg2,__arg3,__arg4)
  def array_update[T:Manifest](__arg0: Rep[ForgeArray[T]],__arg1: Rep[Int],__arg2: Rep[T])(implicit __imp0: SourceContext): Rep[Unit]
    = __arg0(__arg1) = __arg2
  def array_apply[T:Manifest](__arg0: Rep[ForgeArray[T]],__arg1: Rep[Int])(implicit __imp0: SourceContext): Rep[T]
    = __arg0(__arg1)
  def array_length[T:Manifest](__arg0: Rep[ForgeArray[T]])(implicit __imp0: SourceContext): Rep[Int]          
    = __arg0.length
  def array_clone[T:Manifest](__arg0: Rep[ForgeArray[T]])(implicit __imp0: SourceContext): Rep[ForgeArray[T]]
    = __arg0.clone
  def array_sort[T:Manifest:Ordering](__arg0: Rep[ForgeArray[T]])(implicit __imp0: SourceContext): Rep[ForgeArray[T]] = {
    val d = array_empty[T](__arg0.length)
    array_copy(__arg0,0,d,0,__arg0.length)
    scala.util.Sorting.quickSort(d)
    d
  }
  def array_fromseq[T:Manifest](__arg0: Seq[Rep[T]])(implicit __imp0: SourceContext): Rep[ForgeArray[T]]
    = __arg0.toArray
  def array_string_split(__arg0: Rep[String], __arg1: Rep[String])(implicit __imp0: SourceContext): Rep[ForgeArray[String]]
    = __arg0.split(__arg1)

  def scala_array_apply[T:Manifest](__arg0: Rep[Array[T]],__arg1: Rep[Int])(implicit __imp0: SourceContext): Rep[T] 
    = array_apply(__arg0,__arg1)
  def scala_array_length[T:Manifest](__arg0: Rep[Array[T]])(implicit __imp0: SourceContext): Rep[Int] 
    = array_length(__arg0)
}

trait ForgeArrayBufferWrapper extends HUMAN_DSL_NAMEBase {
  type ForgeArrayBuffer[T] = scala.collection.mutable.ArrayBuffer[T]

  def array_buffer_empty[T:Manifest](__arg0: Rep[Int])(implicit __imp0: SourceContext): Rep[ForgeArrayBuffer[T]]
    = (new scala.collection.mutable.ArrayBuffer[T]()) ++ (new Array[T](__arg0))
  def array_buffer_copy[T:Manifest](src: Rep[ForgeArrayBuffer[T]], srcPos: Rep[Int], dest: Rep[ForgeArrayBuffer[T]], destPos: Rep[Int], length: Rep[Int])(implicit __imp0: SourceContext): Rep[Unit] = {
    for (i <- 0 until length) {
      dest(destPos+i) = src(srcPos+i)
    }
  }
  def array_buffer_update[T:Manifest](__arg0: Rep[ForgeArrayBuffer[T]],__arg1: Rep[Int],__arg2: Rep[T])(implicit __imp0: SourceContext): Rep[Unit]
    = __arg0(__arg1) = __arg2
  def array_buffer_apply[T:Manifest](__arg0: Rep[ForgeArrayBuffer[T]],__arg1: Rep[Int])(implicit __imp0: SourceContext): Rep[T]
    = __arg0(__arg1)
  def array_buffer_length[T:Manifest](__arg0: Rep[ForgeArrayBuffer[T]])(implicit __imp0: SourceContext): Rep[Int]
    = __arg0.length
  def array_buffer_set_length[T:Manifest](__arg0: Rep[ForgeArrayBuffer[T]],__arg1: Rep[Int])(implicit __imp0: SourceContext): Rep[Unit]
    = __arg0.slice(0,__arg1)
  def array_buffer_append[T:Manifest](__arg0: Rep[ForgeArrayBuffer[T]],__arg1: Rep[T])(implicit __imp0: SourceContext): Rep[Unit]
    = { __arg0 += __arg1 }
  def array_buffer_indexof[T:Manifest](__arg0: Rep[ForgeArrayBuffer[T]],__arg1: Rep[T])(implicit __imp0: SourceContext): Rep[Int]
    = __arg0.indexOf(__arg1)
}




