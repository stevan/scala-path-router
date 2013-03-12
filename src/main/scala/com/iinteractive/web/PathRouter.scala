package com.iinteractive.web

import org.jboss.netty.handler.codec.http._
import org.jboss.netty.handler.codec.http.HttpMethod._
import org.jboss.netty.buffer.ChannelBuffer
import org.jboss.netty.buffer.ChannelBuffers.copiedBuffer
import org.jboss.netty.util.CharsetUtil.UTF_8

import scala.util.Try

object PathRouter {

    // fiddle with the route method, uri
    // and make it suitable for pattern
    // matching and destructuring bind.
    object Route {
        def unapply (r: (HttpMethod, String)): Some[(HttpMethod, String)] = Some(r)
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

        def matcher : PartialFunction[(HttpMethod, String), Target]

        // this can be used to check if
        // a given uri pattern
        // is valid for this router
        def checkUri (r: (HttpMethod, String)): Option[String] = if (isDefinedAt(r)) Some(r._2) else None

        // this will perform the match and
        // return an Option[Target]
        def matchUri (r: (HttpMethod, String)): Try[Target] = Try(matcher(r))

        def isDefinedAt (r: (HttpMethod, String)) = matcher.isDefinedAt(r)
        def orElse (x: Router) = {
            val m = matcher
            new Router { def matcher = m orElse x.matcher }
        }
    }

    // this is the basis for the Targets
    // it is pretty flexible, but on its
    // own is not terrible pretty, the
    // main priority is to capture the
    // request context and any of the
    // uri bindings
    abstract class Target (r: HttpRequest, b: AnyRef*) {
        private val req      : HttpRequest = r
        private val bindings : Seq[AnyRef] = b

        def binding (i: Int) = bindings(i)

        // the below methods combine to create
        // the body of the response, which is
        // created by the render method

        def body: String

        def status = HttpResponseStatus.OK
        def render = {
            val resp = new DefaultHttpResponse(req.getProtocolVersion, status)
            // set headers here too
            resp.setContent(copiedBuffer(body, UTF_8))
            resp
        }
    }
}
