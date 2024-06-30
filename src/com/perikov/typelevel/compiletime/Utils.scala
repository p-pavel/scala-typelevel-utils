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
