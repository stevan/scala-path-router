import com.twitter.util.Future
import com.twitter.finagle.Service
import com.twitter.finagle.builder.{Server, ServerBuilder}
import com.twitter.finagle.http.Http

import org.jboss.netty.handler.codec.http._
import org.jboss.netty.handler.codec.http.HttpMethod._
import org.jboss.netty.buffer.ChannelBuffer
import org.jboss.netty.buffer.ChannelBuffers.copiedBuffer
import org.jboss.netty.util.CharsetUtil.UTF_8

import java.net.InetSocketAddress

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
        def matchUri (r: (HttpMethod, String)): Option[Target] = if (isDefinedAt(r)) Some(matcher(r)) else None

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
            Future.value(resp)
        }
    }
}

import PathRouter._

// Target should be easily overridden
// to make simple custom classes
class HelloFromFoo(r: HttpRequest) extends Target(r) {
    def body = "Hello From foo\n"
}

// Target can also be overridden to
// take specific parameters as well
class HelloFromFooBarX (r: HttpRequest, private val a: String) extends Target(r) {
    def body = "Hello From foo/bar/" + a + "\n"
}


object HTTPServer extends App {

    val service = new Service[ HttpRequest, HttpResponse ] {

        def apply ( req : HttpRequest ) : Future[ HttpResponse ] = {

            // creation of routers is
            // very straight forward
            val fooRouter = new Router {
                def matcher = {
                    case Route(GET, url"/foo")            => new HelloFromFoo(req)
                    case Route(GET, url"/foo/bar/$x")     => new HelloFromFooBarX (req, x)
                    case Route(GET, url"/foo/bar")        => new Target (req) { def body = "Hello From foo/bar\n" }
                    case Route(GET, url"/foo/bar/$x/baz") => new Target (req, x) {
                        def body = ">>Hello From foo/bar/" + binding(0)  + "/baz\n"
                    }
                }
            }

            // having a catch all router
            // is recommended
            val catchAllRouter = new Router {
                def matcher = {
                    case _ => new Target (req) {
                        override def status = HttpResponseStatus.NOT_FOUND
                                 def body   = "404 - Not Found\n"
                    }
                }
            }

            // at any time you can check
            // to see if a uri is valid
            // for that particular router
            println(fooRouter.checkUri(GET, "/foo/bar/100"))

            // routers can easily be chained
            val allRoutes = fooRouter orElse catchAllRouter

            // matching is
            val uri = req.getUri()
            allRoutes.matchUri( req.getMethod() -> uri ).getOrElse(
                throw new Exception(s"Unknown Error: could not match $uri")
            ).render
        }
    }

    val address: InetSocketAddress = new InetSocketAddress(8080)

    val server = ServerBuilder()
        .codec(Http())
        .bindTo(address)
        .name("HttpServer")
        .build(service)
}
