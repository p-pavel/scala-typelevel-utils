/* BSD 3-Clause License
 *
 * Copyright (c) 2024, Pavel Perikov
 *
 * Contact: pavel@perikov.consulting
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted
 * provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of
 * conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other materials provided
 * with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to
 * endorse or promote products derived from this software without specific prior written
 * permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package com.perikov.typelevel.compiletime

import Impl.*
transparent inline def typeReprStructureString[T <: AnyKind]: String = ${ typeReprStructureStringImpl[T] }
transparent inline def typeAnsiString[T <: AnyKind]: String          = ${ typeAnsiStringImpl[T] }
transparent inline def exprTreeStructureString(a: Any): String       = ${ exprTreeStructureStringImpl('a) }
transparent inline def isSingletonType[T]: Boolean                   = ${ isSingletonTypeImpl[T] }

extension (s: String)
  inline def compileInfo: s.type = ${ infoImpl[s.type]('s) }
  inline def compileWarn: s.type = ${ warnImpl[s.type]('s) }

private object Impl:
  import scala.quoted.*

  def typeAnsiStringImpl[T <: AnyKind: Type](using Quotes): Expr[String] =
    import quotes.reflect.*
    val tpe = TypeRepr.of[T].dealias.simplified
    Expr(tpe.show(using Printer.TypeReprAnsiCode))

  def isSingletonTypeImpl[T: Type](using Quotes): Expr[Boolean] =
    import quotes.reflect.*
    val tpe = TypeRepr.of[T].dealias.simplified
    Expr(tpe.isSingleton)

  def typeReprStructureStringImpl[T <: AnyKind: Type](using Quotes): Expr[String] =
    import quotes.reflect.*
    val tpe = TypeRepr.of[T].dealias.simplified
    Expr(tpe.show(using Printer.TypeReprStructure))

  def infoImpl[T <: String](s: Expr[T])(using Quotes): Expr[T] =
    import quotes.reflect.*
    report.info(s.valueOrAbort)
    s

  def warnImpl[T <: String](s: Expr[T])(using Quotes): Expr[T] =
    import quotes.reflect.*
    report.warning(s.valueOrAbort)
    s

  def exprTreeStructureStringImpl(a: Expr[Any])(using Quotes): Expr[String] =
    import quotes.reflect.*
    Expr(a.asTerm.show(using Printer.TreeStructure))
