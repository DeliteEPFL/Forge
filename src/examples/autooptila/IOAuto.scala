package ppl.dsl.forge
package examples
package autooptila

import core.{ForgeApplication,ForgeApplicationRunner}

trait IOOps {
  this: AutoOptiLADSL =>

  def importIOOps() {
    val IO = grp("LAio") // avoid conflict with IOOps in LMS

    val DenseVector = lookupTpe("DenseVector")
    val DenseMatrix = lookupTpe("DenseMatrix")

    // -- input

    direct (IO) ("readVector", Nil, ("path",MString) :: DenseVector(MDouble)) implements composite ${ readVector[Double]($path, v => v(0).toDouble) }

    direct (IO) ("readMatrix", Nil, ("path", MString) :: DenseMatrix(MDouble)) implements composite ${ readMatrix[Double]($path, s => s.toDouble) }
    direct (IO) ("readMatrix", Nil, (("path", MString), ("delim", MString)) :: DenseMatrix(MDouble)) implements composite ${ readMatrix[Double]($path, s => s.toDouble, $delim) }

    val Elem = tpePar("Elem")

    // whitespace delimited by default
    direct (IO) ("readVector", Elem, MethodSignature(List(("path",MString),("schemaBldr",DenseVector(MString) ==> Elem),("delim",MString,"\"\\s+\"")), DenseVector(Elem))) implements composite ${
      val a = ForgeFileReader.readLines($path){ line =>
        val tokens = line.trim.fsplit(delim)
        val tokenVector = (0::array_length(tokens)) { i => tokens(i) }
        schemaBldr(tokenVector)
      }
      densevector_fromarray(a, true)
    }

    direct (IO) ("readMatrix", Elem, MethodSignature(List(("path",MString),("schemaBldr",MString ==> Elem),("delim",MString,"\"\\s+\"")), DenseMatrix(Elem))) implements composite ${
      val a = ForgeFileReader.readLinesUnstructured($path){ (line:Rep[String], buf:Rep[ForgeArrayBuffer[Elem]]) =>
        val tokens = line.trim.fsplit(delim)
        for (i <- 0 until array_length(tokens)) {
          array_buffer_append(buf, schemaBldr(tokens(i)))
        }
      }
      val numCols = array_length(readFirstLine(path).trim.fsplit(delim))
      densematrix_fromarray(a, array_length(a) / numCols, numCols).unsafeImmutable // unsafeImmutable needed due to struct unwrapping Reflect(Reflect(..)) bug (see LAInputReaderOps.scala line 46 in Delite)
    }

    compiler (IO) ("readFirstLine", Nil, ("path",MString) :: MString) implements codegen($cala, ${
      val xfs = new java.io.BufferedReader(new java.io.FileReader($path))
      val line = xfs.readLine()
      xfs.close()
      line
    })


    // -- output

    direct (IO) ("writeVector", Elem withBound TStringable, (("v",DenseVector(Elem)),("path",MString)) :: MUnit, effect = simple) implements composite ${
      write_vector_helper($path, densevector_raw_data($v.map(_.makeStr)), $v.length)
    }

    compiler (IO) ("write_vector_helper", Nil, (("path",MString),("data",MArray(MString)),("length",MInt)) :: MUnit, effect = simple) implements codegen($cala, ${
      val xfs = new java.io.BufferedWriter(new java.io.FileWriter($path))
      for (i <- 0 until $length) {
        xfs.write($data(i) + "\\n")
      }
      xfs.close()
    })

    direct (IO) ("writeMatrix", Elem withBound TStringable, MethodSignature(List(("m",DenseMatrix(Elem)),("path",MString),("delim",MString,"\"    \"")), MUnit), effect = simple) implements composite ${
      write_matrix_helper($path, densematrix_raw_data($m.map(_.makeStr)), $m.numRows, $m.numCols, $delim)
    }

    compiler (IO) ("write_matrix_helper", Nil, (("path",MString),("data",MArray(MString)),("numRows",MInt),("numCols",MInt),("delim",MString)) :: MUnit, effect = simple) implements codegen($cala, ${
      val xfs = new java.io.BufferedWriter(new java.io.FileWriter($path))
      for (i <- 0 until $numRows) {
        for (j <- 0 until $numCols) {
          xfs.write($data(i*$numCols+j) + $delim)
        }
        xfs.write("\\n")
      }
      xfs.close()
    })
  }
}
