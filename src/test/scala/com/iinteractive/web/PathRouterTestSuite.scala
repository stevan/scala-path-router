package com.iinteractive.web

import org.scalatest.FunSuite
import org.scalatest.BeforeAndAfter

import org.jboss.netty.handler.codec.http._
import org.jboss.netty.handler.codec.http.HttpMethod._

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

class PathRouterTestSuite extends FunSuite with BeforeAndAfter {

    test("... basic router") {
        val req = new DefaultHttpRequest(
            HttpVersion.HTTP_1_1,
            GET,
            "/foo/bar/10"
        )

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

        // matching
        val response = allRoutes.matchUri(
            req.getMethod() -> req.getUri()
        ).get.render


        println(response)
    }
}

