package com.iinteractive.web

import scala.util.Try

object PathRouter {

    // we need to have our own methods
    object Method extends Enumeration {
        type Method = Value
        val OPTIONS, GET, HEAD, POST, PUT, DELETE = Value
    }

    // simple type alias ...
    type Request = (Method.Method, String)

    // fiddle with the route method, uri
    // and make it suitable for pattern
    // matching and destructuring bind.
    object Route {
        def unapply (r: Request): Some[Request] = Some(r)
    }

    // this code shamelessly stolen from:
    // http://hootenannylas.blogspot.com/2013/02/pattern-matching-with-string.html
    implicit class RouteContext (val sc : StringContext) {
        object url {
            def unapplySeq (s : String) : Option[Seq[String]] = {
                val regexp = (sc.parts.mkString("([^/]+)") + "[/]?$").r
                regexp.unapplySeq(s)
            }
        }
    }

    // this basically just wraps the
    // PartialFunction and adds a
    // few extra methods
    trait Router {

        def matcher : PartialFunction[Request, Target]

        // this can be used to check if
        // a given uri pattern
        // is valid for this router
        def checkUri (r: Request): Option[String] = if (isDefinedAt(r)) Some(r._2) else None

        // this will perform the match and
        // return an Option[Target]
        def matchUri (r: Request): Try[Target] = Try(matcher(r))

        def isDefinedAt (r: Request) = matcher.isDefinedAt(r)
        def orElse (x: Router) = {
            val m = matcher
            new Router { def matcher = m orElse x.matcher }
        }
    }

    // this is the basis for the Targets
    // it is pretty flexible, but on its
    // own is not terrible pretty, the
    // main priority is to capture the
    // uri bindings and provide a way
    // to return some kind of result
    abstract class Target (b: AnyRef*) {
        private val bindings : Seq[AnyRef] = b

        def binding (i: Int) = bindings(i)
        def result: AnyRef
    }
}
