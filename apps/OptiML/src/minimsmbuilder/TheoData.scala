/*
 * This file is part of a port of MSMBuilder.
 * 
 * Copyright 2011 Stanford University
 * 
 * MSMBuilder is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 * 
 */

import reflect.Manifest;
import org.scala_lang.virtualized.SourceContext
import scala.virtualization.lms.common.Record
import optiml.compiler._
import optiml.library._
import optiml.shared._

trait TheoData extends OptiMLApplication {

  type XYZ = Tup3[Float,Float,Float]
      
  /**
   * parser for reading xyz elements from file
   */
  def lineToXYZ(line: Rep[String]) = {
    val data = line.fsplit(",")
    pack(data(0).toFloat, data(1).toFloat, data(2).toFloat)
  }
  
  /* syntactic sugar */
  implicit class XYZOpsCls(val a: Rep[XYZ]) {
    def x = a._1
    def y = a._2
    def z = a._3
  }
                           
  /* 
   Stores temporary data required during Theobald RMSD calculation.

   Notes:
     Storing temporary data allows us to avoid re-calculating the G-Values
     repeatedly. Also avoids re-centering the coordinates. 
  */                     
  
  type Theo = Record{val XYZData            : DenseMatrix[XYZ] 
                     val G                  : DenseVector[Float]
                     val numAtoms           : Int
                     val numAtomsWithPadding: Int}
  
  implicit val mTheo: Manifest[Theo]

  /*
  def theo(pathToXYZ: Rep[String], numAtoms: Option[Rep[Int]] = None, G: Option[Rep[DenseMatrix[Float]]] = None) = {
    val v = readVector[XYZ](pathToXYZ, lineToXYZ)
    
    // Create a container for intermediate values during RMSD Calculation.
    // 
    // Notes:
    //   1.  We remove center of mass.
    //   2.  We pre-calculate matrix magnitudes (ConfG)    
  }
  */
   
  // -- helpers  

  def get(x: Rep[Theo], n: Rep[IndexVector]) = {
    // doesn't compile if the applyDynamic field accesses are inside the new record definition...
    val t1 = x.XYZData
    val t2 = x.G
    val t3 = x.numAtoms
    val t4 = x.numAtomsWithPadding
    Record (
      XYZData = t1(n),
      G = t2(n),
      numAtoms = t3,
      numAtomsWithPadding = t4
    )
  }

  /*
  def set(x: Rep[Theo], index: Rep[IndexVector], value: Rep[Theo]): Rep[Theo] = Record (
    x.XYZData(index) = value.XYZData,
    x.G(index) = value.G 
  )
  */

  def len(x: Rep[Theo]) = x.XYZData.numRows
       
}
