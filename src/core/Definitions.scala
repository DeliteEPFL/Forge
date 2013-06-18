package ppl.dsl.forge
package core

trait Definitions extends DerivativeTypes {
  this: Forge =>

  /**
   * String constants
   */
  val opIdentifierPrefix = "mn"
  val opArgPrefix = "__arg"
  val implicitOpArgPrefix = "__imp"
  val qu = "__quote"
  val symMarker = "__sym"
    
  /**
   * Built-in types
   */    
  // concrete types (M stands for "Meta", C stands for "Current").. these aren't exactly consistent
  lazy val MAny = tpe("Any")
  lazy val CAny = tpe("Any", stage = now)
  lazy val MInt = tpe("Int")
  lazy val CInt = tpe("Int", stage = now)
  lazy val MFloat = tpe("Float")
  lazy val CFloat = tpe("Float", stage = now)  
  lazy val MDouble = tpe("Double")
  lazy val CDouble = tpe("Double", stage = now)
  lazy val MBoolean = tpe("Boolean")
  lazy val CBoolean = tpe("Boolean", stage = now)
  lazy val MString = tpe("String")
  lazy val CString = tpe("String", stage = now)   
  lazy val MUnit = tpe("Unit")
  lazy val CUnit = tpe("Unit", stage = now)
  lazy val MNothing = tpe("Nothing")
  lazy val CNothing = tpe("Nothing", stage = now)
  lazy val MChar = tpe("Char")
  lazy val CChar = tpe("Char", stage = now)
  lazy val byName = tpe("Thunk")
  def MThunk(ret: Rep[DSLType], freq: Frequency = normal) = ftpe(List(forge_arg("", byName, None)),ret,freq) // TODO
  def MFunction(args: List[Rep[Any]], ret: Rep[DSLType], freq: Frequency = normal) = ftpe(args.zipWithIndex.map(anyToArg),ret,freq)
  lazy val MSourceContext = tpe("SourceContext", stage = now)
  
  // generic types
  // should these return a different Forge type (e.g. Rep[TypeConstructor] or Rep[GenericType]) than concrete types?
  lazy val MVar = tpe("Var", tpePar("A"))
  lazy val MArray = tpe("ForgeArray", tpePar("A"))
  lazy val MArrayBuffer = tpe("ForgeArrayBuffer", tpePar("A"))
  
  /**
   * DSLType placeholders
   */
  def varArgs(tpeArg: Rep[DSLType]): Rep[DSLType]  
  
  /**
   * Delite parallel strategies
   */  
  // def parBuffer: Rep[DeliteParallelStrategy]
  // def parFlat: Rep[DeliteParallelStrategy]
  
  /**
   * stage tags - only 2 stages
   */
  object future extends StageTag { override def toString = "future" }
  object now extends StageTag { override def toString = "now" }
  
  /**
   * code generators
   */  
  object $cala extends CodeGenerator { def name = "Scala" }  // odd things happen if you try to re-use the existing object name 'scala'
  object cuda extends CodeGenerator { def name = "Cuda" }  
  object opencl extends CodeGenerator { def name = "OpenCL" }  
  object cpp extends CodeGenerator { def name = "C" }  
  
  val generators = List($cala, cuda, opencl, cpp)
  
  /**
   * Type classes
   * DSLs can extend these by adding their own
   */
  object TManifest extends TypeClassSignature {
    def name = "Manifest"
    def prefix = "_m"
    def wrapper = Some("mtype")
  }
  object TNumeric extends TypeClassSignature {
    def name = "Numeric"
    def prefix = "_num"
    def wrapper = Some("ntype") 
  }
  object TFractional extends TypeClassSignature {
    def name = "Fractional"
    def prefix = "_frac"
    def wrapper = Some("frtype") 
  }  
  object TOrdering extends TypeClassSignature {
    def name = "Ordering"
    def prefix = "_ord"
    def wrapper = Some("otype")
  }

  
  /**
   * Method syntax types
   */
  object staticMethod extends MethodType  
  object infixMethod extends MethodType
  object directMethod extends MethodType
  object compilerMethod extends MethodType  
  object implicitMethod extends MethodType
  
  // blacklist for op names that cannot be expressed with infix methods
  // we also blacklist some operators for improved compilation performance or to avoid ambiguities in the REPL version
  // unfortunately, blacklisting arithmetic operators causes some erroneous type errors in application code for combinations that should work. however, using infix does appear to have a significant compilation cost
  var noInfixList = List("apply", "update", /*"+",*/ "-", "*", "/", "<", ">", "<=", ">=")  // string + doesn't resolve correctly in the compiler version using only implicits
    
  // blacklist for op names that need to be overridden in instance methods
  var overrideList = Set("toString", "hashCode", "equals")
  
  // blacklist for op names that need the SourceContext implicit parameter to be surpressed (usually because they construct an object with an apply method)
  var noSourceContextList = List[String]()
    
  /**
   * Effect types
   */  
  object pure extends EffectType
  object mutable extends EffectType  
  object simple extends EffectType
  case class write(args: Int*) extends EffectType
  
  /**
   * Alias hints
   */
  object nohint extends AliasHint  
  case class AliasInfo(aliases: Option[List[Int]], contains: Option[List[Int]], extracts: Option[List[Int]], copies: Option[List[Int]]) extends AliasHint
  case class AliasCopies(args: List[Int]) extends AliasHint
  
  // generic alias hint constructor
  def info(aliases: Option[List[Int]], contains: Option[List[Int]], extracts: Option[List[Int]], copies: Option[List[Int]]) = AliasInfo(aliases, contains, extracts, copies)
  
  // convenience methods for constructing common alias hints
  def copies(arg: Int): AliasHint = copies(List(arg))
  def copies(args: List[Int]) = AliasCopies(args)
  // others? aliasesSome(..)?
  
  /**
   * Frequency annotations for code motion
   */
  object normal extends Frequency
  object hot extends Frequency
  object cold extends Frequency
    
  /**
   * Parallel collections
   */
  case class ParallelCollection(val tpeArg: Rep[DSLType], val alloc: Rep[DSLOp], val size: Rep[DSLOp], val apply: Rep[DSLOp], val update: Rep[DSLOp]) extends ForgeCollectionType
  case class ParallelCollectionBuffer(
    val tpeArg: Rep[DSLType], val alloc: Rep[DSLOp], val size: Rep[DSLOp], val apply: Rep[DSLOp], val update: Rep[DSLOp],
    /*val parallelization: Rep[DSLOp],*/ val setSize: Rep[DSLOp], val appendable: Rep[DSLOp], val append: Rep[DSLOp], val copy: Rep[DSLOp]
  ) extends ForgeCollectionType
  
     
  /**
   * Op types
   */   
  abstract class DeliteOpType extends OpType
  
  /**
   * Codegenerated
   */
  def forge_codegen(generator: CodeGenerator, rule: Rep[String]): OpType
  object codegen {
    def apply(generator: CodeGenerator, rule: Rep[String]) = forge_codegen(generator,rule)
  }
  
  /**
   * Composite
   * 
   * @param retTpe    R, the return type of the function
   * @param func      string representation of the function ( => R)
   */
  def forge_composite(func: Rep[String]): OpType
  object composite {
    def apply(func: Rep[String]) = forge_composite(func)
  }
  
  /**
   * Getters / setters for DSL structs
   */
  def forge_getter(structArgIndex: Int, field: String): OpType
  object getter {
    def apply(structArgIndex: Int, field: String) = forge_getter(structArgIndex,field)
  }
  
  def forge_setter(structArgIndex: Int, field: String, value: Rep[String]): OpType
  object setter {
    def apply(structArgIndex: Int, field: String, value: Rep[String]) = forge_setter(structArgIndex,field,value)
  }
  
  /**
   * Allocates
   * 
   * @param data     The data struct that this op allocates
   * @param init     A sequence of tuples (fieldName, initialValue)
   */ 
  def forge_allocates(tpe: Rep[DSLType], init: Seq[Rep[String]]): DeliteOpType
  object allocates {
    def apply(tpe: Rep[DSLType], init: Rep[String]*) = forge_allocates(tpe, init)
  }
  

  /**
   * SingleTask
   * 
   * @param retTpe    R, the return type of the function
   * @param func      string representation of the function ( => R)
   */
  def forge_single(func: Rep[String]): DeliteOpType
  object single {
    def apply(func: Rep[String]) = forge_single(func)
  }
  
  
  // --
  // TODO: we should adopt the Delite pattern of only generating the generalized loops to simplify this, and
  //       have all simpler (user-facing patterns) instantiate a generalized loop instance. Forge should thus
  //       only deal with the generators (collect, reduce, hashcollect, hashreduce, foreach). (Issue #37)
  // --  
  
    
  /**
   * Map
   * 
   * @param tpePars   [A,R]
   * @param argIndex  index of op argument that corresponds to input collection
   * @param func      string representation of a map function A => R
   */
   def forge_map(tpePars: (Rep[DSLType],Rep[DSLType]), argIndex: Int, func: Rep[String]): DeliteOpType
   object map {
     def apply(tpePars: (Rep[DSLType],Rep[DSLType]), mapArgIndex: Int, func: Rep[String]) = forge_map(tpePars, mapArgIndex, func)
   }  
   
  /**
   * ZipWith
   * 
   * @param tpePars       [A,B,R]
   * @param argIndices    index of op arguments that correspond to zip arguments inA, inB (first and second collection respectively)
   * @param func          string representation of a zip function (A, B) => R
   */
  def forge_zip(tpePars: (Rep[DSLType],Rep[DSLType],Rep[DSLType]), argIndices: (Int,Int), func: Rep[String]): DeliteOpType
  object zip {
    // def apply[T](x: (T,T) => T)
    def apply(tpePars: (Rep[DSLType],Rep[DSLType],Rep[DSLType]), zipArgIndices: (Int,Int), func: Rep[String]) = forge_zip(tpePars, zipArgIndices, func)
  }
  
  /**
   * Reduce
   * 
   * @param tpePars   A
   * @param argIndex  index of op argument that corresponds to input collection
   * @param zero      string representation of a function => A
   * @param func      string representation of a reduce function (A, A) => A
   */
   def forge_reduce(tpePar: Rep[DSLType], argIndex: Int, zero: Rep[String], func: Rep[String]): DeliteOpType
   object reduce {
     def apply(tpePar: Rep[DSLType], redArgIndex: Int, zero: Rep[String], func: Rep[String]) = forge_reduce(tpePar, redArgIndex, zero, func)
   }
   
  /**
   * MapReduce
   * 
   * Note: this pattern should be obsoleted by the introduction of abstract IR nodes and lowerings for composite ops.
   * 
   * @param tpePars     [A,R]
   * @param argIndex    index of op argument that corresponds to input collection
   * @param map         string representation of a function A => R
   * @param zero        string representation of a function => R
   * @param reduce      string representation of a reduce function (R, R) => R
   */
   def forge_mapreduce(tpePars: (Rep[DSLType],Rep[DSLType]), argIndex: Int, map: Rep[String], zero: Rep[String], reduce: Rep[String]): DeliteOpType
   object mapReduce {
     def apply(tpePars: (Rep[DSLType],Rep[DSLType]), argIndex: Int, map: Rep[String], zero: Rep[String], reduce: Rep[String]) = forge_mapreduce(tpePars, argIndex, map, zero, reduce)
   }   
    
  /**
   * Filter
   * 
   * @param tpePars   [A,R]
   * @param argIndex  index of op argument that correspond to input collection
   * @param cond      string representation of predicate function A => Boolean
   * @param func      string representation of a map function A => R
   */
   def forge_filter(tpePars: (Rep[DSLType],Rep[DSLType]), argIndex: Int, cond: Rep[String], func: Rep[String]): DeliteOpType
   object filter {
     def apply(tpePars: (Rep[DSLType],Rep[DSLType]), filterArgIndex: Int, cond: Rep[String], func: Rep[String]) = forge_filter(tpePars, filterArgIndex, cond, func)
   }  

  /**
   * HashFilterReduce
   * 
   * @param tpePars   [A,K,V]
   * @param argIndex  index of op argument that correspond to input collection
   * @param cond      string representation of predicate function A => Boolean
   * @param key       string representation of a function A => K
   * @param map       string representation of a function A => V
   * @param zero      string representation of a function => V
   * @param reduce    string representation of a reduce function (V, V) => V
   */
   def forge_hashfilterreduce(tpePars: (Rep[DSLType],Rep[DSLType],Rep[DSLType]), argIndex: Int, cond: Rep[String], key: Rep[String], map: Rep[String], zero: Rep[String], reduce: Rep[String]): DeliteOpType
   object hashFilterReduce {
     def apply(tpePars: (Rep[DSLType],Rep[DSLType],Rep[DSLType]), hashArgIndex: Int, cond: Rep[String], key: Rep[String], map: Rep[String], zero: Rep[String], reduce: Rep[String]) = forge_hashfilterreduce(tpePars, hashArgIndex, cond, key, map, zero, reduce)
   }  
   
  
  /**
   * Foreach
   * 
   * @param tpePar    A
   * @param argIndex  index of op argument that correspond to foreach argument in 
   * @param func      string representation of a foreach function A => Unit
   */
   def forge_foreach(tpePar: Rep[DSLType], argIndex: Int, func: Rep[String]): DeliteOpType
   object foreach {
     def apply(tpePar: Rep[DSLType], foreachArgIndex: Int, func: Rep[String]) = forge_foreach(tpePar, foreachArgIndex, func)
   }            
}


trait DefinitionsExp extends Definitions with DerivativeTypesExp {
  this: ForgeExp =>
  
  /**
   * DSLType placeholders
   */
  
   // T*
   case class VarArgs(tpeArg: Rep[DSLType]) extends Def[DSLType]
   def varArgs(tpeArg: Rep[DSLType]) = VarArgs(tpeArg)  
   
  /**
   * Delite parallel strategies
   */  
   // case class ParBuffer() extends Def[DeliteParallelStrategy]
   // case class ParFlat() extends Def[DeliteParallelStrategy]
   // 
   // def parBuffer = ParBuffer()
   // def parFlat = ParFlat()

  case class CodeGenDecl(decl: Rep[String])    
  case class CodeGen(decls: scala.collection.mutable.HashMap[CodeGenerator,CodeGenDecl]) extends OpType
  def forge_codegen(generator: CodeGenerator, rule: Rep[String]) = {
    CodeGen(scala.collection.mutable.HashMap(generator -> CodeGenDecl(rule)))    
  }
  
  case class Getter(structArgIndex: Int, field: String) extends OpType
  def forge_getter(structArgIndex: Int, field: String) = Getter(structArgIndex,field)
  
  case class Setter(structArgIndex: Int, field: String, value: Rep[String]) extends OpType
  def forge_setter(structArgIndex: Int, field: String, value: Rep[String]) = Setter(structArgIndex,field,value)

  case class Composite(func: Rep[String]) extends OpType
  def forge_composite(func: Rep[String]) = Composite(func)
  
  /**
   * Delite ops
   */
  case class Allocates(tpe: Rep[DSLType], init: Seq[Rep[String]]) extends DeliteOpType
  def forge_allocates(tpe: Rep[DSLType], init: Seq[Rep[String]]) = Allocates(tpe,init)
   
  case class SingleTask(func: Rep[String]) extends DeliteOpType
  def forge_single(func: Rep[String]) = SingleTask(func)
  
  case class Map(tpePars: (Rep[DSLType],Rep[DSLType]), argIndex: Int, func: Rep[String]) extends DeliteOpType  
  def forge_map(tpePars: (Rep[DSLType],Rep[DSLType]), argIndex: Int, func: Rep[String]) = Map(tpePars, argIndex, func)
    
  case class Zip(tpePars: (Rep[DSLType],Rep[DSLType],Rep[DSLType]), argIndices: (Int,Int), func: Rep[String]) extends DeliteOpType  
  def forge_zip(tpePars: (Rep[DSLType],Rep[DSLType],Rep[DSLType]), argIndices: (Int,Int), func: Rep[String]) = Zip(tpePars, argIndices, func)
  
  case class Reduce(tpePar: Rep[DSLType], argIndex: Int, zero: Rep[String], func: Rep[String]) extends DeliteOpType
  def forge_reduce(tpePar: Rep[DSLType], argIndex: Int, zero: Rep[String], func: Rep[String]) = Reduce(tpePar, argIndex, zero, func)
  
  case class MapReduce(tpePars: (Rep[DSLType],Rep[DSLType]), argIndex: Int, map: Rep[String], zero: Rep[String], reduce: Rep[String]) extends DeliteOpType
  def forge_mapreduce(tpePars: (Rep[DSLType],Rep[DSLType]), argIndex: Int, map: Rep[String], zero: Rep[String], reduce: Rep[String]) = MapReduce(tpePars, argIndex, map, zero, reduce)
  
  case class Filter(tpePars: (Rep[DSLType],Rep[DSLType]), argIndex: Int, cond: Rep[String], func: Rep[String]) extends DeliteOpType  
  def forge_filter(tpePars: (Rep[DSLType],Rep[DSLType]), argIndex: Int, cond: Rep[String], func: Rep[String]) = Filter(tpePars, argIndex, cond, func)  
  
  case class HashFilterReduce(tpePars: (Rep[DSLType],Rep[DSLType],Rep[DSLType]), argIndex: Int, cond: Rep[String], key: Rep[String], map: Rep[String], zero: Rep[String], reduce: Rep[String]) extends DeliteOpType
  def forge_hashfilterreduce(tpePars: (Rep[DSLType],Rep[DSLType],Rep[DSLType]), argIndex: Int, cond: Rep[String], key: Rep[String], map: Rep[String], zero: Rep[String], reduce: Rep[String]) = HashFilterReduce(tpePars, argIndex, cond, key, map, zero, reduce)
  
  case class Foreach(tpePar: Rep[DSLType], argIndex: Int, func: Rep[String]) extends DeliteOpType  
  def forge_foreach(tpePar: Rep[DSLType], argIndex: Int, func: Rep[String]) = Foreach(tpePar, argIndex, func)    
    
}
